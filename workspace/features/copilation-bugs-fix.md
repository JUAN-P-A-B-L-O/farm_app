# Feature: Fix Flyway Compatibility Issue with PostgreSQL

## Goal
Resolve startup failure caused by Flyway incompatibility with the current PostgreSQL version, ensuring migrations run correctly and the application starts successfully.

## Scope
- Backend: database migration setup (Flyway)
- Build configuration (dependencies)
- Application startup flow (JPA initialization)

## Requirements
- Identify incompatibility between Flyway and PostgreSQL version
- Ensure Flyway can execute migrations successfully
- Restore proper initialization of JPA (EntityManagerFactory)
- Ensure dependent beans (repositories, filters) are created correctly
- Maintain compatibility across environments (dev and production)

## Constraints
- Do NOT remove migration mechanism
- Do NOT bypass migrations permanently
- Do NOT apply unsafe database changes
- Keep changes minimal and controlled
- Follow existing architecture and dependency patterns

## Implementation Notes
- Verify current PostgreSQL version and Flyway version
- Align versions to a compatible combination
- Adjust dependency versions if necessary (without breaking other components)
- Avoid relying on unsupported configurations
- Ensure migrations are executed before JPA initialization
- Keep solution consistent with project setup

## Validation
- Flyway starts and executes migrations successfully
- Application starts without errors
- EntityManagerFactory is initialized
- Repositories and dependent beans are created
- No regression in database behavior

## Done Criteria
- Flyway is compatible with the database version
- Migrations execute without failure
- Application starts fully
- No cascading bean creation errors remain