# Business Rules

## Rule Categories

The application enforces business behavior through a combination of:

- bean validation on request DTOs
- explicit service-layer validation
- existence checks across repositories
- read-time financial formulas

This split is intentional. Basic input shape is validated at the API edge, while cross-entity and semantic rules are enforced in services.

## Animal Rules

### Creation

- request must not be null
- `tag` must not be blank
- `breed` must not be blank
- `birthDate` must be present
- `farmId` must not be blank
- `status` is not provided by the caller; it is set automatically to `ACTIVE`

### Update

- update request must not be null
- updates are partial
- if `tag` is supplied, it must not be blank
- if `breed` is supplied, it must not be blank
- if `status` is supplied, it must not be blank
- if `farmId` is supplied, it must not be blank
- target animal must exist

### Deletion

- target animal must exist before deletion

## Feed Type Rules

### Creation

- request must not be null
- `name` must not be blank
- `costPerKg` must be greater than zero
- `active` is assigned automatically as `true`

### Read

- identifier must not be blank
- target feed type must exist

## Feeding Rules

### Creation

- request must not be null
- `animalId` must not be blank
- `feedTypeId` must not be blank
- `date` must not be null
- `quantity` must be greater than zero
- `userId` must not be blank
- `userId` must be a valid UUID string
- referenced animal must exist
- referenced feed type must exist
- referenced user must exist

### Read

- identifier must not be blank
- target feeding record must exist

## Production Rules

### Creation

- request must not be null
- `animalId` must not be blank
- `date` must not be null
- `date` cannot be in the future
- `quantity` must be greater than zero
- `userId` must not be blank
- `userId` must be a valid UUID string
- referenced animal must exist
- referenced user must exist

### Update

- request must not be null
- target production record must exist
- update is partial
- if `date` is supplied, it cannot be in the future
- if `quantity` is supplied, it must be greater than zero

### Summary and Profit

- `animalId` must not be blank
- referenced animal must exist
- aggregate totals default to zero when no source data exists

## User Rules

### Creation

- request must not be null
- `name` must not be blank
- `email` must not be blank
- `role` must not be blank

### Read

- identifier must not be blank
- identifier must be a valid UUID
- target user must exist

## Persistence and Structural Constraints

- animal `tag` is unique
- all persisted fields marked as non-null in entities are mandatory at database level
- feeding and production references are validated by the application, not enforced by entity relationships

## Financial Formulas

## Feeding Cost

Feeding cost is derived, not stored.

Formula:

`feeding cost = SUM(feeding.quantity * feedType.costPerKg)`

Used in:

- profit by animal
- dashboard totals

Implication:

- the system treats feed type price as the live pricing source for every feeding record tied to that feed type

## Revenue

Revenue is derived from total production and a fixed milk price.

Formula:

`revenue = totalProduction * 2.0`

Where:

- `2.0` is the fixed milk price constant used in the codebase

## Profit

### Per-animal profit

`profit = revenue - totalFeedingCost`

Expanded:

`profit = (totalProduction * 2.0) - totalFeedingCost`

### Dashboard profit

`totalProfit = totalRevenue - totalFeedingCost`

Expanded:

`totalProfit = (sumAllProduction * 2.0) - sumAllFeedingCost`

## Rule Implications

These rules create a clear operating model:

- production drives revenue
- feeding drives cost
- profit is a read-time projection over operational records

The model is simple and deterministic, but it also means:

- historical price changes would affect derived totals unless price snapshotting is introduced later
- no inventory or wastage rules currently influence cost
