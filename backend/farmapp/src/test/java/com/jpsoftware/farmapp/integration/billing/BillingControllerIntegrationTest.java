package com.jpsoftware.farmapp.integration.billing;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import com.jpsoftware.farmapp.billing.service.StripeCheckoutSession;
import com.jpsoftware.farmapp.billing.service.StripeService;
import com.jpsoftware.farmapp.billing.service.StripeSubscriptionSnapshot;
import com.jpsoftware.farmapp.billing.service.StripeWebhookEvent;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import java.time.Instant;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;

class BillingControllerIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private EmailSender emailSender;

    @MockBean
    private StripeService stripeService;

    @Test
    void shouldCreateCheckoutSessionForAuthenticatedUser() throws Exception {
        UserEntity user = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        createFarmOwnedBy(user, "Fazenda Billing");

        when(stripeService.createCustomer(anyString(), anyString(), eq(user.getId()))).thenReturn("cus_123");
        when(stripeService.createCheckoutSession(eq("cus_123"), eq(user.getId()), anyString(), anyString(), anyString()))
                .thenReturn(new StripeCheckoutSession("cs_123", "https://checkout.stripe.test/session", "cus_123"));

        mockMvc.perform(post("/billing/checkout-session")
                        .header("Authorization", bearerToken(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.url").value("https://checkout.stripe.test/session"));
    }

    @Test
    void shouldAllowWebhookWithoutAuthentication() throws Exception {
        when(stripeService.verifyAndParseWebhook(anyString(), anyString()))
                .thenReturn(new StripeWebhookEvent("evt_public", "customer.created", null, null, null, null));

        mockMvc.perform(post("/billing/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "valid")
                        .content("{\"id\":\"evt_public\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook processed successfully."));

        Assertions.assertTrue(processedStripeEventRepository.existsById("evt_public"));
    }

    @Test
    void shouldRejectWebhookWithInvalidSignature() throws Exception {
        when(stripeService.verifyAndParseWebhook(anyString(), any()))
                .thenThrow(new ValidationException("Invalid Stripe signature"));

        mockMvc.perform(post("/billing/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "invalid")
                        .content("{\"id\":\"evt_invalid\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("Assinatura Stripe inválida."));
    }

    @Test
    void shouldIgnoreDuplicateWebhookEvents() throws Exception {
        UserEntity user = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        user.setStripeCustomerId("cus_123");
        userRepository.save(user);

        when(stripeService.verifyAndParseWebhook(anyString(), anyString()))
                .thenReturn(new StripeWebhookEvent(
                        "evt_duplicate",
                        "customer.subscription.updated",
                        "cus_123",
                        "sub_123",
                        user.getId().toString(),
                        new StripeSubscriptionSnapshot(
                                "sub_123",
                                "cus_123",
                                BillingSubscriptionStatus.ACTIVE,
                                false,
                                Instant.now().plusSeconds(86_400))));

        mockMvc.perform(post("/billing/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "valid")
                        .content("{\"id\":\"evt_duplicate\"}"))
                .andExpect(status().isOk());

        mockMvc.perform(post("/billing/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "valid")
                        .content("{\"id\":\"evt_duplicate\"}"))
                .andExpect(status().isOk());

        Assertions.assertEquals(1, processedStripeEventRepository.count());
        Assertions.assertEquals(UserPlan.PRO, userRepository.findById(user.getId()).orElseThrow().getPlan());
    }

    @Test
    void shouldActivatePremiumOnlyAfterWebhookEvent() throws Exception {
        UserEntity user = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        user.setStripeCustomerId("cus_123");
        userRepository.save(user);

        when(stripeService.verifyAndParseWebhook(anyString(), anyString()))
                .thenReturn(new StripeWebhookEvent(
                        "evt_123",
                        "customer.subscription.updated",
                        "cus_123",
                        "sub_123",
                        user.getId().toString(),
                        new StripeSubscriptionSnapshot(
                                "sub_123",
                                "cus_123",
                                BillingSubscriptionStatus.ACTIVE,
                                false,
                                Instant.now().plusSeconds(86_400))));

        mockMvc.perform(post("/billing/webhook")
                        .contentType(MediaType.APPLICATION_JSON)
                        .header("Stripe-Signature", "valid")
                        .content("{\"id\":\"evt_123\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.message").value("Webhook processed successfully."));

        UserEntity updatedUser = userRepository.findById(user.getId()).orElseThrow();
        Assertions.assertEquals(UserPlan.PRO, updatedUser.getPlan());
        Assertions.assertEquals(BillingSubscriptionStatus.ACTIVE, updatedUser.getBillingSubscriptionStatus());
        Assertions.assertEquals("sub_123", updatedUser.getStripeSubscriptionId());
    }
}
