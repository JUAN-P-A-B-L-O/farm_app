package com.jpsoftware.farmapp.shared.payment;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeProperties.class)
public class StripePaymentConfiguration {

    @Bean
    PaymentCheckoutProvider stripePaymentCheckoutProvider(StripeProperties stripeProperties) {
        return new StripePaymentCheckoutProvider(stripeProperties);
    }
}
