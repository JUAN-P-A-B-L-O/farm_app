# Database Model

## Persistence Strategy

The backend uses Spring Data JPA with a relational schema generated from entity mappings. By default, the application runs against an H2 in-memory database with `ddl-auto=update`, which is practical for local execution and tests. The build also includes PostgreSQL support, indicating the code is intended to remain compatible with a production-grade relational database.

Reference diagram: [database.mmd](./diagrams/database.mmd)

## Table Overview

## `animals`

Purpose:

- stores the farm-managed animal catalog

Fields:

- `id` `string` primary key
- `tag` `string` unique, not null
- `breed` `string` not null
- `birth_date` `date` not null
- `status` `string` not null
- `farm_id` `string` not null

Notes:

- `id` is assigned in application code through the mapper
- `tag` is the strongest operational identifier because it is unique at database level

## `productions`

Purpose:

- stores milk production records

Fields:

- `id` `string` primary key
- `animal_id` `string` not null
- `date` `date` not null
- `quantity` `double` not null
- `created_by` `string` not null

Notes:

- `id` uses JPA UUID generation but is stored as a string field
- `created_by` contains the user UUID string supplied by the API layer
- revenue is not stored

## `feed_types`

Purpose:

- stores the feed catalog and unit price basis

Fields:

- `id` `string` primary key
- `name` `string` not null
- `cost_per_kg` `double` not null
- `active` `boolean` not null

Notes:

- unit cost is the authoritative source for feeding cost aggregation
- active state exists, but the current code does not filter by it in feeding creation or cost calculation

## `feedings`

Purpose:

- stores feed consumption records

Fields:

- `id` `string` primary key
- `animal_id` `string` not null
- `feed_type_id` `string` not null
- `date` `date` not null
- `quantity` `double` not null
- `created_by` `string` not null

Notes:

- feeding cost is not persisted in the row
- cost is derived through a join with `feed_types`

## `users`

Purpose:

- stores operational users

Fields:

- `id` `uuid` primary key
- `name` `string` not null
- `email` `string` not null
- `role` `string` not null

Notes:

- the user entity is used as a validation and accountability reference
- no authentication credentials or security metadata are stored

## Constraints

Implemented constraints in the mapped model:

- `animals.tag` is unique
- all entity fields marked `nullable = false` are required at persistence level
- all primary keys are mandatory and generated or assigned before insert

Application-enforced constraints:

- feeding references must point to existing animal, feed type, and user
- production references must point to existing animal and user
- user identifiers supplied in feeding and production requests must be valid UUIDs

## Lack of Foreign Keys

The current entity model does not declare explicit foreign-key relationships through JPA mappings. The schema therefore behaves as an ID-based relational model rather than an association-driven one.

Practical consequences:

- referential integrity is enforced primarily in services, not in the schema definition
- direct database writes outside the application could create orphaned references
- joins for reporting or aggregation must be written manually
- ORM behavior remains predictable because the application avoids lazy-loading and relationship state management

This is a reasonable trade-off for a small operational system, but it would need re-evaluation in a larger multi-writer environment.

## Aggregate Query Model

The schema supports two key reporting patterns:

### Production aggregation

- total production by animal
- total production for the whole system

### Feeding cost aggregation

- total feeding cost by animal
- total feeding cost for the whole system

Feeding cost aggregation depends on joining:

- `feedings.feed_type_id`
- `feed_types.id`

and applying:

- `SUM(feedings.quantity * feed_types.cost_per_kg)`

## Data Integrity Implications

The model is optimized for simplicity over strict relational enforcement.

Strengths:

- easy to understand
- low ORM complexity
- simple inserts and reads

Risks:

- no schema-level protection against orphaned references
- financial calculations depend on current feed type price, not a historical snapshot
- user references are stored as strings in operational tables while `users.id` is a UUID, which increases reliance on application-level conversion discipline
