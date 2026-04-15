# AI Context

## 1. Project Overview

### What the system does

`farm_app` is a farm management system focused on cattle operations. The current implementation covers operational records for animals, feeding, milk production, users, and analytics/dashboard reporting.

### Core features

- Farms:
  - create and list farms accessible to the authenticated user
  - farm is the operating boundary used by animals, feeding, production, feed types, dashboard, and analytics
- Animals:
  - create, list, retrieve, update, and delete animal records
  - track origin (`BORN` or `PURCHASED`) and optional acquisition cost
  - lifecycle is status-based (`ACTIVE`, `SOLD`, `DEAD`, `INACTIVE`)
  - optional filtering by `farmId`
- Feeding:
  - create and list feeding records
  - retrieve individual feeding entries
  - optional filtering by `animalId` and `date`
  - optional pagination on listing endpoints
- Production:
  - create and list milk production records
  - retrieve individual production entries
  - update production date and quantity
  - optional filtering by `animalId` and `date`
  - optional pagination on listing endpoints
  - summary and profit views by animal
- Users:
  - create users
  - list and retrieve users
  - users are also the accountability reference for feeding and production records
- Analytics:
  - dashboard aggregates total production, feeding cost, revenue, profit, and animal count
  - profit-oriented endpoints support `includeAcquisitionCost` and default it to `true`
  - frontend includes analytics pages and charts

### Current implementation note

- Backend is active under `backend/farmapp`
- Frontend web app is active under `frontend/web/farm_web`
- A parallel `backend/farm_app` tree exists in the repository, but `backend/farmapp` is the more complete and current backend implementation

## 2. Architecture

### Backend

The backend is a Spring Boot 3.4.x modular monolith with a layered structure per business module.

Current layers in code:

- `controller`: HTTP endpoints, request binding, status codes, `@Valid` entry points
- `service`: business rules, validations, orchestration, transaction boundaries
- `repository`: Spring Data JPA persistence and aggregate queries
- `mapper`: DTO/entity transformations
- `entity` and `dto`: persistence and API models
- `shared`: exception handling, config, reusable DTOs
- `auth`: JWT security, login flow, authentication context

### Clean Architecture alignment

The codebase is aligned with Clean Architecture principles, but it is not a strict four-ring implementation yet.

Practical mapping today:

- Domain:
  - business concepts live mostly in entities, DTOs, and service rules
  - there is no isolated pure domain layer with rich aggregates
- Application:
  - service classes act as application services/use-case orchestration
- Infrastructure:
  - repositories, security filters, JWT token service, Spring configuration, JPA entities
- Controller:
  - REST controllers in each module

Important constraint:

- Do not assume a fully separated `domain/application/infrastructure/controller` package structure already exists
- Future work should preserve current layering and move incrementally if stronger separation is introduced

### Backend design characteristics

- Controllers are intentionally thin
- Services own business validation and referential checks
- Persistence is ID-based rather than relation-heavy
- JPA entities mostly store scalar foreign keys such as `animalId`, `feedTypeId`, and `createdBy`
- Financial values such as revenue and profit are calculated at read time
- Milk price is currently hard-coded as `2.0` in reporting services

### Frontend

The frontend is a React + TypeScript + Vite application.

Current structure:

- `src/pages`: route-level screens
- `src/components`: reusable UI by domain area
- `src/services`: API access layer using Axios
- `src/types`: typed frontend contracts
- `src/layout`: application shell and navigation
- `src/context` and `src/hooks`: shared state and helpers

Frontend architectural rules already visible in code:

- component-based composition
- typed service functions wrapping HTTP calls
- route-based page organization
- translation support via language context and dictionaries
- farm selection is handled centrally through `FarmContext`

Current frontend constraint:

- the service layer exists, but the frontend does not currently attach JWT tokens to requests
- backend security now requires JWT for almost all endpoints, so frontend work must account for that without broad refactoring

## 3. API Contract

## Base behavior

- Base URL in frontend service config: `http://localhost:8080`
- Content type: JSON
- Protected endpoints require `Authorization: Bearer <jwt>`
- Error envelope is standardized:

```json
{
  "timestamp": "2026-04-07T10:00:00",
  "status": 400,
  "error": "validation message",
  "path": "/animals"
}
```

### `/farms`

Endpoints:

- `GET /farms`
- `POST /farms`

Create request:

```json
{
  "name": "North Dairy"
}
```

Required fields:

- `name`

Key validations and rules:

- `name` must not be blank
- authenticated user is required for farm creation
- created farm is owned by the authenticated user
- `GET /farms` returns farms accessible to the authenticated user
- frontend uses a dedicated farm creation flow before regular module access when no farm is selected

### `/animals`

Endpoints:

- `POST /animals`
- `GET /animals`
- `GET /animals/{id}`
- `PUT /animals/{id}`
- `DELETE /animals/{id}`

Create request:

```json
{
  "tag": "COW-101",
  "breed": "Holstein",
  "birthDate": "2022-01-15",
  "origin": "PURCHASED",
  "acquisitionCost": 1250.50,
  "farmId": "farm-001"
}
```

Required fields:

- `tag`
- `breed`
- `birthDate`
- `origin`
- `farmId`

Key validations and rules:

- `tag`, `breed`, and `farmId` must not be blank
- `birthDate` must not be null
- `origin` must be `PURCHASED` or `BORN`
- `acquisitionCost` is required and must be greater than zero when `origin = PURCHASED`
- `acquisitionCost` is cleared when `origin = BORN`
- `tag` must be unique
- new animals default to status `ACTIVE`
- `PUT /animals/{id}` can be used for lifecycle transitions by updating `status`
- `DELETE /animals/{id}` is now soft-delete behavior and marks the animal as `INACTIVE`
- `GET /animals` optionally accepts `farmId`
- update is partial, but provided string fields must not be blank

### `/productions`

Endpoints:

- `GET /productions`
- `GET /productions/{id}`
- `GET /productions/summary/by-animal?animalId=...`
- `GET /productions/summary/profit/by-animal?animalId=...&includeAcquisitionCost=true`
- `POST /productions`
- `PUT /productions/{id}`

Create request:

```json
{
  "animalId": "animal-001",
  "date": "2026-03-20",
  "quantity": 32.8,
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Update request:

```json
{
  "date": "2026-03-21",
  "quantity": 34.2
}
```

Required fields on create:

- `animalId`
- `date`
- `quantity`
- `userId`

Key validations and rules:

- `animalId` and `userId` must not be blank
- `date` must not be null
- `date` cannot be in the future
- `quantity` must be greater than zero
- referenced animal must exist and be `ACTIVE`
- `userId` must be a valid UUID and must exist as a user
- authenticated user context can override/fill `createdBy`
- list endpoint supports optional `animalId`, `date`, `page`, and `size`
- pagination is only returned when both `page` and `size` are provided
- profit summary includes acquisition cost by default and allows opting out with `includeAcquisitionCost=false`

Response characteristics:

- includes top-level production fields
- includes embedded animal summary
- does not expose `createdBy`

### `/feedings`

Endpoints:

- `POST /feedings`
- `GET /feedings`
- `GET /feedings/{id}`

Create request:

```json
{
  "animalId": "animal-001",
  "feedTypeId": "feed-type-001",
  "date": "2026-03-20",
  "quantity": 14.5,
  "userId": "550e8400-e29b-41d4-a716-446655440000"
}
```

Required fields:

- `animalId`
- `feedTypeId`
- `date`
- `quantity`
- `userId`

Key validations and rules:

- all string identifiers must not be blank
- `date` must not be null
- `quantity` must be greater than zero
- referenced animal must exist and be `ACTIVE`
- referenced feed type must exist
- `userId` must be a valid UUID and must exist as a user
- authenticated user context can override/fill `createdBy`
- list endpoint supports optional `animalId`, `date`, `page`, and `size`
- pagination is only returned when both `page` and `size` are provided

Response characteristics:

- includes top-level feeding fields
- includes embedded animal summary
- includes embedded feed type summary
- does not expose calculated feeding cost

### `/feed-types`

Endpoints:

- `POST /feed-types`
- `GET /feed-types`
- `GET /feed-types/{id}`

Create request:

```json
{
  "name": "Corn Silage",
  "costPerKg": 1.85
}
```

Required fields:

- `name`
- `costPerKg`

Key validations and rules:

- `name` must not be blank
- `costPerKg` must be greater than zero
- created feed types default to `active = true`
- current backend has no update, deactivate, or delete endpoint for feed types

Important frontend mismatch:

- frontend service code includes `updateFeedType` and `deleteFeedType`, but the current backend does not implement those endpoints

### `/users`

Endpoints:

- `POST /users`
- `GET /users`
- `GET /users/{id}`

Create request:

```json
{
  "name": "Maria Silva",
  "email": "maria.silva@farmapp.com",
  "role": "MANAGER",
  "password": "farmapp@123"
}
```

Required fields:

- `name`
- `email`
- `role`

Optional field:

- `password`

Key validations and rules:

- `name`, `email`, and `role` must not be blank
- `id` in read endpoints must be a valid UUID
- if `password` is omitted, the backend generates a random password before hashing
- `POST /users` is public
- `GET /users` and `GET /users/{id}` require JWT authentication

Important frontend mismatch:

- frontend service code currently omits `password` on user creation
- frontend service code also includes `updateUser` and `deleteUser`, but the current backend does not implement those endpoints

### Dashboard and analytics profit behavior

- `GET /dashboard` now accepts optional `includeAcquisitionCost` and defaults it to `true`
- `GET /analytics/profit` now accepts optional `includeAcquisitionCost` and defaults it to `true`
- dashboard total profit subtracts acquisition cost when enabled
- analytics profit series applies acquisition cost once to the earliest returned period because the current animal model does not store a separate acquisition date

### `/auth/login`

Endpoint:

- `POST /auth/login`

Request:

```json
{
  "email": "admin@farmapp.com",
  "password": "admin123"
}
```

Required fields:

- `email`
- `password`

Key validations and rules:

- both fields must not be blank
- invalid credentials return `401`
- success returns `accessToken` and a `user` object

Response shape:

```json
{
  "accessToken": "jwt-token",
  "user": {
    "id": "550e8400-e29b-41d4-a716-446655440000",
    "name": "Default Admin",
    "email": "admin@farmapp.com",
    "role": "MANAGER"
  }
}
```

## 4. Authentication

- Authentication is JWT-based
- Public paths:
  - `/auth/**`
  - `/v3/api-docs/**`
  - `/swagger-ui/**`
  - `/swagger-ui.html`
  - `POST /users`
- All other endpoints require authentication

### Login flow

1. Client sends `POST /auth/login` with email and password
2. Backend verifies credentials with `AuthService`
3. Backend returns `accessToken` and user summary
4. Client sends the token on protected requests using:

```http
Authorization: Bearer <jwt>
```

### Current implementation notes

- JWT filter runs before `UsernamePasswordAuthenticationFilter`
- sessions are stateless
- passwords are hashed with BCrypt
- a default admin is created on startup if the user table is empty
- default admin credentials come from environment or fallback values:
  - `ADMIN_EMAIL`, default `admin@farmapp.com`
  - `ADMIN_PASSWORD`, default `admin123`

## 5. Database

- Primary configured database: PostgreSQL
- Driver and dialect are PostgreSQL in `backend/farmapp/src/main/resources/application.properties`
- Schema generation mode is currently `spring.jpa.hibernate.ddl-auto=create`
- H2 is included for tests

### Main entities

- User
  - `id`, `name`, `email`, `role`, `password`
- Animal
  - `id`, `tag`, `breed`, `birthDate`, `status`, `farmId`
- Production
  - `id`, `animalId`, `date`, `quantity`, `createdBy`
- Feeding
  - `id`, `animalId`, `feedTypeId`, `date`, `quantity`, `createdBy`
- FeedType
  - `id`, `name`, `costPerKg`, `active`

### Database constraints and behavior

- `animals.tag` is unique
- many relational checks are enforced in services rather than via JPA relationships
- operational tables use scalar IDs rather than object associations
- feeding cost is derived from `feedings.quantity * feed_types.cost_per_kg`
- revenue and profit are derived at query time, not persisted

## 6. Coding Standards

### Backend

- Do not mix domain logic directly into controllers
- Controllers must remain thin and delegate to services
- Services own business rules, validations, and orchestration
- Do not couple business logic to JPA annotations or entity graph behavior
- Do not introduce rich JPA relation graphs unless explicitly required
- Preserve mapper usage for DTO/entity transformations
- Preserve uniform exception handling through the shared exception layer

Important interpretation:

- The codebase does use JPA entities for persistence
- However, it intentionally avoids mixing business rules with persistence concerns and avoids relation-heavy modeling

### Frontend

- Do not hardcode API contracts directly inside UI components
- Use the service layer in `src/services`
- Keep components simple and page-oriented
- Keep types in `src/types`
- Preserve the current component/page/service separation
- When adding auth-aware features, centralize token handling in the API/service layer rather than scattering headers across components

## 7. Development Rules

- Do not break existing functionality
- Do not refactor the entire codebase
- Make incremental changes only
- Always respect the existing API contract unless a contract change is explicitly approved
- Preserve existing module boundaries and naming
- Prefer extending existing services and controllers over introducing parallel patterns
- Account for the fact that backend security is stricter than the current frontend integration
- Treat `backend/farmapp` as the active backend unless there is an explicit reason to work in the legacy duplicate tree

## 8. Testing Strategy

### Automated tests

Backend tests currently include:

- Spring Boot integration tests
- controller contract tests
- service/unit tests
- auth integration tests

Observed examples:

- `AuthIntegrationTest` verifies login and JWT protection
- integration tests for animals, feedings, and productions use authenticated requests

### Manual tests

The repository includes a manual curl-based script:

- `backend/test/animal/manual test/test-animals.sh`

This script exercises:

- user creation
- login
- authenticated animal flow
- production flow
- feed type and feeding flow
- profit queries

### Testing constraints

- JWT is required for protected endpoints
- manual test flows rely on `Authorization: Bearer <token>`
- when adding or changing protected features, include authenticated test coverage

## 9. Prompt Engineering Rules

When generating code for this project:

- be safe
- be incremental
- avoid over-engineering
- follow existing patterns
- prefer compatibility over novelty
- preserve controller/service/repository/mapper boundaries
- validate all input that crosses the API boundary
- do not invent missing endpoints or frontend capabilities
- if frontend and backend are out of sync, fix the mismatch with the smallest coherent change

## 10. Known Decisions

- JWT authentication is the current security model
- manual curl script testing is part of the current workflow
- frontend and backend are separated applications
- backend is a single Spring Boot deployable unit
- PostgreSQL is the configured runtime database
- H2 is used in tests
- service-layer validation is preferred over ORM-heavy relations
- financial metrics are derived, not persisted
- deployment approach is currently simple and monolithic rather than containerized or automated
- there is no CI/CD or Docker setup in the repository at this time

## 11. DO NOT

- Do not change API contracts without explicit approval
- Do not remove existing validations
- Do not introduce breaking changes
- Do not replace thin controllers with business-heavy controllers
- Do not bypass the service layer for business operations
- Do not assume missing backend endpoints exist because frontend stubs reference them
- Do not introduce large-scale refactors to force a stricter architecture in one pass
- Do not remove JWT protection from protected endpoints
- Do not silently change derived financial logic such as milk price handling

## 12. Future Roadmap

These are reasonable next areas based on current gaps, but they are not fully implemented now:

- authentication improvements in the frontend
- token persistence and logout/session handling
- CI/CD pipeline setup
- Docker-based local and deployment workflow
- stronger environment-based configuration
- scaling and reporting improvements if data volume grows
- possible tightening of architectural separation toward stricter Clean Architecture boundaries

## 13. Version Control Rules

- Every meaningful change must result in a commit
- Commits must follow the Conventional Commits pattern
- Examples:
  - `feat: add authentication`
  - `fix: correct validation bug`
  - `refactor: simplify service logic`
- Never push automatically
- Commits should be atomic and focused
- Avoid mixing unrelated changes in a single commit

Before committing:

- ensure no breaking changes were introduced
- ensure code compiles logically

## Working Guidance For Future AI Agents

- Start by checking whether a change belongs in `backend/farmapp` or `frontend/web/farm_web`
- Preserve the current backend layering even if a broader architecture is desired
- Verify frontend requests against real backend endpoints before changing UI code
- Prefer small compatible changes that close concrete gaps
- If a requested feature assumes missing infrastructure, document the gap first and then implement the narrowest viable step
