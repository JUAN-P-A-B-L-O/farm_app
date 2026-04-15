# Domain Model

## Domain Scope

The domain is centered on dairy farm operations. The system models the operational chain required to turn farm context, animal lifecycle, feed consumption, milk production, and pricing into financial visibility.

Reference diagram: [domain.mmd](./diagrams/domain.mmd)

## Domain Entities

## Farm

`Farm` is the operating boundary for animals, feed types, feeding records, production records, milk prices, dashboard metrics, and analytics filters.

Responsibilities:

- identify a farm through a generated string ID
- store a farm name
- store the owning user through `ownerId`
- limit farm-specific operations to accessible farms

## Animal

`Animal` is the primary business subject of the system.

Responsibilities:

- identify an animal through a unique `tag`
- store descriptive herd data such as `breed` and `birthDate`
- store `origin` as `BORN` or `PURCHASED`
- store optional `acquisitionCost` for purchased animals
- expose lifecycle status: `ACTIVE`, `SOLD`, `DEAD`, or `INACTIVE`
- store sale information through `salePrice` and `saleDate`
- associate the animal with a `farmId`

Operational role:

- target of production registration
- target of feeding registration
- anchor for profitability queries

Important characteristics:

- new animals default to `ACTIVE`
- selling is a dedicated action, not a generic status update
- deletion is soft and marks the animal `INACTIVE`

## Production

`Production` captures milk output for a specific animal on a specific date.

Responsibilities:

- represent recorded production quantity
- associate production with an animal
- associate production with a farm
- associate production with the responsible user through `createdBy`
- expose an active/inactive status for soft deletion

Business meaning:

- production is a source event, not a derived metric
- revenue is not stored in the record
- revenue and profit are calculated at read time

## Feeding

`Feeding` captures a feed consumption event for a specific animal on a specific date.

Responsibilities:

- store feed quantity consumed
- associate the event with an animal
- associate the event with a feed type
- associate the event with a farm
- associate the event with the responsible user through `createdBy`
- expose an active/inactive status for soft deletion

Business meaning:

- feeding is the operational source of feed cost
- cost is not persisted in the feeding row
- cost is derived later from `quantity * feedType.costPerKg`

## FeedType

`FeedType` is a farm-scoped feed catalog entity.

Responsibilities:

- define the commercial feed name
- define `costPerKg`
- indicate whether the feed type is active
- associate the feed type with a farm

Important characteristics:

- new feed types default to `active = true`
- deletion is soft and sets `active = false`
- list and read operations return only active feed types

## MilkPrice

`MilkPrice` stores append-only milk price history for a farm.

Responsibilities:

- store `price`
- store `effectiveDate`
- store creation metadata through `createdAt` and `createdBy`
- allow current price resolution by farm

Business meaning:

- the current milk price is the latest record whose effective date is on or before today
- if no effective price exists for a farm, reporting falls back to `2.0`
- historical price rows are preserved, but current reporting uses the currently resolved price rather than period-specific historical price lookup

## User

`User` represents both an authenticated account and an accountability reference for operational records.

Responsibilities:

- store `name`, `email`, `role`, and hashed `password`
- authenticate through `/auth/login`
- own farms through `Farm.ownerId`
- provide `createdBy` attribution for feeding, production, and milk price records

## Logical Relationships

The domain relationships are business-driven and mostly represented through scalar IDs:

- one user can own many farms
- one farm can contain many animals, feed types, feeding records, production records, and milk price records
- one animal can have many production records
- one animal can have many feeding records
- one feed type can be referenced by many feeding records
- one user can create many feeding, production, and milk price records

These relationships are enforced primarily in services and repository queries rather than rich JPA relationship annotations.

## ID-Based References

The system consistently uses scalar identifiers for cross-entity references:

- `farmId`
- `animalId`
- `feedTypeId`
- `createdBy`
- `ownerId`

Practical implications:

- services own referential and access validation
- records remain lightweight and independent
- entity graph coupling is avoided
- reporting queries join or aggregate explicitly

## Domain Behavior Summary

- `Farm` is the operating boundary
- `Animal` is the productive asset and lifecycle subject
- `Feeding` is the cost input event
- `Production` is the milk output event
- `FeedType` is the feed cost source
- `MilkPrice` is the milk revenue price source
- `User` is the authentication and accountability source

Profit is not a stored domain entity. It is a derived business view over production, feeding, milk price, acquisition cost, and sale data.
