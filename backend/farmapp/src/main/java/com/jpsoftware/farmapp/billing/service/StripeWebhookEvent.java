package com.jpsoftware.farmapp.billing.service;

public record StripeWebhookEvent(
        String id,
        String type,
        String customerId,
        String subscriptionId,
        String userId,
        StripeSubscriptionSnapshot subscriptionSnapshot) {
}
