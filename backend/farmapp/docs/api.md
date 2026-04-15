# API Endpoints

This document catalogs the HTTP interface exposed by the backend. Protected endpoints require `Authorization: Bearer <jwt>` unless noted otherwise.

## Authentication

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/auth/login` | Authenticate by email and password and return a JWT access token |

Public endpoint.

## Farms

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/farms` | List farms accessible to the authenticated user |
| `POST` | `/farms` | Create a farm owned by the authenticated user |

Notes:

- create request requires `name`
- created farms store the authenticated user as `ownerId`

## Animals

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/animals` | Create a new animal record |
| `GET` | `/animals` | List animals, optionally filtered by `farmId` |
| `GET` | `/animals/{id}` | Retrieve a single animal, optionally scoped by `farmId` |
| `PUT` | `/animals/{id}` | Partially update mutable animal fields, optionally scoped by `farmId` |
| `POST` | `/animals/{id}/sell` | Mark an active animal as sold and store sale data |
| `DELETE` | `/animals/{id}` | Soft-delete an animal by marking it `INACTIVE` |

Notes:

- create request requires `tag`, `breed`, `birthDate`, `origin`, and `farmId`
- `origin` is `BORN` or `PURCHASED`
- `acquisitionCost` is required for purchased animals and omitted/cleared for born animals
- `POST /animals/{id}/sell` requires `salePrice`; `saleDate` defaults to the current date when omitted

## Productions

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/productions` | List active production records; optionally filter by `animalId`, `date`, and `farmId` |
| `GET` | `/productions/{id}` | Retrieve an active production record, optionally scoped by `farmId` |
| `GET` | `/productions/summary/by-animal` | Return total active production quantity for an animal |
| `GET` | `/productions/summary/profit/by-animal` | Return production, cost, milk price, revenue, and profit for an animal |
| `POST` | `/productions` | Create a production record |
| `PUT` | `/productions/{id}` | Update mutable fields of an active production record |
| `DELETE` | `/productions/{id}` | Soft-delete a production record by marking it `INACTIVE` |

Supported query parameters:

- list: `animalId`, `date`, `farmId`, `page`, `size`
- summary: `animalId`, optional `farmId`
- profit summary: `animalId`, optional `farmId`, optional `includeAcquisitionCost` defaulting to `true`

Notes:

- when both `page` and `size` are provided, list endpoints return a paginated envelope
- create request requires `animalId`, `date`, `quantity`, and `userId`
- authenticated user context overrides/fills `createdBy`
- update supports `animalId`, `date`, and `quantity`

## Feed Types

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/feed-types` | Create a feed type for a farm |
| `GET` | `/feed-types` | List active feed types, optionally filtered by `farmId` |
| `GET` | `/feed-types/{id}` | Retrieve an active feed type, optionally scoped by `farmId` |
| `PUT` | `/feed-types/{id}` | Update an active feed type |
| `DELETE` | `/feed-types/{id}` | Soft-delete a feed type by setting `active = false` |

Notes:

- create requires `name`, `costPerKg`, and query parameter `farmId`
- update reuses the create request shape
- responses include `id`, `name`, `costPerKg`, and `active`

## Feedings

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/feedings` | Create a feeding record |
| `GET` | `/feedings` | List active feeding records; optionally filter by `animalId`, `date`, and `farmId` |
| `GET` | `/feedings/{id}` | Retrieve an active feeding record, optionally scoped by `farmId` |
| `PUT` | `/feedings/{id}` | Update mutable fields of an active feeding record |
| `DELETE` | `/feedings/{id}` | Soft-delete a feeding record by marking it `INACTIVE` |

Supported query parameters:

- list: `animalId`, `date`, `farmId`, `page`, `size`
- create/update/read/delete: optional `farmId`

Notes:

- create request requires `animalId`, `feedTypeId`, `date`, `quantity`, and `userId`
- update supports `animalId`, `feedTypeId`, `date`, and `quantity`
- authenticated user context overrides/fills `createdBy`
- responses embed animal and feed type summaries but do not expose calculated cost

## Milk Prices

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/milk-prices?farmId=...` | Register a new milk price history record |
| `GET` | `/milk-prices/current?farmId=...` | Return the current effective milk price for a farm |
| `GET` | `/milk-prices?farmId=...` | Return milk price history for a farm, newest first |

Notes:

- create request requires `price` and `effectiveDate`
- price records are append-only
- current price is the latest row whose `effectiveDate` is on or before today
- when no effective price exists, current price falls back to `2.0` with `fallbackDefault = true`

## Users

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/users` | Create an application user |
| `GET` | `/users` | List all users |
| `GET` | `/users/{id}` | Retrieve a user by UUID |

Notes:

- `POST /users` is public
- read endpoints require JWT authentication
- create request requires `name`, `email`, and `role`; `password` is optional
- omitted passwords are replaced with a generated value before hashing
- responses do not expose `password`

## Dashboard

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/dashboard` | Return production, feeding cost, revenue, profit, and animal count metrics |

Supported query parameters:

- optional `farmId`
- optional `includeAcquisitionCost`, defaulting to `true`

## Analytics

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/analytics/production` | Return production quantity over time |
| `GET` | `/analytics/feeding` | Return feeding cost over time |
| `GET` | `/analytics/profit` | Return production, feeding cost, revenue, and profit over time |
| `GET` | `/analytics/production/by-animal` | Return production quantity grouped by animal |

Supported query parameters:

- `startDate`
- `endDate`
- `animalId`
- `farmId`
- `groupBy`, accepted values `day` and `month`, defaulting to `day`
- `includeAcquisitionCost` on `/analytics/profit`, defaulting to `true`

## Error Surface

The API exposes a uniform error payload with:

- `timestamp`
- `status`
- `error`
- `path`

Typical status usage:

- `400` for validation and business-rule violations
- `401` for invalid credentials or missing/invalid authentication
- `404` for resource absence or inaccessible farm-scoped resources
- `409` for conflicts such as duplicate animal tags or updates to inactive records
- `500` for unhandled failures
