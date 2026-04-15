# Database Model

## Persistence Strategy

The backend uses Spring Data JPA with a relational schema generated from entity mappings. Runtime configuration targets PostgreSQL:

- datasource URL default: `jdbc:postgresql://localhost:5432/farmdb`
- dialect: PostgreSQL
- schema generation: `spring.jpa.hibernate.ddl-auto=create`

Tests use H2 in PostgreSQL compatibility mode.

Reference diagram: [database.mmd](./diagrams/database.mmd)

## Table Overview

## `users`

Purpose:

- stores application users for authentication and accountability

Fields:

- `id` `uuid` primary key
- `name` `string` not null
- `email` `string` not null
- `role` `string` not null
- `password` `string` not null

Notes:

- passwords are BCrypt hashes
- responses do not expose `password`
- a default admin is created on startup when the user table is empty

## `farms`

Purpose:

- stores farms accessible to users

Fields:

- `id` `string` primary key
- `name` `string` not null
- `owner_id` `uuid` not null

Notes:

- `owner_id` references the owning user logically
- a default farm is created with the default admin when the user table is empty

## `animals`

Purpose:

- stores the farm-managed animal catalog

Fields:

- `id` `string` primary key
- `tag` `string` unique, not null
- `breed` `string` not null
- `birth_date` `date` not null
- `status` `string` not null
- `origin` `string` not null
- `acquisition_cost` `double` nullable
- `sale_price` `double` nullable
- `sale_date` `date` nullable
- `farm_id` `string` not null

Notes:

- `tag` is the strongest operational identifier because it is unique at database level
- lifecycle deletion is represented by status `INACTIVE`
- sold animals keep `salePrice` and `saleDate`

## `feed_types`

Purpose:

- stores the feed catalog and unit price basis

Fields:

- `id` `string` primary key
- `name` `string` not null
- `cost_per_kg` `double` not null
- `active` `boolean` not null
- `farm_id` `string` nullable

Notes:

- create operations assign a farm ID
- list/read operations return active feed types
- soft deletion sets `active = false`

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
- `farm_id` `string` nullable
- `status` `string` not null

Notes:

- feeding cost is not persisted in the row
- cost is derived through a join with `feed_types`
- soft deletion sets status to `INACTIVE`

## `productions`

Purpose:

- stores milk production records

Fields:

- `id` `string` primary key
- `animal_id` `string` not null
- `date` `date` not null
- `quantity` `double` not null
- `created_by` `string` not null
- `farm_id` `string` nullable
- `status` `string` not null

Notes:

- revenue is not stored
- soft deletion sets status to `INACTIVE`

## `milk_prices`

Purpose:

- stores append-only milk price history by farm

Fields:

- `id` `string` primary key
- `farm_id` `string` not null
- `price` `double` not null
- `effective_date` `date` not null
- `created_at` `timestamp` not null
- `created_by` `string` not null

Notes:

- current price lookup selects the latest row effective on or before today
- there is no update or delete endpoint for milk price records

## Constraints

Implemented constraints in the mapped model:

- `animals.tag` is unique
- fields marked `nullable = false` are required at persistence level
- UUID/string primary keys are generated or assigned before insert depending on entity

Application-enforced constraints:

- farm access and ownership
- animal, feed type, farm, and user existence checks
- active-status checks for feeding and production operations
- UUID format checks for user identifiers
- lifecycle transitions and sell behavior
- decimal scale limits for financial and quantity inputs

## Lack of JPA Relationships

The current entity model does not declare rich relationship mappings such as `@ManyToOne`. The schema behaves as an ID-based relational model.

Practical consequences:

- referential integrity is enforced primarily in services, not entity associations
- direct database writes outside the application could create orphaned references
- joins for reporting and aggregation are explicit
- ORM behavior remains predictable because lazy-loading and bidirectional relationship state are avoided

## Aggregate Query Model

The schema supports these reporting patterns:

- total active production by animal
- total active production by farm
- total active production system-wide
- total active feeding cost by animal
- total active feeding cost by farm
- total active feeding cost system-wide
- milk price lookup by farm and effective date

Feeding cost aggregation depends on joining:

- `feedings.feed_type_id`
- `feed_types.id`

and applying:

`SUM(feedings.quantity * feed_types.cost_per_kg)`

## Data Integrity Implications

Strengths:

- simple persistence model
- predictable inserts and reads
- low ORM coupling
- service-layer rules are explicit

Risks:

- no schema-level protection against most orphaned references
- financial calculations depend on current feed type prices, not historical feed price snapshots
- current reporting uses the latest effective farm milk price rather than price-by-production-date logic
- operational tables store `created_by` as strings while `users.id` is a UUID, increasing reliance on application-level conversion discipline
