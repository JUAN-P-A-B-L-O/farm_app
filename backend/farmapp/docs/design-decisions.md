# Design Decisions

## Context

The codebase favors implementation simplicity, explicit service-layer rules, JWT-secured REST endpoints, farm-scoped operations, and derived financial metrics over richer ORM modeling or materialized reporting projections.

## 1. Layered Monolith

### Decision

All backend functionality resides inside one Spring Boot application with module-oriented packages.

### Rationale

- domain scope is still compact
- local development and testing remain straightforward
- controller, service, repository, mapper, entity, and DTO responsibilities are explicit
- distributed-system complexity is not needed for the current feature set

### Trade-offs

Benefits:

- low operational overhead
- simple deployment model
- easy cross-module service orchestration

Costs:

- scaling is coarse-grained
- reporting and transactional concerns share one runtime
- future domain growth may require clearer bounded contexts

## 2. ID-Based References Instead of Object Relations

### Decision

Operational entities store scalar identifiers such as `farmId`, `animalId`, `feedTypeId`, `createdBy`, and `ownerId` instead of rich JPA associations.

### Rationale

- keeps the entity model simple
- avoids lazy-loading and bidirectional mapping complexity
- makes request payloads explicit
- keeps services in control of access and referential validation

### Trade-offs

Benefits:

- lower persistence complexity
- predictable serialization behavior
- easy debugging of entity state

Costs:

- weaker schema-level referential integrity
- more manual checks in services
- explicit joins or lookups are needed for reporting

## 3. Service-Layer Farm Access

### Decision

Farm ownership and access are enforced in services through `FarmAccessService` instead of database relationships or controller logic.

### Rationale

- farm context applies across multiple modules
- controllers stay thin
- access failures can be treated consistently as not found
- existing scalar-ID persistence style is preserved

### Trade-offs

Benefits:

- consistent farm validation path
- minimal controller duplication
- compatible with current entity design

Costs:

- direct database writes can bypass access expectations
- every new farm-scoped service must explicitly use the access service

## 4. JWT Authentication With Public User Creation

### Decision

The API uses stateless JWT authentication. `POST /users` remains public while most endpoints are protected.

### Rationale

- keeps backend security explicit and testable
- supports authenticated farm ownership
- allows initial user creation without an existing session
- avoids server-side session storage

### Trade-offs

Benefits:

- simple protected endpoint model
- works well for separate frontend/backend applications
- authenticated user context can fill `createdBy`

Costs:

- frontend must consistently attach tokens
- public user creation may need stricter policy in a production deployment

## 5. Append-Only Milk Price History

### Decision

Milk prices are stored as append-only farm-scoped records with `price`, `effectiveDate`, `createdAt`, and `createdBy`.

### Rationale

- preserves prior price entries
- supports current price resolution without overwriting history
- keeps price management separate from production writes

### Trade-offs

Benefits:

- simple audit trail for price changes
- current price lookup is deterministic
- future historical analytics can build on existing records

Costs:

- current reporting still uses the latest effective farm price by default
- production rows do not snapshot milk price at write time

## 6. Calculated Values Instead of Persisted Financial Snapshots

### Decision

Revenue, feeding cost, and profit are calculated at read time.

### Rationale

- source-of-truth data stays limited to operational events and price records
- dashboards and analytics immediately reflect changed inputs
- no batch recalculation process is required

### Trade-offs

Benefits:

- less risk of stale derived data
- simpler write path
- fewer persisted financial fields

Costs:

- every reporting request depends on live aggregation
- historical results can change when feed type costs or current milk prices change
- performance may require projections if data volume grows

## 7. Soft Deletes for Operational Records

### Decision

Animal, feeding, production, and feed type delete endpoints do not hard-delete rows.

### Current behavior

- animals are marked `INACTIVE`
- feedings are marked `INACTIVE`
- productions are marked `INACTIVE`
- feed types are marked `active = false`

### Rationale

- preserves historical operational records
- avoids accidental loss of data used for auditing
- keeps delete operations reversible at database level if needed

### Trade-offs

Benefits:

- safer operational history
- read paths can consistently filter active records

Costs:

- services and repositories must consistently filter inactive rows
- aggregate queries must account for status/active flags

## Decision Summary

The system currently favors:

- explicit service rules over ORM-rich modeling
- JWT-secured REST over session-based state
- farm-scoped source records over global-only operations
- append-only price history over mutable price settings
- derived reporting over persisted projections
- soft deletion over physical deletion

These choices are coherent for the current backend. Long-term scale, stricter relational integrity, and historically exact financial reporting would require deliberate architectural strengthening.
