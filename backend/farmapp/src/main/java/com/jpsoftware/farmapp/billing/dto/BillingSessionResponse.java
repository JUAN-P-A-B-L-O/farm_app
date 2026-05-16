package com.jpsoftware.farmapp.billing.dto;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Redirect URL for an external billing session.")
public class BillingSessionResponse {

    @Schema(description = "Redirect URL returned by Stripe.", example = "https://checkout.stripe.com/c/pay/cs_test_123")
    private String url;

    public BillingSessionResponse() {
    }

    public BillingSessionResponse(String url) {
        this.url = url;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }
}
