package com.jpsoftware.farmapp.billing.service;

import java.util.UUID;

public interface StripeService {

    String createCustomer(String email, String name, UUID userId);

    StripeCheckoutSession createCheckoutSession(String customerId, UUID userId, String priceId, String successUrl, String cancelUrl);

    StripePortalSession createPortalSession(String customerId, String returnUrl);

    StripeWebhookEvent verifyAndParseWebhook(String payload, String signatureHeader);
}
