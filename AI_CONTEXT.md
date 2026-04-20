# AI Context

## 1. Project Overview

### What the system does

`farm_app` is a farm management system focused on cattle operations. The current implementation covers authentication, farms, operational records for animals, feeding, milk production, feed types, milk prices, users, and analytics/dashboard reporting.

### Core features

- Farms:
  - create and list farms accessible to the authenticated user
  - farm access can come from ownership or explicit user assignment
  - farm is the operating boundary used by animals, feeding, production, feed types, dashboard, and analytics
- Animals:
  - create, list, retrieve, update, and delete animal records
  - track origin (`BORN` or `PURCHASED`) and optional acquisition cost
  - support a dedicated sell action with persisted sale price and sale date
  - lifecycle is status-based (`ACTIVE`, `SOLD`, `DEAD`, `INACTIVE`)
  - optional filtering by `farmId`
- Feeding:
  - create, list, update, and soft-delete feeding records
  - retrieve individual feeding entries
  - optional filtering by `animalId`, `date`, and `farmId`
  - optional pagination on listing endpoints
  - on create, `WORKER` users cannot choose the date; frontend hides the date field and backend uses the current server date, ignoring any incoming date
  - on create, `MANAGER` users can set the date manually
  - lifecycle is status-based (`ACTIVE`, `INACTIVE`)
- Production:
  - create, list, update, and soft-delete milk production records
  - retrieve individual production entries
  - update production animal, date, and quantity
  - optional filtering by `animalId`, `date`, and `farmId`
  - optional pagination on listing endpoints
  - on create, `WORKER` users cannot choose the date; frontend hides the date field and backend uses the current server date, ignoring any incoming date
  - on create, `MANAGER` users can set the date manually
  - lifecycle is status-based (`ACTIVE`, `INACTIVE`)
  - summary and profit views by animal
- Feed types:
  - create, list, retrieve, update, and soft-delete feed type records
  - feed types are scoped by farm
  - new feed types default to `active = true`
  - delete marks feed types inactive rather than physically deleting rows
- Users:
  - create users
  - list, retrieve, update, activate, inactivate, and delete users
  - users are also the accountability reference for feeding and production records
  - current role values used by the frontend are `MANAGER` and `WORKER`
  - `MANAGER` is the privileged role for dashboard, analytics, and delete operations
  - only authenticated `MANAGER` users can create, update, activate, inactivate, and delete users
  - new users must be assigned to at least one farm owned by the creating manager
  - user updates must also keep at least one farm owned by the authenticated manager
  - users store an `active` login flag; inactive users cannot authenticate
  - managers can reactivate inactive users through a dedicated activation action and may set a replacement password during reactivation
  - inactivation sets `active = false` and blocks login plus JWT-authenticated access
  - users who own farms cannot be inactivated or deleted, and farm owners must remain `MANAGER`
  - user email is unique and enforced in service validation plus a database constraint
  - users can store an optional `avatarUrl` image reference for list and form display
  - user listing supports optional server-side filtering by search text, status, and role
  - authenticated users can update only their own password through a dedicated self-service settings flow
- Analytics:
  - dashboard aggregates total production, feeding cost, revenue, profit, and animal count
  - dashboard and analytics endpoints require role `MANAGER`
  - sold-animal revenue is included in dashboard and analytics revenue/profit calculations
  - profit-oriented endpoints support `includeAcquisitionCost` and default it to `true`
  - frontend includes analytics pages and charts
- Milk prices:
  - milk price is managed per farm as a time-based history
  - each new price is stored as a new record with `price`, `effectiveDate`, `createdAt`, and `createdBy`
  - current price is the latest record whose `effectiveDate` is on or before today
  - if a farm has no effective milk price yet, reporting falls back to the legacy default price `2.0`

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
- milk price resolution now lives in `milkprice.service.MilkPriceService`
- production, dashboard, and analytics services use the current farm milk price by default for revenue/profit calculations

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
- reusable listing search/filter controls should be implemented as configurable shared components when multiple pages can adopt the same pattern

Current frontend constraint:

- JWT attachment is centralized in the Axios service layer
- backend security requires JWT for almost all endpoints, so frontend work should continue to centralize auth headers rather than scattering them across components

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
- `GET /farms` returns farms accessible to the authenticated user, including farms explicitly assigned to that user
- `GET /farms?ownedOnly=true` returns only farms owned by the authenticated user
- frontend uses a dedicated farm creation flow before regular module access when no farm is selected

### `/animals`

Endpoints:

- `POST /animals`
- `GET /animals`
- `GET /animals/{id}`
- `PUT /animals/{id}`
- `POST /animals/{id}/sell`
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
- `PUT /animals/{id}` can update lifecycle status except that selling must use the dedicated sell action
- `POST /animals/{id}/sell` stores `salePrice`, optional `saleDate` (defaults to current date), and changes status to `SOLD`
- only `ACTIVE` animals can be sold
- once sold, the animal cannot transition back to another status through generic update
- `DELETE /animals/{id}` is now soft-delete behavior and marks the animal as `INACTIVE`
- `DELETE /animals/{id}` requires role `MANAGER`
- `GET /animals` optionally accepts `farmId`
- update is partial, but provided string fields must not be blank

Sell request:

```json
{
  "salePrice": 3200.0,
  "saleDate": "2026-04-14"
}
```

Animal response characteristics:

- includes optional `salePrice`
- includes optional `saleDate`

### `/productions`

Endpoints:

- `GET /productions`
- `GET /productions/{id}`
- `GET /productions/summary/by-animal?animalId=...`
- `GET /productions/summary/profit/by-animal?animalId=...&includeAcquisitionCost=true`
- `POST /productions`
- `PUT /productions/{id}`
- `DELETE /productions/{id}`

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
  "animalId": "animal-001",
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
- when the authenticated user has role `WORKER`, create ignores the incoming `date` and stores the current server date
- when the authenticated user has role `MANAGER`, create still requires an explicit date
- list endpoint supports optional `animalId`, `date`, `farmId`, `page`, and `size`
- pagination is only returned when both `page` and `size` are provided
- update can change `animalId`, `date`, and `quantity`
- delete is soft-delete behavior and marks the record as `INACTIVE`
- delete requires role `MANAGER`
- profit summary includes acquisition cost by default and allows opting out with `includeAcquisitionCost=false`
- profit summary uses the current milk price for the animal's farm

Response characteristics:

- includes top-level production fields
- includes embedded animal summary
- does not expose `createdBy`

### `/milk-prices`

Endpoints:

- `POST /milk-prices?farmId=...`
- `GET /milk-prices/current?farmId=...`
- `GET /milk-prices?farmId=...`

Create request:

```json
{
  "price": 2.35,
  "effectiveDate": "2026-04-14"
}
```

Required fields:

- `farmId` query param
- `price`
- `effectiveDate`

Key validations and rules:

- `farmId` must reference an accessible existing farm
- `price` must be greater than zero and have at most 2 decimal places
- `effectiveDate` must not be null
- creating a price always inserts a new history record; previous values are never overwritten
- `GET /milk-prices/current` returns the latest price effective on or before today for the farm
- when no effective price exists yet, `GET /milk-prices/current` returns the legacy default price `2.0` with `fallbackDefault = true`
- `GET /milk-prices` returns full farm price history ordered from newest to oldest

Response characteristics:

- includes `id`, `farmId`, `price`, `effectiveDate`, `createdAt`, `createdBy`, and `fallbackDefault`

### `/feedings`

Endpoints:

- `POST /feedings`
- `GET /feedings`
- `GET /feedings/{id}`
- `PUT /feedings/{id}`
- `DELETE /feedings/{id}`

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
- when the authenticated user has role `WORKER`, create ignores the incoming `date` and stores the current server date
- when the authenticated user has role `MANAGER`, create still requires an explicit date
- list endpoint supports optional `animalId`, `date`, `farmId`, `page`, and `size`
- pagination is only returned when both `page` and `size` are provided
- update can change `animalId`, `feedTypeId`, `date`, and `quantity`
- delete is soft-delete behavior and marks the record as `INACTIVE`
- delete requires role `MANAGER`

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
- `PUT /feed-types/{id}`
- `DELETE /feed-types/{id}`

Create request:

```json
{
  "name": "Corn Silage",
  "costPerKg": 1.85
}
```

Required fields:

- `farmId` query param on create
- `name`
- `costPerKg`

Key validations and rules:

- `name` must not be blank
- `costPerKg` must be greater than zero
- created feed types default to `active = true`
- list and read endpoints return active feed types
- update changes `name` and `costPerKg`
- delete is soft-delete behavior and marks the feed type inactive
- delete requires role `MANAGER`

### `/users`

Endpoints:

- `POST /users`
- `GET /users`
- `GET /users/{id}`
- `PUT /users/{id}`
- `PATCH /users/{id}/activate`
- `PATCH /users/{id}/inactivate`
- `DELETE /users/{id}`
- `PUT /users/me/password`

Create request:

```json
{
  "name": "Maria Silva",
  "email": "maria.silva@farmapp.com",
  "role": "MANAGER",
  "password": "farmapp@123",
  "active": true,
  "avatarUrl": "https://example.com/avatar.png",
  "farmIds": ["farm-001"]
}
```

Update request:

```json
{
  "name": "Maria Silva",
  "email": "maria.silva@farmapp.com",
  "role": "WORKER",
  "avatarUrl": "https://example.com/avatar.png",
  "farmIds": ["farm-001"]
}
```

Activate request:

```json
{
  "password": "farmapp@456"
}
```

Update password request:

```json
{
  "currentPassword": "farmapp@123",
  "newPassword": "farmapp@456"
}
```

Required fields:

- `name`
- `email`
- `role`
- `active`
- `farmIds`

Conditional field:

- `password`

Key validations and rules:

- `name`, `email`, and `role` must not be blank
- `active` must not be null
- `farmIds` must contain at least one value
- `id` in read endpoints must be a valid UUID
- only authenticated `MANAGER` users can call `POST /users`, `PUT /users/{id}`, `PATCH /users/{id}/inactivate`, and `DELETE /users/{id}`
- `email` must be unique
- if `active = true`, `password` is required
- if `active = false`, login is blocked even if a password hash is stored
- every `farmId` must belong to the authenticated manager creating the user
- `avatarUrl` is optional on create and update
- update changes `name`, `email`, `role`, `avatarUrl`, and `farmIds`
- `PATCH /users/{id}/activate` marks the user active again and may replace the password used for the next login
- password changes do not go through the manager edit endpoint
- `PUT /users/me/password` is self-service only for the authenticated user
- `currentPassword` and `newPassword` must not be blank
- `currentPassword` must match the stored password
- `newPassword` must differ from `currentPassword`
- `PATCH /users/{id}/activate` requires role `MANAGER`
- `PATCH /users/{id}/inactivate` marks the user inactive instead of deleting the row
- `DELETE /users/{id}` removes the user row and its farm assignments
- users who own farms cannot be inactivated or deleted
- users who own farms cannot be changed from `MANAGER` to another role
- `GET /users` accepts optional `search`, `active`, and `role` filters
- `GET /users` and `GET /users/{id}` require JWT authentication

### Dashboard and analytics profit behavior

- `GET /dashboard` requires role `MANAGER`
- all `/analytics/**` endpoints require role `MANAGER`
- `GET /dashboard` now accepts optional `includeAcquisitionCost` and defaults it to `true`
- `GET /analytics/profit` now accepts optional `includeAcquisitionCost` and defaults it to `true`
- dashboard revenue now includes milk revenue plus sold-animal revenue
- dashboard total profit subtracts feeding cost and, when enabled, acquisition cost
- analytics profit series applies acquisition cost once to the earliest returned period because the current animal model does not store a separate acquisition date
- analytics profit series now also recognizes sold-animal revenue on each animal's `saleDate`
- milk revenue in dashboard and analytics is based on the current milk price resolved for each farm
- historical milk price records are preserved for future period-aware analytics, but current reporting still applies the latest effective farm price by default

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
- All other endpoints require authentication
- Role-based authorization:
  - `MANAGER` can access dashboard and analytics
  - `MANAGER` can create, update, activate, inactivate, and delete users
  - `MANAGER` can perform `DELETE` requests
  - non-manager authenticated users receive `403 Forbidden` for user creation, dashboard, analytics, and delete operations
  - any authenticated user can call `PUT /users/me/password` to update their own password

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
- inactive users are rejected by the login service and by JWT request authentication
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
  - `id`, `name`, `email`, `role`, `password`, `active`
- Farm
  - `id`, `name`, `ownerId`
- UserFarmAssignment
  - `id`, `userId`, `farmId`
- Animal
  - `id`, `tag`, `breed`, `birthDate`, `status`, `origin`, `acquisitionCost`, `salePrice`, `saleDate`, `farmId`
- Production
  - `id`, `animalId`, `date`, `quantity`, `createdBy`, `farmId`, `status`
- Feeding
  - `id`, `animalId`, `feedTypeId`, `date`, `quantity`, `createdBy`, `farmId`, `status`
- MilkPrice
  - `id`, `farmId`, `price`, `effectiveDate`, `createdAt`, `createdBy`
- FeedType
  - `id`, `name`, `costPerKg`, `active`, `farmId`

### Database constraints and behavior

- `animals.tag` is unique
- `users.email` is unique
- many relational checks are enforced in services rather than via JPA relationships
- operational tables use scalar IDs rather than object associations
- user-to-farm access is persisted through a scalar-ID assignment table rather than relation-heavy JPA mappings
- production and feeding delete behavior is implemented with `status = INACTIVE`
- feed type delete behavior is implemented with `active = false`
- feeding cost is derived from `feedings.quantity * feed_types.cost_per_kg`
- milk price history is persisted as append-only records per farm
- revenue and profit are derived at query time, not persisted

## 6. Coding Standards

### Backend

- Do not mix domain logic directly into controllers
- Controllers must remain thin and delegate to services
- Services own business rules, validations, and orchestration
- Services use constructor injection; if a Spring service keeps multiple constructors for compatibility, explicitly annotate the full dependency constructor with `@Autowired`
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
- Prefer reusable search/filter components for listing screens instead of duplicating filter markup and query wiring

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
