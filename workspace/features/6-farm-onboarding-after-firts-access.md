# Feature: Farm Onboarding After First Access

## Goal
Guide users to create their first farm after successful account creation and email confirmation, separating account setup from initial system usage.

## Scope
- Frontend: onboarding flow after first login
- Backend: validation for users without a farm

## Requirements
- After first valid login, user must be guided to create a farm
- Onboarding must be simple and focused (minimal required fields)
- System must detect users without a farm
- Users without a farm should not access full system features
- Onboarding must be resumable if interrupted
- Clear feedback and guidance throughout the process

## Constraints
- Do NOT modify signup flow
- Do NOT include email confirmation logic
- Do NOT include Stripe or plan logic
- Do NOT include employee invites or multi-farm flows
- Keep changes incremental and localized

## Implementation Notes
- Detect onboarding state based on absence of farm linked to user
- Redirect user to onboarding flow after login if needed
- Keep onboarding isolated from main application navigation
- Reuse existing farm creation logic
- Avoid coupling onboarding with unrelated features

## Validation
- New users are redirected to onboarding after first login
- Farm can be created successfully through onboarding
- Users without farm cannot access main features
- Onboarding can be resumed if interrupted
- No regression in login or navigation

## Done Criteria
- Onboarding flow is triggered correctly
- Users can create first farm easily
- System handles non-onboarded users safely
- Experience is simple and leads quickly to usable state