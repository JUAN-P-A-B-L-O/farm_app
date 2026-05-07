# Feature: Signup Entry from Login Screen

## Goal
Allow users without an account to easily start the signup flow directly from the login screen, keeping the experience simple and clear.

## Scope
- Frontend: login page and new signup flow/page
- Backend: basic user registration endpoint (if not already available)

## Requirements
- Add a clear "Create account" entry on the login screen
- Implement a dedicated signup flow/page
- Keep initial registration minimal (only essential fields)
- Provide clear feedback for success, errors, and next steps
- Do NOT include any onboarding steps beyond account creation

## Constraints
- Do NOT include email confirmation
- Do NOT include farm creation
- Do NOT include payment/Stripe logic
- Do NOT include messaging/notifications
- Do NOT break existing login behavior
- Keep changes incremental and localized
- Follow existing architecture and UI patterns

## Implementation Notes
- Add navigation from login → signup page
- Reuse existing form patterns and validation
- Keep UX focused and lightweight
- Ensure backend endpoint supports minimal registration
- Avoid coupling with future onboarding flows

## Validation
- User can navigate from login to signup easily
- Signup form submits successfully
- Clear feedback is shown for success and error states
- Login flow remains unaffected

## Done Criteria
- Login screen includes signup entry
- Signup flow is functional and minimal
- No onboarding or extra steps are introduced
- UX is clear and intuitive