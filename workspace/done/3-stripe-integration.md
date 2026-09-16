# Feature: Stripe Premium Subscription Integration

## Goal
Enable Premium subscription using Stripe Checkout, activating the user’s plan only via secure webhook events and allowing self-management via Customer Portal.

## Scope
- Backend: Stripe integration (checkout session, webhook, customer portal), plan activation
- Frontend: trigger checkout and access customer portal (no card UI)

## Requirements
- Start checkout using Stripe Checkout for recurring subscription
- Do NOT collect or store card data in the system
- Use STRIPE_PREMIUM_PRICE_ID for subscription pricing
- Create Stripe Customer linked to user (store customerId)
- Create Checkout Session for subscription
- Provide endpoint to create session and return redirect URL
- Implement webhook endpoint to handle subscription events
- Verify webhook signature using STRIPE_WEBHOOK_SECRET
- Activate Premium ONLY via webhook events (never via frontend)
- Handle lifecycle:
  - payment success → activate Premium
  - cancellation → downgrade at period end (or defined rule)
  - payment failure → reflect status accordingly (if applicable)
- Provide endpoint to create Customer Portal session
- Centralize plan check (e.g., isPremium) in a single place

## Constraints
- Do NOT trust frontend to activate Premium
- Do NOT hardcode Stripe keys or secrets
- Do NOT spread plan rules across controllers/components
- Keep changes incremental and localized
- Use environment variables:
  - STRIPE_SECRET_KEY
  - STRIPE_WEBHOOK_SECRET
  - STRIPE_PREMIUM_PRICE_ID
  - APP_FRONTEND_URL

## Implementation Notes
- Create StripeService abstraction to encapsulate provider logic
- Store minimal billing data (e.g., stripeCustomerId, subscriptionId, status)
- Map Stripe subscription status to internal plan state
- Ensure idempotent webhook handling (avoid duplicate processing)
- Secure webhook endpoint (signature verification before processing)
- Provide endpoints:
  - POST /billing/checkout-session
  - POST /billing/webhook
  - POST /billing/portal-session
- Keep plan activation logic centralized (service/policy)

## Validation
- Free user can initiate checkout and is redirected to Stripe
- After successful payment, webhook activates Premium
- Invalid/unsigned webhook requests are rejected
- Cancellation updates user plan per defined rule
- Customer Portal link works for managing subscription
- No sensitive keys exposed in code or logs

## Done Criteria
- Checkout flow works end-to-end via Stripe
- Premium is activated ONLY via webhook
- Plan state updates correctly on subscription changes
- Centralized plan check is in place
- All Stripe configuration is externalized via environment variables