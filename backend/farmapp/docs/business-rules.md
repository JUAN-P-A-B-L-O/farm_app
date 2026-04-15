# Business Rules

## Rule Categories

The application enforces behavior through:

- bean validation on request DTOs
- explicit service-layer validation
- farm access checks
- authenticated user resolution
- repository existence checks
- read-time financial formulas

Basic input shape is validated at the API edge. Cross-entity, access, lifecycle, and financial rules are enforced in services.

## Authentication and Farm Access

- `/auth/login`, OpenAPI paths, Swagger UI paths, and `POST /users` are public
- all other endpoints require JWT authentication
- farm creation requires an authenticated user
- farms are owned by `ownerId`
- farm-scoped operations validate the requested farm when `farmId` is supplied or required
- inaccessible farm-scoped resources are treated as not found
- production, feeding, and milk price creation use the authenticated user as `createdBy` when available
- role `MANAGER` is required for dashboard access, analytics access, and all delete operations
- non-manager authenticated users receive `403 Forbidden` for those restricted operations

## Farm Rules

### Creation

- request must not be null
- `name` must not be blank
- authenticated user is required
- created farm is owned by the authenticated user

### Read

- `GET /farms` returns farms accessible to the authenticated user

## Animal Rules

### Creation

- request must not be null
- `tag` must not be blank
- `breed` must not be blank
- `birthDate` must be present
- `origin` must be `BORN` or `PURCHASED`
- `farmId` must not be blank and must reference an accessible farm
- `tag` must be unique
- status is set automatically to `ACTIVE`
- `acquisitionCost` is required, positive, and limited to two decimals when `origin = PURCHASED`
- `acquisitionCost` is cleared when `origin = BORN`

### Update

- update request must not be null
- updates are partial
- supplied `tag`, `breed`, `status`, `origin`, and `farmId` must not be blank
- `status` must be `ACTIVE`, `SOLD`, `DEAD`, or `INACTIVE`
- changing status to `SOLD` must use the dedicated sell action
- sold animals cannot transition back to another status through generic update
- supplied `farmId` must reference an accessible farm
- supplied `tag` must remain unique
- acquisition cost is normalized according to the updated or existing origin

### Sale

- sell request must not be null
- target animal must exist and be accessible
- only active animals can be sold
- `salePrice` is required, positive, and limited to two decimals
- `saleDate` defaults to the current date when omitted
- selling sets status to `SOLD`

### Deletion

- target animal must exist and be accessible
- deletion is soft and sets status to `INACTIVE`
- deleting an already inactive animal is idempotent
- deletion requires role `MANAGER`

## Feed Type Rules

### Creation

- request must not be null
- `farmId` query parameter must reference an accessible farm
- `name` must not be blank
- `costPerKg` must be positive and limited to two decimals
- `active` is assigned automatically as `true`

### Read

- list and read endpoints return only active feed types
- optional `farmId` scopes results to an accessible farm

### Update

- target feed type must exist, be active, and be accessible when scoped by `farmId`
- update request uses `name` and `costPerKg`
- `costPerKg` must be positive and limited to two decimals

### Deletion

- target feed type must exist and be accessible when scoped by `farmId`
- deletion is soft and sets `active = false`
- deleting an already inactive feed type is idempotent
- deletion requires role `MANAGER`

## Feeding Rules

### Creation

- request must not be null
- `animalId` must not be blank
- `feedTypeId` must not be blank
- `date` must not be null
- `quantity` must be positive and limited to two decimals
- `userId` must be present when no authenticated user is available
- effective `createdBy` must be a valid existing user UUID
- referenced animal must exist, be accessible, and be `ACTIVE`
- referenced feed type must exist and belong to the selected farm when `farmId` is supplied
- farm is resolved from the query parameter when supplied; otherwise from the animal

### Read

- list supports optional `animalId`, `date`, `farmId`, `page`, and `size`
- only active feeding records are returned
- pagination is returned only when both `page` and `size` are provided

### Update

- target feeding must exist, be accessible, and be active
- updates are partial
- supplied `animalId` must reference an active animal
- supplied `feedTypeId` must reference an existing feed type
- supplied `quantity` must be positive and limited to two decimals
- inactive feedings cannot be updated

### Deletion

- deletion is soft and sets status to `INACTIVE`
- deleting an already inactive feeding is idempotent
- deletion requires role `MANAGER`

## Production Rules

### Creation

- request must not be null
- `animalId` must not be blank
- `date` must not be null and cannot be in the future
- `quantity` must be positive and limited to two decimals
- `userId` must be present when no authenticated user is available
- effective `createdBy` must be a valid existing user UUID
- referenced animal must exist, be accessible, and be `ACTIVE`
- farm is resolved from the query parameter when supplied; otherwise from the animal

### Read

- list supports optional `animalId`, `date`, `farmId`, `page`, and `size`
- only active production records are returned
- pagination is returned only when both `page` and `size` are provided

### Update

- target production must exist, be accessible, and be active
- updates are partial
- supplied `animalId` must reference an active animal
- supplied `date` cannot be in the future
- supplied `quantity` must be positive and limited to two decimals
- inactive productions cannot be updated

### Deletion

- deletion is soft and sets status to `INACTIVE`
- deleting an already inactive production is idempotent
- deletion requires role `MANAGER`

## Dashboard and Analytics Authorization

- `GET /dashboard` requires role `MANAGER`
- all `/analytics/**` endpoints require role `MANAGER`

### Summary and Profit

- `animalId` must not be blank
- referenced animal must exist and match `farmId` when scoped
- aggregate totals default to zero when no source data exists
- profit summary includes acquisition cost by default
- `includeAcquisitionCost=false` excludes acquisition cost from the cost side

## Milk Price Rules

### Creation

- `farmId` must not be blank and must reference an accessible existing farm
- request must not be null
- `price` must be positive and limited to two decimals
- `effectiveDate` must not be null
- authenticated user is required as `createdBy`
- creating a milk price always inserts a new history record

### Current Price

- current price is the latest record whose `effectiveDate` is on or before today
- when no such record exists, current price falls back to `2.0`
- fallback responses set `fallbackDefault = true`

### History

- history is returned by farm, ordered by `effectiveDate` descending and then `createdAt` descending

## User Rules

### Creation

- request must not be null
- `name` must not be blank
- `email` must not be blank
- `role` must not be blank
- `password` is optional
- password is hashed before persistence
- when omitted, a random password is generated before hashing

### Read

- identifier must not be blank
- identifier must be a valid UUID
- target user must exist

## Financial Formulas

## Feeding Cost

Feeding cost is derived, not stored.

Formula:

`feeding cost = SUM(active feeding.quantity * feedType.costPerKg)`

Used in:

- production profit by animal
- dashboard totals
- analytics feeding and profit series

## Milk Revenue

Milk revenue is derived from active production quantity and the currently resolved farm milk price.

For a farm-scoped view:

`milk revenue = totalProduction * currentFarmMilkPrice`

For unscoped dashboard and analytics views:

`milk revenue = SUM(production.quantity * currentPrice(production.farmId))`

If a farm has no effective milk price, `currentPrice(farmId)` falls back to `2.0`.

## Sold-Animal Revenue

Sold-animal revenue is derived from animal sale data.

Dashboard:

`soldAnimalRevenue = SUM(animal.salePrice)`

Analytics profit:

- sale revenue is grouped into the period containing `saleDate`
- sale records outside the requested date range are excluded

## Acquisition Cost

When enabled, acquisition cost is included in profit calculations.

- production profit by animal adds the animal acquisition cost to total cost
- dashboard adds acquisition cost for all included animals
- analytics profit applies acquisition cost once to the earliest returned period

## Profit

Per-animal production profit:

`profit = milkRevenue - (feedingCost + acquisitionCostWhenEnabled)`

Dashboard profit:

`totalProfit = (milkRevenue + soldAnimalRevenue) - (feedingCost + acquisitionCostWhenEnabled)`

Analytics profit by period:

`profit = (milkRevenue + soldAnimalRevenueInPeriod) - feedingCostInPeriod`

with acquisition cost optionally added to the earliest returned period.
