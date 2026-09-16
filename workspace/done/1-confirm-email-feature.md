# Feature: Email Confirmation Endpoint Flow

## Goal
Ensure the email confirmation endpoint correctly validates a token from a public request and marks the user’s email as confirmed.

## Scope
- Backend: email confirmation endpoint and service logic
- Authentication flow (only for validation, not access restriction)

## Requirements
- Provide a public endpoint for email confirmation (no authentication required)
- Accept confirmation token as input
- Validate token integrity and expiration
- Locate the corresponding user securely
- Mark user email as confirmed upon valid token
- Invalidate or consume token after use
- Return clear success and error responses

## Constraints
- Do NOT require authentication for confirmation endpoint
- Do NOT expose sensitive data in responses
- Do NOT break existing registration or login flows
- Keep changes incremental and localized
- Follow existing architecture and patterns

## Implementation Notes
- Use existing token generation/validation logic
- Ensure token is securely stored (e.g., hashed if pattern exists)
- Validate expiration before confirming
- Prevent token reuse (one-time use)
- Keep controller thin and delegate logic to service layer
- Standardize response messages

## Validation
- User can confirm email via public endpoint with valid token
- Invalid or expired tokens are rejected properly
- Email is marked as confirmed after success
- Token cannot be reused
- No authentication is required for confirmation
- No regression in login or registration flow

## Done Criteria
- Public confirmation endpoint is functional
- Token validation is secure and reliable
- Email confirmation state is correctly updated
- Flow works end-to-end without authentication