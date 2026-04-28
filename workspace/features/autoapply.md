# Feature: Auto-Apply Filters and Search

## Goal
Apply filters and search automatically on change, removing the need for an "Apply" button.

## Scope
- Frontend: all listing pages, dashboard, and analytics filters/search
- Backend: existing endpoints (ensure compatibility with frequent calls)

## Requirements
- Remove "Apply" button from all filter UIs
- Apply filters immediately when values change
- Apply search immediately as the user types
- Debounce search input to avoid excessive requests
- Preserve existing pagination, sorting, and filter parameters
- Reset/clear filters should trigger immediate refresh

## Constraints
- Do NOT break existing filtering behavior or results
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized

## Implementation Notes
- Centralize auto-apply logic in reusable filter/search components
- Use debouncing for text search inputs
- Trigger data fetch via service layer on filter/search state change
- Maintain synchronization with pagination (reset to first page on change)
- Avoid duplicating logic across screens

## Validation
- Filters update results immediately on change
- Search updates results as user types (with debounce)
- No unnecessary duplicate requests are made
- Pagination, sorting, and filters remain consistent
- No regression in existing screens

## Done Criteria
- "Apply" button removed across the app
- All filters and search are auto-applied
- Reusable, centralized logic implemented
- UX is responsive and consistent