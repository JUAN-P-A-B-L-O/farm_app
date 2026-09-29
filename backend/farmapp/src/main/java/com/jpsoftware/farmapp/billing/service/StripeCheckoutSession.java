package com.jpsoftware.farmapp.billing.service;

public record StripeCheckoutSession(String id, String url, String customerId) {
}
