package com.jpsoftware.farmapp.billing.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.jpsoftware.farmapp.billing.config.StripeBillingProperties;
import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

@Service
public class StripeHttpService implements StripeService {

    private static final String STRIPE_API_BASE_URL = "https://api.stripe.com";

    private final StripeBillingProperties stripeBillingProperties;
    private final ObjectMapper objectMapper;

    public StripeHttpService(StripeBillingProperties stripeBillingProperties, ObjectMapper objectMapper) {
        this.stripeBillingProperties = stripeBillingProperties;
        this.objectMapper = objectMapper;
    }

    @Override
    public String createCustomer(String email, String name, UUID userId) {
        ensureSecretKeyConfigured();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("email", email);
        form.add("name", name);
        form.add("metadata[userId]", userId.toString());

        JsonNode response = postForm("/v1/customers", form);
        return requireText(response, "id");
    }

    @Override
    public StripeCheckoutSession createCheckoutSession(
            String customerId,
            UUID userId,
            String priceId,
            String successUrl,
            String cancelUrl) {
        ensureSecretKeyConfigured();
        if (!StringUtils.hasText(priceId)) {
            throw new ValidationException("Stripe premium price is not configured");
        }

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("mode", "subscription");
        form.add("customer", customerId);
        form.add("success_url", successUrl);
        form.add("cancel_url", cancelUrl);
        form.add("line_items[0][price]", priceId);
        form.add("line_items[0][quantity]", "1");
        form.add("client_reference_id", userId.toString());
        form.add("metadata[userId]", userId.toString());
        form.add("subscription_data[metadata][userId]", userId.toString());

        JsonNode response = postForm("/v1/checkout/sessions", form);
        return new StripeCheckoutSession(
                requireText(response, "id"),
                requireText(response, "url"),
                requireText(response, "customer"));
    }

    @Override
    public StripePortalSession createPortalSession(String customerId, String returnUrl) {
        ensureSecretKeyConfigured();

        MultiValueMap<String, String> form = new LinkedMultiValueMap<>();
        form.add("customer", customerId);
        form.add("return_url", returnUrl);

        JsonNode response = postForm("/v1/billing_portal/sessions", form);
        return new StripePortalSession(requireText(response, "url"));
    }

    @Override
    public StripeWebhookEvent verifyAndParseWebhook(String payload, String signatureHeader) {
        ensureWebhookSecretConfigured();
        if (!StringUtils.hasText(signatureHeader)) {
            throw new ValidationException("Missing Stripe signature");
        }
        if (!isValidSignature(payload, signatureHeader)) {
            throw new ValidationException("Invalid Stripe signature");
        }

        try {
            JsonNode event = objectMapper.readTree(payload);
            String type = requireText(event, "type");
            String eventId = requireText(event, "id");
            JsonNode object = event.path("data").path("object");

            String customerId = textOrNull(object, "customer");
            String subscriptionId = textOrNull(object, "subscription");
            String userId = textOrNull(object.path("metadata"), "userId");
            StripeSubscriptionSnapshot subscriptionSnapshot = null;

            if (type.startsWith("customer.subscription.")) {
                subscriptionId = textOrNull(object, "id");
                subscriptionSnapshot = toSubscriptionSnapshot(object);
                userId = firstNonBlank(userId, textOrNull(object.path("metadata"), "userId"));
            } else if ("checkout.session.completed".equals(type)) {
                userId = firstNonBlank(userId, textOrNull(object, "client_reference_id"));
                if ("subscription".equalsIgnoreCase(textOrNull(object, "mode"))) {
                    subscriptionSnapshot = new StripeSubscriptionSnapshot(
                            subscriptionId,
                            customerId,
                            BillingSubscriptionStatus.CHECKOUT_PENDING,
                            false,
                            null);
                }
            } else if ("invoice.payment_failed".equals(type)) {
                subscriptionSnapshot = new StripeSubscriptionSnapshot(
                        subscriptionId,
                        customerId,
                        BillingSubscriptionStatus.PAST_DUE,
                        false,
                        extractEpochSecond(object, "period_end"));
            }

            return new StripeWebhookEvent(eventId, type, customerId, subscriptionId, userId, subscriptionSnapshot);
        } catch (ValidationException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new ValidationException("Invalid Stripe webhook payload");
        }
    }

    private JsonNode postForm(String path, MultiValueMap<String, String> form) {
        RestClient restClient = RestClient.builder()
                .baseUrl(STRIPE_API_BASE_URL)
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + stripeBillingProperties.getSecretKey().trim())
                .build();

        return restClient.post()
                .uri(path)
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(JsonNode.class);
    }

    private StripeSubscriptionSnapshot toSubscriptionSnapshot(JsonNode object) {
        BillingSubscriptionStatus status = BillingSubscriptionStatus.fromStripeStatus(textOrNull(object, "status"));
        return new StripeSubscriptionSnapshot(
                textOrNull(object, "id"),
                textOrNull(object, "customer"),
                status,
                object.path("cancel_at_period_end").asBoolean(false),
                extractEpochSecond(object, "current_period_end"));
    }

    private Instant extractEpochSecond(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        if (!value.isNumber()) {
            return null;
        }
        return Instant.ofEpochSecond(value.asLong());
    }

    private boolean isValidSignature(String payload, String signatureHeader) {
        String timestamp = null;
        List<String> signatures = new java.util.ArrayList<>();

        for (String part : signatureHeader.split(",")) {
            String[] tokens = part.split("=", 2);
            if (tokens.length != 2) {
                continue;
            }
            if ("t".equals(tokens[0])) {
                timestamp = tokens[1];
            }
            if ("v1".equals(tokens[0])) {
                signatures.add(tokens[1]);
            }
        }

        if (!StringUtils.hasText(timestamp) || signatures.isEmpty()) {
            return false;
        }

        String signedPayload = timestamp + "." + payload;
        String computed = computeHmacSha256(stripeBillingProperties.getWebhookSecret().trim(), signedPayload);
        return signatures.stream().anyMatch(candidate -> candidate.equalsIgnoreCase(computed));
    }

    private String computeHmacSha256(String secret, String payload) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] digest = mac.doFinal(payload.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (Exception exception) {
            throw new ValidationException("Unable to verify Stripe signature");
        }
    }

    private String requireText(JsonNode node, String fieldName) {
        String value = textOrNull(node, fieldName);
        if (!StringUtils.hasText(value)) {
            throw new ValidationException("Missing Stripe field: " + fieldName);
        }
        return value;
    }

    private String textOrNull(JsonNode node, String fieldName) {
        JsonNode value = node.path(fieldName);
        return value.isMissingNode() || value.isNull() ? null : value.asText(null);
    }

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private void ensureSecretKeyConfigured() {
        if (!StringUtils.hasText(stripeBillingProperties.getSecretKey())) {
            throw new ValidationException("Stripe secret key is not configured");
        }
    }

    private void ensureWebhookSecretConfigured() {
        if (!StringUtils.hasText(stripeBillingProperties.getWebhookSecret())) {
            throw new ValidationException("Stripe webhook secret is not configured");
        }
    }
}
