package com.jpsoftware.farmapp.billing.service;

import com.jpsoftware.farmapp.billing.model.BillingSubscriptionStatus;
import java.time.Instant;

public record StripeSubscriptionSnapshot(
        String subscriptionId,
        String customerId,
        BillingSubscriptionStatus status,
        boolean cancelAtPeriodEnd,
        Instant currentPeriodEnd) {
}
