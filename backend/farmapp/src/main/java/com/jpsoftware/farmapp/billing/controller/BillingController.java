package com.jpsoftware.farmapp.billing.controller;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.billing.dto.BillingSessionResponse;
import com.jpsoftware.farmapp.billing.service.BillingService;
import com.jpsoftware.farmapp.shared.dto.MessageResponse;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/billing")
@Tag(name = "Billing", description = "Operations for Stripe subscription billing.")
public class BillingController {

    private final BillingService billingService;
    private final AuthenticationContextService authenticationContextService;

    public BillingController(
            BillingService billingService,
            AuthenticationContextService authenticationContextService) {
        this.billingService = billingService;
        this.authenticationContextService = authenticationContextService;
    }

    @PostMapping("/checkout-session")
    @Operation(summary = "Create checkout session", description = "Creates a Stripe Checkout session for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Checkout session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid billing state or Stripe configuration",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BillingSessionResponse> createCheckoutSession() {
        return ResponseEntity.ok(new BillingSessionResponse(
                billingService.createCheckoutSession(requireAuthenticatedUserId())));
    }

    @PostMapping("/portal-session")
    @Operation(summary = "Create customer portal session", description = "Creates a Stripe Customer Portal session for the authenticated user.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Portal session created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid billing state or Stripe configuration",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "401", description = "Authentication required",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<BillingSessionResponse> createPortalSession() {
        return ResponseEntity.ok(new BillingSessionResponse(
                billingService.createPortalSession(requireAuthenticatedUserId())));
    }

    @PostMapping(value = "/webhook", consumes = MediaType.APPLICATION_JSON_VALUE)
    @Operation(summary = "Process Stripe webhook", description = "Validates and processes Stripe subscription webhook events.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Webhook processed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid Stripe signature or payload",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> handleWebhook(
            @RequestBody String payload,
            @RequestHeader(name = "Stripe-Signature", required = false) String stripeSignature) {
        billingService.handleWebhook(payload, stripeSignature);
        return ResponseEntity.ok(new MessageResponse("Webhook processed successfully."));
    }

    private java.util.UUID requireAuthenticatedUserId() {
        return authenticationContextService.getAuthenticatedUserId()
                .orElseThrow(() -> new ValidationException("Authenticated user is required"));
    }
}
