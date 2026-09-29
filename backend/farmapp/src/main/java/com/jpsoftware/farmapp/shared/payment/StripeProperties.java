package com.jpsoftware.farmapp.shared.payment;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.util.StringUtils;

@ConfigurationProperties(prefix = "app.payment.stripe")
public class StripeProperties {

    private String publicKey;
    private String secretKey;
    private String webhookSecret;
    private String successUrl;
    private String cancelUrl;

    public String getPublicKey() {
        return publicKey;
    }

    public void setPublicKey(String publicKey) {
        this.publicKey = publicKey;
    }

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

    public String getSuccessUrl() {
        return successUrl;
    }

    public void setSuccessUrl(String successUrl) {
        this.successUrl = successUrl;
    }

    public String getCancelUrl() {
        return cancelUrl;
    }

    public void setCancelUrl(String cancelUrl) {
        this.cancelUrl = cancelUrl;
    }

    public boolean isConfigured() {
        return StringUtils.hasText(publicKey)
                && StringUtils.hasText(secretKey)
                && StringUtils.hasText(webhookSecret)
                && StringUtils.hasText(successUrl)
                && StringUtils.hasText(cancelUrl);
    }
}
