# Feature: Fix Infinite Loading in Dashboard and Analytics

## Goal
Resolve the infinite/continuous loading issue in dashboard and analytics to ensure data loads once per valid trigger and UI remains stable.

## Scope
- Frontend: dashboard and analytics pages
- Backend: endpoints only if they contribute to repeated calls

## Requirements
- Stop continuous/repeated API calls
- Ensure data fetch happens only on valid triggers (initial load, filter/search/pagination changes)
- Prevent state updates that retrigger the same effect in a loop
- Ensure loading state transitions correctly (start → success/error → stop)

## Constraints
- Do NOT break existing data contracts or endpoints
- Do NOT refactor unrelated parts of the system
- Keep changes minimal and localized

## Implementation Notes
- Inspect effects/subscriptions triggering data fetch (e.g., useEffect) and their dependencies
- Ensure dependencies are stable (avoid recreating functions/objects on every render)
- Centralize fetch logic in service layer/hooks if pattern exists
- Add guards to avoid duplicate requests (e.g., in-flight flags or request deduping)
- Ensure filter/search changes trigger a single fetch and reset pagination if applicable
- Verify backend endpoints are not causing cascading calls (e.g., redirects/retries)

## Validation
- Dashboard and analytics load data once on initial render
- Changing filters/search triggers a single request
- No continuous network calls after data is loaded
- Loading indicators stop correctly on success or error
- No regression in data accuracy or UI behavior

## Done Criteria
- Infinite loading issue is resolved
- Network requests are controlled and predictable
- UI is stable with correct loading states
- Behavior is consistent across dashboard and analytics