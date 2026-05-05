# Feature: Standardize Dependency Version Management

## Goal
Ensure consistent and stable dependency management by explicitly versioning only what is necessary and relying on Spring Boot BOM for the rest.

## Scope
- Backend build configuration (pom.xml)
- Dependency declarations
- Maven plugins and annotation processors

## Requirements
- Explicitly define versions for:
  - External libraries outside Spring Boot ecosystem
  - Libraries with independent version cycles
  - Related dependency families that must match versions
  - Annotation processors (e.g., Lombok, MapStruct)
  - Maven plugins (e.g., Compiler, Surefire, JaCoCo, Flyway)
- Do NOT define versions for:
  - Spring Boot starters
  - Dependencies managed by Spring Boot BOM
  - Internal libraries pulled by starters

## Constraints
- Do NOT break build or dependency compatibility
- Do NOT override Spring Boot managed versions unnecessarily
- Keep changes minimal and controlled
- Follow existing build structure and patterns

## Implementation Notes
- Review pom.xml and identify dependencies that should/should not define versions
- Align related dependencies to the same version where required
- Ensure annotation processors are compatible with JDK/compiler
- Validate plugin versions explicitly
- Avoid duplicate or conflicting dependency versions

## Validation
- Project builds successfully
- No dependency conflicts or warnings
- All required dependencies have correct versions
- Spring Boot managed dependencies remain aligned

## Done Criteria
- Dependency versions follow defined rules
- Build is stable and reproducible
- No redundant or conflicting version declarations remain