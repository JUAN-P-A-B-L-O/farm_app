# Data Flow

## Overview

The system follows a source-record pattern:

- users authenticate and operate inside farm context
- master data and operational events are persisted as canonical records
- financial metrics are computed later from aggregates

Feeding, production, milk prices, animal acquisition cost, and animal sale data are the source inputs for financial reporting. Cost, revenue, and profit are derived views.

## Flow 1: Authentication -> Farm Context

1. Client sends `POST /auth/login` with email and password.
2. `AuthService` verifies the user and password hash.
3. `TokenService` generates a JWT.
4. Client sends `Authorization: Bearer <token>` on protected requests.
5. `JwtAuthenticationFilter` validates the token and populates the security context.
6. Services read the authenticated user through `AuthenticationContextService`.
7. Farm operations use the authenticated user to create or list accessible farms.

## Flow 2: Farm -> Animal Lifecycle

1. A user creates or selects a farm.
2. The client submits `POST /animals` with `tag`, `breed`, `birthDate`, `origin`, optional `acquisitionCost`, and `farmId`.
3. `AnimalService` validates the accessible farm, tag uniqueness, origin, and acquisition cost rules.
4. The animal is persisted with status `ACTIVE`.
5. Later lifecycle actions update the same animal:
   - generic update can change mutable fields and allowed statuses except new sale transitions
   - `POST /animals/{id}/sell` sets status `SOLD` and stores `salePrice` and `saleDate`
   - `DELETE /animals/{id}` sets status `INACTIVE`

## Flow 3: User -> Feeding -> Cost

1. A farm, active animal, active feed type, and user exist.
2. The client submits `POST /feedings` with `animalId`, `feedTypeId`, `date`, `quantity`, and `userId`, optionally scoped with `farmId`.
3. `FeedingService` resolves `createdBy` from the authenticated user when available.
4. The service validates required fields, quantity scale, user UUID, active animal, feed type, and farm access.
5. The mapper creates a `FeedingEntity`.
6. The entity is persisted with `farmId`, `createdBy`, and status `ACTIVE`.
7. No monetary value is stored in the feeding record.
8. Later, profit and dashboard reads join active feedings with feed types.
9. Feeding cost is computed as `SUM(quantity * costPerKg)`.

## Why this flow matters

The design avoids storing duplicated cost fields in feeding transactions. That reduces write-time complexity, but financial views depend on the current feed type cost at query time.

## Flow 4: User -> Production -> Milk Revenue

1. A farm, active animal, and user exist.
2. The client submits `POST /productions` with `animalId`, `date`, `quantity`, and `userId`, optionally scoped with `farmId`.
3. `ProductionService` resolves `createdBy` from the authenticated user when available.
4. The service validates required fields, quantity scale, non-future date, user UUID, active animal, and farm access.
5. The mapper creates a `ProductionEntity`.
6. The entity is persisted with `farmId`, `createdBy`, and status `ACTIVE`.
7. No revenue value is stored in the production row.
8. Later, reporting aggregates active production quantity.
9. Milk revenue uses the current milk price for the production farm, falling back to `2.0` when no price is effective.

## Flow 5: Farm -> Milk Price History

1. The client submits `POST /milk-prices?farmId=...` with `price` and `effectiveDate`.
2. `MilkPriceService` validates farm access, price, effective date, and authenticated user context.
3. A new `MilkPriceEntity` is inserted with `createdAt` and `createdBy`.
4. Existing milk price rows are not overwritten.
5. `GET /milk-prices/current?farmId=...` resolves the latest row effective on or before today.
6. If no current row exists, the response uses fallback price `2.0` and sets `fallbackDefault = true`.

## Flow 6: System -> Profit

### Per-animal production profit

1. Client requests `GET /productions/summary/profit/by-animal?animalId=...`.
2. `ProductionService` validates `animalId`, optional `farmId`, and animal existence.
3. Repository queries aggregate active production quantity for the animal.
4. Repository queries aggregate active feeding cost for the animal.
5. The service resolves the current milk price for the animal farm.
6. If enabled, acquisition cost is added to total cost.
7. The service computes:
   - `revenue = totalProduction * currentMilkPrice`
   - `profit = revenue - (feedingCost + acquisitionCostWhenEnabled)`
8. The API returns the calculated view without persisting it.

### Dashboard profit

1. Client requests `GET /dashboard`, optionally with `farmId` and `includeAcquisitionCost`.
2. `DashboardService` validates farm access when scoped.
3. The service loads:
   - active production total
   - active feeding cost
   - acquisition cost when enabled
   - animal sale revenue
   - animal count
4. Milk revenue uses current farm milk price for farm-scoped views or each production record's farm price for unscoped views.
5. The service computes:
   - `totalRevenue = milkRevenue + soldAnimalRevenue`
   - `totalProfit = totalRevenue - (feedingCost + acquisitionCostWhenEnabled)`

### Analytics profit

1. Client requests `GET /analytics/profit`.
2. `AnalyticsService` validates date range, animal filter, farm filter, and grouping.
3. Active production and feeding records are filtered in memory by date and animal.
4. Production, milk revenue, sale revenue, and feeding cost are grouped by day or month.
5. Acquisition cost is applied once to the earliest returned period when enabled.
6. The API returns period-level production, feeding cost, revenue, and profit.

## Lifecycle of Business Data

The business lifecycle can be summarized as:

- users authenticate
- farms are created or selected
- master data is created: animals and feed types
- optional price history is registered: milk prices
- operational events are created: feedings and productions
- lifecycle events occur: animal sale and soft deletion
- financial insights are calculated: summaries, dashboard, and analytics

## Data Reuse Across Flows

- `users` are reused for authentication, farm ownership, and operational attribution
- `farms` scope animals, feed types, feeding, production, milk prices, dashboard, and analytics
- `animals` anchor feeding, production, acquisition cost, and sale revenue
- `feed_types` provide the live cost basis for feeding calculations
- `milk_prices` provide the current price basis for milk revenue calculations
- `productions` and `feedings` are source inputs for profit calculations

## Architectural Consequence

The system is split into:

- a write model for source records
- a read model computed on demand from aggregates

The read model is not materialized. This keeps writes simple and avoids stale derived data, but reporting logic remains in the application service layer.
