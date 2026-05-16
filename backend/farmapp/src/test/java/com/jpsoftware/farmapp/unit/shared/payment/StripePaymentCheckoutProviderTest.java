package com.jpsoftware.farmapp.unit.shared.payment;

import com.jpsoftware.farmapp.shared.payment.PaymentProvider;
import com.jpsoftware.farmapp.shared.payment.StripePaymentCheckoutProvider;
import com.jpsoftware.farmapp.shared.payment.StripeProperties;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertEquals;

class StripePaymentCheckoutProviderTest {

    @Test
    void reportsStripeProviderWithoutCheckoutImplementation() {
        StripePaymentCheckoutProvider provider = new StripePaymentCheckoutProvider(new StripeProperties());

        assertEquals(PaymentProvider.STRIPE, provider.provider());
        assertFalse(provider.isConfigured());
        assertFalse(provider.isCheckoutImplemented());
    }

    @Test
    void becomesConfiguredOnlyWhenAllStripePlaceholdersAreFilled() {
        StripeProperties properties = new StripeProperties();
        properties.setPublicKey("pk_test_123");
        properties.setSecretKey("sk_test_123");
        properties.setWebhookSecret("whsec_123");
        properties.setSuccessUrl("http://localhost:5173/plans?checkout=success");
        properties.setCancelUrl("http://localhost:5173/plans?checkout=cancel");

        StripePaymentCheckoutProvider provider = new StripePaymentCheckoutProvider(properties);

        assertTrue(provider.isConfigured());
        assertFalse(provider.isCheckoutImplemented());
    }
}
