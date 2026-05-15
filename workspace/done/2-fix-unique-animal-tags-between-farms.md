# Feature: Scope Animal Tag Uniqueness by Farm

## Goal
Ensure animal tags are unique only within a farm (tenant scope), allowing the same tag to exist across different farms.

## Scope
- Backend: Animal entity, repository queries, database constraints
- Database: unique constraints/indexes
- Frontend: validation messages (if any)

## Requirements
- Remove global uniqueness constraint on animal tag
- Enforce uniqueness of tag within the same farm
- Allow duplicate tags across different farms
- Validate uniqueness at application level and database level
- Ensure clear error message when duplicate occurs within the same farm

## Constraints
- Do NOT break existing animal creation/update flows
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized
- Preserve existing farm scoping rules

## Implementation Notes
- Update database constraint to composite unique (farm_id + tag)
- Adjust repository queries/validations to consider farm context
- Ensure service layer enforces uniqueness within farm
- Avoid relying only on frontend validation
- Handle migration safely (existing data must not break constraints)

## Validation
- Same tag can be used in different farms
- Duplicate tag in the same farm is rejected
- Existing data remains valid after migration
- No regression in animal CRUD operations

## Done Criteria
- Tag uniqueness is scoped to farm
- Database and application validations are aligned
- No global uniqueness restriction remains
- Behavior is consistent across all operations