package com.jpsoftware.farmapp.billing.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(StripeBillingProperties.class)
public class BillingConfig {
}
