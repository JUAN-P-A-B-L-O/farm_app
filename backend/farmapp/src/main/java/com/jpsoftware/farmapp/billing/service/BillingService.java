package com.jpsoftware.farmapp.billing.service;

import com.jpsoftware.farmapp.billing.config.StripeBillingProperties;
import com.jpsoftware.farmapp.billing.entity.ProcessedStripeEventEntity;
import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import com.jpsoftware.farmapp.billing.repository.ProcessedStripeEventRepository;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.Instant;
import java.util.UUID;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class BillingService {

    private final UserRepository userRepository;
    private final ProcessedStripeEventRepository processedStripeEventRepository;
    private final StripeService stripeService;
    private final StripeBillingProperties stripeBillingProperties;
    private final String appFrontendUrl;

    public BillingService(
            UserRepository userRepository,
            ProcessedStripeEventRepository processedStripeEventRepository,
            StripeService stripeService,
            StripeBillingProperties stripeBillingProperties,
            @Value("${app.frontend.url:http://localhost:5173}") String appFrontendUrl) {
        this.userRepository = userRepository;
        this.processedStripeEventRepository = processedStripeEventRepository;
        this.stripeService = stripeService;
        this.stripeBillingProperties = stripeBillingProperties;
        this.appFrontendUrl = appFrontendUrl;
    }

    @Transactional
    public String createCheckoutSession(UUID userId) {
        UserEntity user = findUser(userId);
        String customerId = ensureStripeCustomer(user);
        System.out.println(user.getEmail());

        StripeCheckoutSession session = stripeService.createCheckoutSession(
                customerId,
                user.getId(),
                stripeBillingProperties.getPremiumPriceId(),
                buildFrontendUrl("?billing=success"),
                buildFrontendUrl("?billing=cancelled"));

        System.out.println(user.toString());
        user.setStripeCustomerId(session.customerId());
        user.setBillingSubscriptionStatus(BillingSubscriptionStatus.CHECKOUT_PENDING);

        userRepository.save(user);
        return session.url();
    }

    @Transactional
    public String createPortalSession(UUID userId) {
        UserEntity user = findUser(userId);
        if (!StringUtils.hasText(user.getStripeCustomerId())) {
            throw new ValidationException("Stripe customer is not available for this user");
        }

        return stripeService.createPortalSession(
                        user.getStripeCustomerId(),
                        buildFrontendUrl("?billing=portal"))
                .url();
    }

    @Transactional
    public void handleWebhook(String payload, String signatureHeader) {
        StripeWebhookEvent event = stripeService.verifyAndParseWebhook(payload, signatureHeader);
        if (isAlreadyProcessed(event.id())) {
            return;
        }

        switch (event.type()) {
            case "checkout.session.completed" -> handleCheckoutCompleted(event);
            case "customer.subscription.updated", "customer.subscription.deleted", "invoice.payment_failed" ->
                    handleSubscriptionChange(event);
            default -> {
                // Ignore unsupported events but still mark them as processed.
            }
        }

        saveProcessedEvent(event.id());
    }

    private void handleCheckoutCompleted(StripeWebhookEvent event) {
        if (!StringUtils.hasText(event.userId())) {
            return;
        }

        UserEntity user = findUser(parseUserId(event.userId()));
        user.setStripeCustomerId(firstNonBlank(event.customerId(), user.getStripeCustomerId()));
        user.setStripeSubscriptionId(firstNonBlank(event.subscriptionId(), user.getStripeSubscriptionId()));
        user.setBillingSubscriptionStatus(BillingSubscriptionStatus.CHECKOUT_PENDING);
        userRepository.save(user);
    }

    private void handleSubscriptionChange(StripeWebhookEvent event) {
        StripeSubscriptionSnapshot snapshot = event.subscriptionSnapshot();
        if (snapshot == null) {
            return;
        }

        UserEntity user = resolveUserForSubscriptionEvent(event, snapshot);
        user.setStripeCustomerId(firstNonBlank(snapshot.customerId(), user.getStripeCustomerId()));
        user.setStripeSubscriptionId(firstNonBlank(snapshot.subscriptionId(), user.getStripeSubscriptionId()));
        user.setBillingSubscriptionStatus(snapshot.status());
        user.setStripeCancelAtPeriodEnd(snapshot.cancelAtPeriodEnd());
        user.setStripeCurrentPeriodEnd(snapshot.currentPeriodEnd());
        user.setPlan(resolvePlan(snapshot.status()));
        userRepository.save(user);
    }

    private UserEntity resolveUserForSubscriptionEvent(StripeWebhookEvent event, StripeSubscriptionSnapshot snapshot) {
        if (StringUtils.hasText(snapshot.subscriptionId())) {
            return userRepository.findByStripeSubscriptionId(snapshot.subscriptionId())
                    .orElseGet(() -> resolveUserFromFallbacks(event, snapshot));
        }
        return resolveUserFromFallbacks(event, snapshot);
    }

    private UserEntity resolveUserFromFallbacks(StripeWebhookEvent event, StripeSubscriptionSnapshot snapshot) {
        if (StringUtils.hasText(event.userId())) {
            return findUser(parseUserId(event.userId()));
        }
        if (StringUtils.hasText(snapshot.customerId())) {
            return userRepository.findByStripeCustomerId(snapshot.customerId())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found for Stripe customer"));
        }
        throw new ResourceNotFoundException("User not found for Stripe event");
    }

    private UserPlan resolvePlan(BillingSubscriptionStatus status) {
        return status != null && status.isPremiumActive() ? UserPlan.PRO : UserPlan.FREE;
    }

    private String ensureStripeCustomer(UserEntity user) {
        if (StringUtils.hasText(user.getStripeCustomerId())) {
            return user.getStripeCustomerId();
        }

        String customerId = stripeService.createCustomer(user.getEmail(), user.getName(), user.getId());
        user.setStripeCustomerId(customerId);
        userRepository.save(user);
        return customerId;
    }

    private UserEntity findUser(UUID userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }

    private UUID parseUserId(String userId) {
        try {
            return UUID.fromString(userId);
        } catch (IllegalArgumentException exception) {
            throw new ValidationException("Invalid Stripe user reference");
        }
    }

    private boolean isAlreadyProcessed(String eventId) {
        return processedStripeEventRepository.existsById(eventId);
    }

    private void saveProcessedEvent(String eventId) {
        try {
            processedStripeEventRepository.save(new ProcessedStripeEventEntity(eventId, Instant.now()));
        } catch (DataIntegrityViolationException ignored) {
            // Another concurrent request already stored this event.
        }
    }

    private String buildFrontendUrl(String queryString) {
        String baseUrl = appFrontendUrl == null ? "http://localhost:5173" : appFrontendUrl.trim();
        String normalizedBaseUrl = baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
        return normalizedBaseUrl + "/plans" + queryString;
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }
}
