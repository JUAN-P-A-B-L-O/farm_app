# Feature: Plan-Based Feature Segmentation

## Goal
Prepare the system to differentiate Free vs Paid features in a centralized and extensible way, without implementing payments.

## Scope
- Backend: plan-based access control logic
- Frontend: visibility/availability of premium features
- Core modules where features may be gated

## Requirements
- Define a clear separation between Free and Premium features
- Free plan must allow initial/basic usage
- Paid plan must represent full access and higher capacity
- Premium features must be:
  - Blocked (hard restriction), or
  - Hidden/disabled with upgrade indication
- Access rules must be centralized (not scattered)
- System must be ready to evolve with more granular rules

## Constraints
- Do NOT implement Stripe or any payment flow
- Do NOT add checkout, billing, coupons, or trials
- Do NOT hardcode rules across multiple layers
- Keep changes incremental and localized

## Implementation Notes
- Introduce a centralized plan/feature access layer (e.g., policy/service)
- Avoid embedding plan checks directly in controllers/components
- Reuse existing user plan information
- Design for extensibility (future limits, quotas, flags)
- Frontend should reflect access state (disabled, hidden, or upgrade hint)
- Keep logic simple but structured for growth

## Validation
- Free users can access only allowed features
- Premium features are consistently restricted
- UI reflects restricted features appropriately
- No scattered or duplicated plan rules
- No regression in existing functionality

## Done Criteria
- Centralized mechanism for feature access by plan
- Clear separation between Free and Premium features
- UI properly handles restricted features
- System is ready for future billing integration without refactor