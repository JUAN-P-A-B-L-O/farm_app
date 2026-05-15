# Feature: Upgrade Flow for Free Users

## Goal
Ensure Free users are consistently prompted to upgrade when accessing restricted features, with a clear path to a plans/pricing page.

## Scope
- Frontend: feature gating UI, modal/popup, plans/pricing page
- Backend: plan awareness (no payment logic)

## Requirements
- Detect when a Free user attempts to access a restricted feature
- Show a modal/popup explaining the limitation
- Provide clear CTA to “Upgrade Plan”
- Redirect to a dedicated plans/pricing page
- Plans page must present available plans (Free vs Paid conceptually)
- Ensure upgrade prompts are consistent across the application

## Constraints
- Do NOT implement payment, Stripe, or checkout
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized
- Follow existing architecture and UI patterns

## Implementation Notes
- Centralize feature access checks (reuse existing plan/feature gating logic)
- Trigger upgrade modal from a shared component/hook (avoid duplication)
- Keep modal lightweight with clear messaging and actions
- Implement a reusable plans/pricing page
- Ensure navigation to plans page is consistent from all entry points

## Validation
- Free users see upgrade prompt when accessing restricted features
- Modal displays correct messaging and CTA
- Navigation to plans page works correctly
- No regression in accessible features for Free users

## Done Criteria
- Upgrade prompt is consistently triggered across restricted features
- Plans/pricing page is accessible and informative
- Flow from restriction → modal → plans page works end-to-end
- No payment logic introduced