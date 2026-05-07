# Feature: Email Confirmation Required

## Goal
Ensure newly created accounts must confirm their email before accessing the system.

## Scope
- Backend: user registration, authentication, confirmation endpoint
- Frontend: confirmation flow, login feedback, resend confirmation

## Requirements
- New accounts must be created in a “pending confirmation” state
- Send a confirmation email after registration
- Provide a link-based confirmation flow
- Block access for users with unconfirmed email
- Provide a resend confirmation option
- Display clear messages about account status

## Constraints
- Do NOT include messaging system
- Do NOT include Stripe or payment logic
- Do NOT include farm onboarding
- Do NOT implement advanced email templating
- Do NOT break existing authentication flow
- Keep changes incremental and localized

## Implementation Notes
- Add confirmation state to user entity
- Generate secure confirmation token
- Create endpoint to validate confirmation link
- Update authentication flow to check confirmation status
- Implement resend confirmation logic
- Keep email sending simple and direct
- Reuse existing patterns for validation and responses

## Validation
- New users cannot access system before confirming email
- Confirmation link activates account successfully
- Resend flow works correctly
- Clear feedback is shown for all states (pending, confirmed, error)
- No regression in login or registration flow

## Done Criteria
- Email confirmation is required before access
- Confirmation and resend flows are functional
- System clearly communicates account status
- No unrelated features introduced