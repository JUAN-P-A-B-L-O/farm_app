# FarmApp Backend Documentation

## Overview

FarmApp is a backend API for operational and financial control of a dairy-oriented farm workflow. The system records herd data, feed catalog data, feeding events, milk production events, and user accountability, then derives business indicators such as production totals, feeding cost, revenue, and profit.

The implementation is intentionally compact: it exposes a REST API over a layered Spring Boot application, persists data through Spring Data JPA, and calculates financial metrics from source records instead of storing precomputed balances.

## Purpose

The system exists to support three operational questions:

1. Which animals are currently managed by the farm?
2. How much feed is being consumed and what does it cost?
3. How much milk is being produced and what profit does that production generate?

This makes the API suitable for a simplified farm operations product, an MVP for farm management, or an internal training system centered on dairy production economics.

## High-Level Capabilities

- Manage animals with identification, breed, lifecycle status, and farm ownership marker
- Manage feed types with commercial cost per kilogram
- Record feeding events by animal, date, quantity, and responsible user
- Record production events by animal, date, quantity, and responsible user
- Register system users responsible for operational entries
- Expose production summaries by animal
- Calculate profit by animal from production and feeding aggregates
- Expose dashboard-level totals for production, cost, revenue, profit, and herd size

## Documentation Map

- [Architecture](./architecture.md): application layers, request lifecycle, architectural boundaries
- [Domain](./domain.md): business entities, responsibilities, and logical relationships
- [Database](./database.md): persistence model, table structure, and integrity considerations
- [API](./api.md): endpoint catalog grouped by resource
- [Business Rules](./business-rules.md): validations, constraints, and formulas
- [Data Flow](./data-flow.md): end-to-end operational flows from input to derived metrics
- [Design Decisions](./design-decisions.md): rationale, trade-offs, and architectural simplifications

## Diagram Inventory

- [Architecture Diagram](./diagrams/architecture.mmd)
- [Domain Diagram](./diagrams/domain.mmd)
- [Database Diagram](./diagrams/database.mmd)

## System Characteristics

- Runtime style: synchronous REST API
- Architecture style: layered monolith
- Persistence style: relational model through JPA repositories
- Default runtime database: H2 in-memory
- Supported database dependency in build: PostgreSQL
- Financial model: calculated on read using a fixed milk price

## Scope Notes

The current codebase models farm operations at animal level. It does not model:

- authentication or authorization
- a first-class `Farm` entity
- inventory depletion
- pricing history
- historical cost snapshots
- asynchronous processing

These omissions are relevant because many apparent simplifications in the code are consequences of this deliberately narrow scope.
