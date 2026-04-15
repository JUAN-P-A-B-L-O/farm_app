# Architecture

## Architectural Style

The backend is a layered Spring Boot monolith organized around business modules. Each module follows the same internal pattern where applicable:

- controller: HTTP contract and request binding
- service: business logic, validation, access checks, and orchestration
- repository: persistence access through Spring Data JPA
- mapper: translation between DTOs and entities
- entity/dto: persistence and API models

The codebase follows Clean Architecture principles pragmatically, but it is not a strict four-ring package layout. Services act as application/use-case orchestration, while JPA entities and repositories remain part of the current infrastructure model.

## Runtime Components

- API client:
  Authenticates, sends JWT-protected requests, and manages farm-scoped workflows.
- Backend API:
  A Spring Boot REST application with stateless JWT security.
- Database:
  A relational database accessed through JPA repositories. Runtime configuration targets PostgreSQL; tests use H2.

Reference diagram: [architecture.mmd](./diagrams/architecture.mmd)

## Layer Responsibilities

## Controllers

Controllers define the external API surface:

- request path and HTTP method mapping
- query parameter and request body binding
- request validation trigger with `@Valid`
- HTTP response status selection

Controllers are intentionally thin. They delegate business rules, cross-entity checks, farm access validation, and financial calculations to services.

## Services

Services are the operational center of the system. They are responsible for:

- semantic validation beyond basic DTO annotations
- existence checks across related records
- farm access validation through `FarmAccessService`
- authentication-context use through `AuthenticationContextService`
- business rule enforcement
- soft-delete behavior
- aggregation logic for profit, dashboard, and analytics metrics
- transaction boundaries

The service layer also compensates for the ID-based persistence model. Entities mostly store scalar IDs instead of object relations, so services enforce most referential and access rules explicitly.

## Repositories

Repositories expose:

- standard CRUD access through `JpaRepository`
- active-record filtering for soft-deleted production and feeding records
- farm-scoped retrieval
- aggregate queries for production totals and feeding cost
- milk price history lookup by effective date

## Mappers

Mappers isolate transformation logic:

- request DTO to entity
- entity to response DTO
- enriched responses with animal and feed type summaries

This keeps controllers and services free from repeated field-copying code and makes default values explicit, such as:

- animal status defaulting to `ACTIVE`
- feed type active flag defaulting to `true`
- production and feeding status defaulting to `ACTIVE`

## Security Boundary

Authentication is stateless and JWT-based.

Public paths:

- `/auth/**`
- `/v3/api-docs/**`
- `/swagger-ui/**`
- `/swagger-ui.html`
- `POST /users`

All other endpoints require authentication. The JWT filter runs before `UsernamePasswordAuthenticationFilter`, and protected requests expose the authenticated user through `AuthenticationContextService`.

## Request Flow

A typical protected request follows this sequence:

1. Client sends a JWT in the `Authorization` header.
2. JWT filter validates the token and populates the security context.
3. Controller binds request data and delegates to a service.
4. Service validates payload and business invariants.
5. Service validates farm access when a `farmId` is present or required.
6. Service loads or checks required records through repositories.
7. Service saves or queries entities.
8. Mapper converts persistence results to response DTOs.
9. Controller returns the HTTP response.
10. Exceptions are normalized by `GlobalExceptionHandler` or the authentication entry point.

## Error Handling Strategy

The application centralizes most error translation in `GlobalExceptionHandler`. The API error envelope contains:

- `timestamp`
- `status`
- `error`
- `path`

Main categories:

- `400 Bad Request`: validation and business-rule violations
- `401 Unauthorized`: invalid credentials or unauthenticated protected requests
- `404 Not Found`: missing entities or inaccessible farm-scoped resources
- `409 Conflict`: duplicate animal tag and inactive-record update conflicts
- `500 Internal Server Error`: unexpected failures

## Command-Style Operations

Create, update, sell, and delete operations follow a command-like path:

- input DTO enters through controller
- service validates and orchestrates
- mapper creates or updates entity data
- repository persists entity
- mapper returns response DTO when applicable

Examples:

- `POST /farms`
- `POST /animals`
- `POST /animals/{id}/sell`
- `POST /feedings`
- `PUT /feedings/{id}`
- `DELETE /productions/{id}`

## Query-Style Operations

Read and summary operations follow a query-like path:

- controller accepts filters or identifiers
- service selects query strategy
- repository returns filtered or aggregated data
- service assembles output DTO

Examples:

- `GET /feedings?animalId=...&date=...&farmId=...`
- `GET /productions/summary/profit/by-animal`
- `GET /milk-prices/current?farmId=...`
- `GET /dashboard?farmId=...`
- `GET /analytics/profit`

## Current Limitations

- no strict pure-domain layer separate from JPA entities
- no asynchronous or event-driven processing
- no materialized reporting projections
- no relation-heavy JPA model or explicit entity association graph
- no historical feed cost snapshots in feeding records
- historical milk price rows exist, but reporting uses the currently resolved farm price by default

These are current implementation choices, not missing endpoints.
