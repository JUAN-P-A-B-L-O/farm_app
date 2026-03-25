# Architecture

## Architectural Style

The backend is a layered Spring Boot monolith organized around business modules. Each module follows the same internal pattern:

- controller: HTTP contract and request binding
- service: business logic and validation orchestration
- repository: persistence access through Spring Data JPA
- mapper: translation between DTOs and entities
- entity/dto: persistence and API models

This is a conventional production-friendly structure for a small to medium backend because it keeps responsibilities explicit, reduces accidental coupling between web and persistence concerns, and supports incremental growth without requiring distributed-system complexity.

## Runtime Components

- Client actor:
  A farm operator or administrative user calls the REST API.
- Backend API:
  A Spring Boot application exposing resource-oriented endpoints.
- Database:
  A relational database accessed through JPA repositories. The default application configuration uses H2 in-memory; the project also includes a PostgreSQL driver for alternative deployment targets.

Reference diagram: [architecture.mmd](./diagrams/architecture.mmd)

## Layer Responsibilities

## Controllers

Controllers define the external API surface:

- request path and HTTP method mapping
- request parameter binding
- request body validation trigger with `@Valid`
- HTTP response status selection

Controllers are intentionally thin. They do not perform cross-entity orchestration or financial calculations directly.

## Services

Services are the operational center of the system. They are responsible for:

- semantic validation beyond basic DTO annotations
- existence checks across related records
- business rule enforcement
- aggregation logic for profit and dashboard metrics
- transaction boundaries

The service layer is also where the system compensates for its intentionally simple persistence model. Because entities do not use object relations such as `@ManyToOne`, the services enforce most referential rules explicitly.

## Repositories

Repositories expose:

- standard CRUD access through `JpaRepository`
- filtered retrieval for production and animal listing
- aggregate queries for production totals and feeding cost

The repositories are deliberately simple and mostly data-oriented. Domain meaning is applied in the service layer.

## Mappers

Mappers isolate transformation logic:

- request DTO -> entity
- entity -> response DTO

This keeps controllers and services free from repeated field-copying code and makes default values explicit, such as:

- animal status defaulting to `ACTIVE`
- feed type active flag defaulting to `true`
- `createdBy` fields populated from request `userId`

## Request Flow

A typical request follows this sequence:

1. Client calls a REST endpoint.
2. Controller binds request data and delegates to a service.
3. Service validates payload and business invariants.
4. Service loads or checks required records through repositories.
5. Service saves or queries entities.
6. Mapper converts persistence results to response DTOs.
7. Controller returns the HTTP response.
8. Exceptions are normalized by `GlobalExceptionHandler`.

## Error Handling Strategy

The application centralizes error translation in `GlobalExceptionHandler`. This gives the API a consistent error envelope with:

- `timestamp`
- `status`
- `error`
- `path`

Operationally, this is important because it separates domain/service exceptions from HTTP concerns and keeps endpoint behavior predictable.

Main categories:

- `400 Bad Request`: validation errors and business rule violations
- `404 Not Found`: missing entities or dependent records
- `500 Internal Server Error`: unexpected failures

## How Data Moves Across Layers

### Command-style operations

Create and update operations follow a command-like path:

- input DTO enters through controller
- service validates and orchestrates
- mapper creates entity
- repository persists entity
- mapper returns response DTO

Examples:

- `POST /feedings`
- `POST /productions`
- `PUT /animals/{id}`
- `PUT /productions/{id}`

### Query-style operations

Read and summary operations follow a query-like path:

- controller accepts filters or identifiers
- service selects query strategy
- repository returns filtered or aggregated data
- service assembles output DTO

Examples:

- `GET /productions?animalId=...&date=...`
- `GET /productions/summary/by-animal`
- `GET /productions/summary/profit/by-animal`
- `GET /dashboard`

## Architectural Boundaries

## What the architecture does well

- Keeps operational logic centralized in services
- Makes read-time financial calculations explicit
- Avoids entity graph complexity in a small codebase
- Preserves a straightforward module-per-concern structure

## Current limitations

- No authentication or authorization boundary
- No explicit domain service abstractions beyond application services
- No asynchronous/event-driven processing
- No persistence-level foreign-key enforcement in entity mappings
- No separation between operational records and reporting projections

These are acceptable trade-offs for the current scope, but they define where the architecture would need to evolve first for production expansion.
