# Domain Model

## Domain Scope

The domain is centered on simplified dairy farm operations. The system models the operational chain required to turn feed consumption into financial visibility:

- animals are managed as productive assets
- feeding events represent cost-driving inputs
- production events represent revenue-driving outputs
- feed types provide the price basis for feed cost
- users provide accountability for recorded operations

Reference diagram: [domain.mmd](./diagrams/domain.mmd)

## Domain Entities

## Animal

`Animal` is the primary business subject of the system. Nearly every operational record is anchored to an animal.

Responsibilities:

- identify an animal through a unique tag
- store descriptive herd data such as breed and birth date
- expose lifecycle status
- associate the animal with a `farmId`

Operational role:

- target of production registration
- target of feeding registration
- anchor for profitability queries

Important characteristics:

- created with default status `ACTIVE`
- identified internally by a generated string ID
- carries `farmId` as a scalar value rather than a relation to a farm aggregate

## Production

`Production` captures milk output for a specific animal on a specific date.

Responsibilities:

- represent recorded production quantity
- associate production with an animal
- associate production with the responsible user through `createdBy`

Business meaning:

- production is a source event, not a derived metric
- revenue is not stored in the record
- profit depends on aggregating production with feeding cost later

This design keeps production data atomic and auditable while leaving commercial calculations flexible.

## Feeding

`Feeding` captures a feed consumption event for a specific animal on a specific date.

Responsibilities:

- store feed quantity consumed
- associate the event with an animal
- associate the event with a feed type
- associate the event with the responsible user through `createdBy`

Business meaning:

- feeding is the operational source of cost
- cost is not persisted inside the feeding record
- cost is derived later from `quantity * feedType.costPerKg`

This keeps feed cost dependent on master data instead of duplicating price fields into every transaction.

## FeedType

`FeedType` is a catalog entity representing a type of feed and its unit cost.

Responsibilities:

- define the commercial name of the feed
- define `costPerKg`
- indicate whether the feed type is active

Business meaning:

- feed type is the pricing basis for feeding cost aggregation
- feedings depend on feed types logically, not through JPA associations

Important characteristic:

- new feed types are created with `active = true`

## User

`User` represents the actor responsible for creating feeding and production records.

Responsibilities:

- provide attribution and operational accountability
- validate that input records were created by a known system user

Business meaning:

- the current model uses users for ownership of actions, not access control
- there is no authentication subsystem attached to this entity in the current codebase

## Logical Relationships

The domain relationships are business-driven rather than ORM-driven:

- one animal can have many production records
- one animal can have many feeding records
- one feed type can be referenced by many feeding records
- one user can create many feeding records
- one user can create many production records

These relationships exist logically in the domain and operationally in the service layer, but they are not modeled with JPA relationship annotations.

## Why ID-Based References Matter

The system consistently uses scalar IDs for cross-entity references:

- `animalId`
- `feedTypeId`
- `createdBy`

This has concrete domain implications:

- the service layer owns referential validation
- records remain lightweight and independent
- the persistence model avoids object graph coupling
- repository queries must join manually when aggregation needs cross-entity data

This approach is pragmatic for a small system, but it shifts integrity discipline from ORM configuration into application logic.

## Domain Boundaries and Omissions

The current domain intentionally excludes several concepts that would exist in a more mature farm management platform:

- `Farm` as a first-class aggregate
- inventory or stock balance
- milk sale contracts or customer billing
- pricing history
- herd groups, lactation stage, or veterinary records

This means the domain is optimized for operational recording and simplified financial visibility, not for full agricultural ERP coverage.

## Domain Behavior Summary

The domain can be summarized as:

- `Animal` is the productive asset
- `Feeding` is the cost input event
- `Production` is the revenue input event
- `FeedType` is the price source for cost calculation
- `User` is the accountability source for operational entries

Profit is therefore not a domain entity. It is a derived business view over production and feeding aggregates.
