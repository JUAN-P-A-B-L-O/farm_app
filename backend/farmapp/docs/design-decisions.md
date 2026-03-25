# Design Decisions

## Context

The codebase reflects a deliberate preference for implementation simplicity, explicit service-layer rules, and derived financial metrics over richer relational modeling. The decisions below are not accidental; they form the operating philosophy of the current system.

## 1. ID-Based References Instead of Object Relations

### Decision

Operational entities store scalar identifiers such as `animalId`, `feedTypeId`, and `createdBy` instead of using JPA associations.

### Why this design is likely used

- keeps the entity model simple
- avoids lazy-loading pitfalls and bidirectional mapping complexity
- makes request payloads straightforward and explicit
- reduces coupling between modules at ORM level
- keeps services in control of referential validation

### Trade-offs

Benefits:

- lower persistence complexity
- easier debugging of entity state
- more predictable serialization behavior

Costs:

- referential integrity is weaker at schema level
- services must perform more manual checks
- joins for reporting become explicit repository concerns
- domain navigation is less expressive inside the codebase

## 2. `userId` as Accountability Field Instead of User Relation

### Decision

Feeding and production records store the responsible user as a scalar identifier in `createdBy`, supplied from request `userId`.

### Why this design makes sense here

The current role of `User` is operational attribution, not identity management. The system only needs to know that a valid user recorded an action; it does not need rich user graph traversal or security-domain behavior.

This makes a scalar reference sufficient for the current scope.

### Trade-offs

Benefits:

- minimal coupling between user records and operational records
- simpler persistence model
- no dependency on user entity loading during normal writes

Costs:

- no database-level navigation from production/feeding to user
- UUID conversion must be handled carefully in services
- future security features would likely require revisiting this model

## 3. Fixed Milk Price

### Decision

Milk price is hard-coded as `2.0` in both production profit and dashboard calculations.

### Why this design is likely used

- establishes a deterministic financial baseline
- keeps the profitability model easy to understand
- avoids introducing pricing catalogs, historical price tables, or market integrations
- allows revenue and profit features to exist without expanding the domain significantly

### Trade-offs

Benefits:

- trivial to reason about
- no pricing synchronization problems
- no additional persistence model required

Costs:

- not realistic for environments with variable milk prices
- cannot support historical revenue accuracy by date
- duplicates the constant across services, which would become a maintenance concern if pricing logic evolves

## 4. No `Farm` Entity

### Decision

The system stores `farmId` directly on `Animal` but does not model a dedicated `Farm` aggregate.

### Why this design is likely used

- the current business workflow is animal-centric
- farm-level master data is not required for the implemented features
- a scalar identifier is enough to support grouping or filtering by farm
- it keeps the scope narrow and avoids introducing another lifecycle to manage

### Trade-offs

Benefits:

- reduced modeling overhead
- simple filtering use case for animals
- fewer dependencies between modules

Costs:

- no farm-level referential integrity
- no place to model farm attributes, ownership, or policy
- future farm-level reporting would require additional domain work

## 5. Calculated Values Instead of Persisted Financial Snapshots

### Decision

Revenue, feeding cost, and profit are calculated at read time rather than persisted as stored values.

### Why this design is strong for the current scope

- source-of-truth data stays limited to operational events
- avoids synchronization bugs between raw records and stored aggregates
- makes dashboards and profit queries immediately reflect the latest inputs

### Trade-offs

Benefits:

- less risk of stale derived data
- simpler write path
- no batch recalculation process required

Costs:

- every reporting request depends on live aggregation
- performance may become a concern as data volume grows
- historical recalculation behavior can become problematic if pricing rules change

## 6. Service-Layer Integrity Over Schema-Layer Integrity

### Decision

The application validates relational consistency in services instead of relying on JPA associations and foreign-key modeling.

### Why this design works in this codebase

It aligns with the overall preference for explicit control and small-system pragmatism. The services already own business validation, so existence checks fit naturally into the same layer.

### Trade-offs

Benefits:

- integrity rules are visible in one place
- application behavior remains explicit
- simpler entity classes

Costs:

- integrity protection is bypassable by direct database writes
- cross-service consistency must remain carefully maintained
- schema evolution toward stronger guarantees will require additional work

## 7. Layered Monolith Instead of Distributed Components

### Decision

All functionality resides inside one Spring Boot application.

### Why this is appropriate

- domain scope is still small
- module boundaries are clear enough within a single deployable unit
- the operational burden of microservices would not be justified

### Trade-offs

Benefits:

- lower operational overhead
- simple deployment model
- easy local development and testing

Costs:

- scaling is coarse-grained
- reporting and transactional concerns share the same runtime boundary
- future domain expansion may eventually require clearer bounded contexts

## Decision Summary

The system favors:

- explicitness over ORM richness
- derived reporting over stored projections
- scope control over completeness

That makes the current implementation coherent for an early-stage operational backend. The main trade-off is that long-term scale, historical financial accuracy, and relational integrity would require deliberate architectural strengthening.
