package com.jpsoftware.farmapp.unit.shared.payment;

import static org.assertj.core.api.Assertions.assertThat;

import com.jpsoftware.farmapp.shared.payment.PaymentCheckoutProvider;
import com.jpsoftware.farmapp.shared.payment.PaymentProvider;
import com.jpsoftware.farmapp.shared.payment.StripePaymentConfiguration;
import com.jpsoftware.farmapp.shared.payment.StripePaymentCheckoutProvider;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

class StripePaymentConfigurationTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withUserConfiguration(StripePaymentConfiguration.class);

    @Test
    void registersStripeCheckoutProviderBeanWithBoundProperties() {
        contextRunner
                .withPropertyValues(
                        "app.payment.stripe.public-key=pk_test_123",
                        "app.payment.stripe.secret-key=sk_test_123",
                        "app.payment.stripe.webhook-secret=whsec_123",
                        "app.payment.stripe.success-url=http://localhost:5173/plans?checkout=success",
                        "app.payment.stripe.cancel-url=http://localhost:5173/plans?checkout=cancel")
                .run(context -> {
                    assertThat(context).hasSingleBean(PaymentCheckoutProvider.class);
                    assertThat(context).hasSingleBean(StripePaymentCheckoutProvider.class);

                    PaymentCheckoutProvider provider = context.getBean(PaymentCheckoutProvider.class);

                    assertThat(provider.provider()).isEqualTo(PaymentProvider.STRIPE);
                    assertThat(provider.isConfigured()).isTrue();
                    assertThat(provider.isCheckoutImplemented()).isFalse();
                });
    }

    @Test
    void keepsStripeCheckoutProviderAvailableWhenPropertiesAreMissing() {
        contextRunner.run(context -> {
            assertThat(context).hasSingleBean(PaymentCheckoutProvider.class);

            PaymentCheckoutProvider provider = context.getBean(PaymentCheckoutProvider.class);

            assertThat(provider.provider()).isEqualTo(PaymentProvider.STRIPE);
            assertThat(provider.isConfigured()).isFalse();
            assertThat(provider.isCheckoutImplemented()).isFalse();
        });
    }
}
