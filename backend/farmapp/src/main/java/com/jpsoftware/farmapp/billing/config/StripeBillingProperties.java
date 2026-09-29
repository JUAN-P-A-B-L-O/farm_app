package com.jpsoftware.farmapp.billing.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.payment.stripe")
public class StripeBillingProperties {

    private String secretKey;
    private String webhookSecret;
    private String premiumPriceId;

    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public String getWebhookSecret() {
        return webhookSecret;
    }

    public void setWebhookSecret(String webhookSecret) {
        this.webhookSecret = webhookSecret;
    }

    public String getPremiumPriceId() {
        return premiumPriceId;
    }

    public void setPremiumPriceId(String premiumPriceId) {
        this.premiumPriceId = premiumPriceId;
    }
}
