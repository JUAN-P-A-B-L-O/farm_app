# API Endpoints

This document catalogs the HTTP interface exposed by the backend. It focuses on resource purpose and endpoint behavior rather than payload examples.

## Animals

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/animals` | Create a new animal record |
| `GET` | `/animals` | List animals; optionally filter by `farmId` |
| `GET` | `/animals/{id}` | Retrieve a single animal by identifier |
| `PUT` | `/animals/{id}` | Update mutable animal fields |
| `DELETE` | `/animals/{id}` | Remove an animal record |

Notes:

- Animal creation sets status implicitly to `ACTIVE`
- `GET /animals` supports `farmId` as an optional query parameter

## Productions

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/productions` | List production records; optionally filter by `animalId`, `date`, or both |
| `GET` | `/productions/{id}` | Retrieve a single production record |
| `GET` | `/productions/summary/by-animal` | Return total production quantity for a specific animal |
| `GET` | `/productions/summary/profit/by-animal` | Return production, feeding cost, revenue, and profit for a specific animal |
| `POST` | `/productions` | Create a production record |
| `PUT` | `/productions/{id}` | Update production date and/or quantity |

Notes:

- summary endpoints require `animalId`
- production creation requires a valid `userId`, but the response does not expose `createdBy`

## Feed Types

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/feed-types` | Create a feed catalog entry |
| `GET` | `/feed-types` | List all feed types |
| `GET` | `/feed-types/{id}` | Retrieve a single feed type |

Notes:

- new feed types are created as active by default
- there are no update, deactivate, or delete endpoints in the current API

## Feedings

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/feedings` | Create a feeding record |
| `GET` | `/feedings` | List all feeding records |
| `GET` | `/feedings/{id}` | Retrieve a single feeding record |

Notes:

- feeding creation requires existing animal, feed type, and user references
- the response returns quantity and identifiers but not calculated cost

## Users

| Method | Path | Purpose |
|---|---|---|
| `POST` | `/users` | Create an operational user |
| `GET` | `/users` | List all users |
| `GET` | `/users/{id}` | Retrieve a user by UUID |

Notes:

- users act as accountable actors for feeding and production entries
- the API does not expose authentication or authorization features

## Dashboard

| Method | Path | Purpose |
|---|---|---|
| `GET` | `/dashboard` | Return system-wide production, feeding cost, revenue, profit, and animal count |

## Error Surface

The API exposes a uniform error payload with:

- `timestamp`
- `status`
- `error`
- `path`

Typical status usage:

- `400` for validation and business-rule violations
- `404` for resource absence
- `500` for unhandled failures
