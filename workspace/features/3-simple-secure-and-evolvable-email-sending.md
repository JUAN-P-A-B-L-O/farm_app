# Feature: Simple, Secure and Evolvable Email Sending

## Goal
Provide a simple and secure way to send transactional emails (starting with account confirmation), while keeping the structure flexible for future evolution.

## Scope
- Backend: email sending logic and configuration
- Initial use case: account confirmation email

## Requirements
- Send emails directly within the current flow (no async/messaging)
- Keep email credentials and configuration outside source code
- Avoid tight coupling with a specific email provider
- Structure code to allow easy replacement of the email mechanism
- Ensure secure handling of credentials and environment configuration

## Constraints
- Do NOT introduce messaging systems or queues
- Do NOT implement async workers or complex architecture
- Do NOT include Stripe or payment logic
- Do NOT implement marketing or campaign emails
- Keep changes incremental and localized

## Implementation Notes
- Introduce an abstraction for email sending (interface/service)
- Keep provider implementation isolated
- Use environment variables or config files for credentials
- Integrate with existing registration/confirmation flow
- Keep implementation simple but extensible
- Avoid duplicating email logic across the codebase

## Validation
- Confirmation email is sent successfully after registration
- Email sending works in local and production environments
- Configuration is externalized and secure
- Email logic can be replaced without affecting other parts of the system
- No regression in registration flow

## Done Criteria
- Email sending is functional and secure
- Structure supports future provider changes
- No unnecessary complexity introduced
- System is ready for additional transactional emails