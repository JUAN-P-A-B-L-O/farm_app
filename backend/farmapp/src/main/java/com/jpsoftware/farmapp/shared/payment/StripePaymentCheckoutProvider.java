package com.jpsoftware.farmapp.shared.payment;

public class StripePaymentCheckoutProvider implements PaymentCheckoutProvider {

    private final StripeProperties stripeProperties;

    public StripePaymentCheckoutProvider(StripeProperties stripeProperties) {
        this.stripeProperties = stripeProperties;
    }

    @Override
    public PaymentProvider provider() {
        return PaymentProvider.STRIPE;
    }

    @Override
    public boolean isConfigured() {
        return stripeProperties.isConfigured();
    }

    @Override
    public boolean isCheckoutImplemented() {
        return false;
    }
}
