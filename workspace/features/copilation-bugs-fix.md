# Feature: Fix Database Schema Mismatch (Migration Issues)

## Goal
Resolve runtime errors caused by mismatches between application entities and database schema (e.g., missing columns), ensuring a safe and scalable migration approach.

## Scope
- Backend: JPA entities and database schema
- Database migration mechanism (e.g., Flyway or equivalent)
- Environment: dev and production consistency

## Requirements
- Identify missing or inconsistent columns between entity and database (e.g., email_confirmed)
- Ensure database schema matches current entity definitions
- Apply proper migration strategy (no manual fixes in production)
- Support safe evolution of schema across environments
- Avoid data loss in production scenarios

## Constraints
- Do NOT recreate or drop production database
- Do NOT apply destructive changes without migration control
- Keep changes incremental and controlled
- Follow existing migration/versioning strategy
- Maintain compatibility with existing data

## Implementation Notes
- Inspect entity vs actual database schema
- Identify missing migration(s) for new fields
- Use migration tool (e.g., Flyway) to create versioned schema updates
- Ensure migrations are idempotent and ordered
- For local/dev:
  - Allow reset/recreate if needed
- For production:
  - Apply forward-only migrations
- Avoid relying on Hibernate auto-ddl for schema evolution

## Validation
- Application runs without SQL errors
- All required columns exist in database
- Existing data remains intact
- Migrations run successfully in clean and existing databases
- No regression in persistence behavior

## Done Criteria
- Schema is aligned with entity model
- Migration process is properly defined and working
- No runtime SQL errors remain
- System is safe for production deployment