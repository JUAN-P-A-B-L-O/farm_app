package com.jpsoftware.farmapp.billing.model;

public enum BillingSubscriptionStatus {
    CHECKOUT_PENDING,
    ACTIVE,
    TRIALING,
    PAST_DUE,
    UNPAID,
    CANCELED,
    INCOMPLETE,
    INCOMPLETE_EXPIRED;

    public static BillingSubscriptionStatus fromStripeStatus(String value) {
        if (value == null) {
            return null;
        }

        return switch (value.trim().toLowerCase(java.util.Locale.ROOT)) {
            case "active" -> ACTIVE;
            case "trialing" -> TRIALING;
            case "past_due" -> PAST_DUE;
            case "unpaid" -> UNPAID;
            case "canceled" -> CANCELED;
            case "incomplete" -> INCOMPLETE;
            case "incomplete_expired" -> INCOMPLETE_EXPIRED;
            default -> null;
        };
    }

    public boolean isPremiumActive() {
        return this == ACTIVE || this == TRIALING;
    }

    public boolean isPendingActivation() {
        return this == CHECKOUT_PENDING || this == INCOMPLETE;
    }

    public boolean isCanceled() {
        return this == CANCELED || this == INCOMPLETE_EXPIRED;
    }
}
