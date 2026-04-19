# FarmApp Backend Documentation

## Overview

FarmApp is a Spring Boot backend API for dairy-oriented cattle operations. The current system records farms, animals, feed catalog data, feeding events, milk production events, milk price history, and users. It derives operational and financial indicators such as production totals, feeding cost, milk revenue, sold-animal revenue, acquisition cost impact, profit, and herd size.

The active backend implementation is `backend/farmapp`. It is a layered modular monolith that exposes a JWT-protected REST API, persists data through Spring Data JPA, and calculates financial metrics from source records instead of storing precomputed balances.

## Purpose

The system supports these operational questions:

1. Which farms and animals are accessible to the authenticated user?
2. Which animals are active, sold, dead, or inactive?
3. How much feed is being consumed and what does it cost?
4. How much milk is being produced and what revenue or profit does it generate?
5. Which farm milk price is currently effective?

## High-Level Capabilities

- Authenticate users with JWT through `/auth/login`
- Create users and list or retrieve users when authenticated
- Create and list farms owned by the authenticated user
- Manage animals with tag, breed, birth date, origin, acquisition cost, lifecycle status, sale data, and `farmId`
- Manage farm-scoped feed types with soft deletion through the `active` flag
- Record and query farm-scoped feeding and production events, including optional pagination
- Soft-delete animals, feedings, productions, and feed types
- Register append-only milk price history per farm and resolve the current effective price
- Expose production summaries, dashboard metrics, and analytics series

## Documentation Map

- [Architecture](./architecture.md): application layers, security boundary, request lifecycle, and architectural boundaries
- [Domain](./domain.md): business entities, responsibilities, and logical relationships
- [Database](./database.md): persistence model, table structure, and integrity considerations
- [API](./api.md): endpoint catalog grouped by resource
- [Business Rules](./business-rules.md): validations, lifecycle rules, and financial formulas
- [Data Flow](./data-flow.md): end-to-end operational and reporting flows
- [Design Decisions](./design-decisions.md): rationale, trade-offs, and current architectural simplifications

## Diagram Inventory

- [Architecture Diagram](./diagrams/architecture.mmd)
- [Domain Diagram](./diagrams/domain.mmd)
- [Database Diagram](./diagrams/database.mmd)
- [PlantUML Domain Diagram](./diagrams/domain.puml)
- [PlantUML Class Diagram](./diagrams/class.puml)
- [PlantUML Component Diagram](./diagrams/component.puml)
- [Production Sequence Diagram](./diagrams/sequence-production.puml)
- [Use Case Diagram](./diagrams/use-case-diagram.puml)

## System Characteristics

- Runtime style: synchronous REST API
- Architecture style: layered Spring Boot monolith
- Persistence style: relational model through JPA repositories
- Runtime database configuration: PostgreSQL by default
- Test database: H2 in PostgreSQL compatibility mode
- Security model: stateless JWT authentication
- Financial model: calculated on read from production, feeding, sale, acquisition, feed type, and milk price records

## Scope Notes

The current backend models farm operations at animal and farm level. It does not currently model:

- feed inventory depletion or stock balance
- milk sale contracts or customer billing
- historical feed cost snapshots inside feeding records
- period-aware milk price selection for historical production records
- veterinary records, lactation stages, or herd groups
- asynchronous processing or materialized reporting projections

These omissions are relevant because reporting uses current feed type costs and current farm milk prices when calculating financial views.
