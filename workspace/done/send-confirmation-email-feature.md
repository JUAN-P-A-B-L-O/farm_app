# Feature: Integrate Brevo SMTP for Email Confirmation

## Goal
Enable real email sending for account confirmation using Brevo SMTP, keeping the implementation simple, secure, and decoupled.

## Scope
- Backend: email sending infrastructure
- User registration flow (email confirmation trigger)

## Requirements
- Use Brevo SMTP:
  - Host: smtp-relay.brevo.com
  - Port: 587
  - TLS/STARTTLS enabled
- Credentials and configs must come from environment variables:
  - BREVO_SMTP_USERNAME
  - BREVO_SMTP_PASSWORD
  - APP_MAIL_FROM
  - APP_FRONTEND_URL
- Create abstraction:
  - Interface: EmailSender
  - Implementation: BrevoEmailSender or SmtpEmailSender
- Send confirmation email with link:
  `${APP_FRONTEND_URL}/confirm-email?token={token}`
- Do NOT log sensitive data (token, password)
- Handle email sending failures safely (do not break user creation flow)

## Constraints
- Do NOT use messaging/queues
- Do NOT couple user logic directly to Brevo implementation
- Do NOT hardcode credentials
- Keep changes incremental and localized
- Follow existing architecture and patterns

## Implementation Notes
- Add dependency: spring-boot-starter-mail (if missing)
- Configure SMTP via application.properties using env vars
- Implement EmailSender abstraction and isolate provider logic
- Integrate email sending into confirmation flow (post user creation)
- Ensure secure handling of tokens and credentials
- Keep implementation simple and replaceable in future

## Validation
- Confirmation email is sent successfully using Brevo SMTP
- Link contains correct token and frontend URL
- Credentials are not exposed in logs or code
- Application does not fail if email sending fails
- No regression in user creation flow

## Done Criteria
- EmailSender abstraction implemented
- Brevo SMTP integration working
- Confirmation emails are sent with correct link
- Configuration is fully externalized via environment variables
- System remains secure and decoupled