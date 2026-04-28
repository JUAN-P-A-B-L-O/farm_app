# Feature: Animal Batches for Bulk Operations

## Goal
Enable creation and usage of animal batches (lotes) to perform feeding and production operations in bulk, improving efficiency for large-scale workflows.

## Scope
- Backend: batch (lote) domain and relations
- Frontend: batch management and selection in feeding/production forms
- Feeding and Production flows

## Requirements
- Allow creation and management of batches (lotes) of animals
- Batches must belong to the current farm of the user
- Batches can only include animals from the same farm
- In feeding and production flows:
  - User can choose between individual or batch operation
- When batch is selected:
  - Operation applies to all animals in the batch
- Maintain compatibility with existing individual operations

## Constraints
- Do NOT break existing feeding/production behavior
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized

## Implementation Notes
- Introduce batch (lote) as a domain concept scoped by farm
- Ensure validation that all animals in a batch belong to the same farm
- Extend feeding/production services to support batch operations
- Reuse existing logic for individual operations where possible
- Avoid duplicating business logic for batch vs individual

## Validation
- Batches can be created and managed correctly
- Feeding/production works for both individual and batch modes
- Batch operations apply to all animals correctly
- Farm constraints are enforced
- No regression in existing individual workflows

## Done Criteria
- Users can create and use batches for operations
- Feeding and production support both individual and batch modes
- All validations (farm and animal consistency) are enforced
- Implementation is reusable and consistent with existing patterns