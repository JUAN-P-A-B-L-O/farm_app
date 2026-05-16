package com.jpsoftware.farmapp.shared.payment;

public interface PaymentCheckoutProvider {

    PaymentProvider provider();

    boolean isConfigured();

    boolean isCheckoutImplemented();
}
