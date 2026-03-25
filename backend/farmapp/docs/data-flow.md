# Data Flow

## Overview

The system follows a source-record pattern:

- users create operational events
- the application stores those events as canonical records
- financial metrics are computed later from aggregates

This is the key to understanding the backend: feeding and production are the persisted facts; cost, revenue, and profit are derived views.

## Flow 1: User -> Feeding -> Cost

1. A user exists in the `users` table and is identified by UUID.
2. A feed type exists with a defined `costPerKg`.
3. An animal exists and can receive feeding records.
4. The client submits `POST /feedings` with `animalId`, `feedTypeId`, `date`, `quantity`, and `userId`.
5. The controller validates request shape and delegates to `FeedingService`.
6. `FeedingService` validates semantic rules:
   - required identifiers
   - positive quantity
   - valid UUID format for `userId`
7. The service checks existence of:
   - animal
   - feed type
   - user
8. The mapper converts the request into a `FeedingEntity`.
9. The entity is persisted in `feedings` with:
   - `animal_id`
   - `feed_type_id`
   - `date`
   - `quantity`
   - `created_by`
10. No monetary value is stored in the feeding record.
11. Later, when profit or dashboard data is requested, the repository joins `feedings` with `feed_types`.
12. Feeding cost is computed as `SUM(quantity * costPerKg)`.

## Why this flow matters

The design avoids storing duplicated cost fields in feeding transactions. That reduces write-time complexity, but it makes the financial view dependent on feed master data at query time.

## Flow 2: User -> Production -> Revenue

1. A user exists in the `users` table.
2. An animal exists and is eligible for production recording.
3. The client submits `POST /productions` with `animalId`, `date`, `quantity`, and `userId`.
4. The controller delegates to `ProductionService`.
5. `ProductionService` validates:
   - required fields
   - positive quantity
   - date not in the future
   - valid UUID format for `userId`
6. The service checks that:
   - animal exists
   - user exists
7. The mapper creates a `ProductionEntity`.
8. The entity is persisted in `productions` with:
   - `animal_id`
   - `date`
   - `quantity`
   - `created_by`
9. No revenue value is stored in the row.
10. Later, when profit or dashboard data is requested, the system aggregates production quantity.
11. Revenue is derived using the fixed milk price constant `2.0`.

## Why this flow matters

Production remains a pure operational measurement. Commercial interpretation is deferred to the read path, which keeps the write model simple and avoids stale stored revenue.

## Flow 3: System -> Profit

### Per-animal profit

1. The client requests `GET /productions/summary/profit/by-animal?animalId=...`.
2. The controller delegates to `ProductionService`.
3. The service validates that `animalId` is present and that the animal exists.
4. The production repository aggregates total production for the animal.
5. The feeding repository aggregates total feeding cost for the animal by joining feeding quantity with feed type cost.
6. The service applies the fixed milk price `2.0`.
7. The service computes:
   - `revenue = totalProduction * 2.0`
   - `profit = revenue - totalFeedingCost`
8. The API returns the calculated view without persisting it.

### Dashboard profit

1. The client requests `GET /dashboard`.
2. `DashboardService` loads:
   - total production across all animals
   - total feeding cost across all animals
   - total animal count
3. The service computes:
   - `totalRevenue = totalProduction * 2.0`
   - `totalProfit = totalRevenue - totalFeedingCost`
4. The API returns a system-level aggregate snapshot.

## Lifecycle of Business Data

The business lifecycle can be summarized as:

- master data is created first: users, animals, feed types
- operational events are created second: feedings, productions
- financial insights are calculated last: revenue, profit, dashboard totals

This ordering is important because downstream calculations depend entirely on upstream record quality.

## Data Reuse Across Flows

- `users` are reused as accountability references in both feeding and production
- `animals` are reused as the common anchor for all operational events
- `feed_types` are reused as the cost basis for feeding cost
- `productions` and `feedings` become source inputs for profit calculations

## Architectural Consequence

The system is effectively split into:

- a write model for source events
- a read model computed on demand from aggregates

The read model is not materialized. That keeps the design simple, but it makes reporting logic part of the application service layer rather than a separate projection subsystem.
