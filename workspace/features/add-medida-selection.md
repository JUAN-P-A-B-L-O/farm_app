You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - Domain entities and fields (quantities, measurements)
  - API contracts
  - Analytics/dashboard calculations
  - Existing UI/state patterns

- Do NOT assume which fields use units
- Do NOT guess conversion rules or storage format
- Validate everything against the codebase

====================================
TASK
====================================

Add support for selecting and using measurement units (e.g., liters, kilograms, etc.) across relevant features.

====================================
GOAL
====================================

- Allow users to choose measurement units where applicable
- Ensure quantities are consistent across inputs, listings, dashboard, and analytics
- Maintain correctness in calculations
- Keep implementation simple and extensible

====================================
CONSTRAINTS
====================================

- Do NOT refactor unrelated parts of the system
- Do NOT break existing endpoints or contracts
- Keep changes minimal and incremental
- Respect existing architecture (domain/application/infra separation)
- Controllers must remain thin
- Business logic must stay in services
- Frontend must use service layer

====================================
DESIGN GUIDELINES
====================================

- Units are part of domain context for quantities

- Storage:
  - Prefer a consistent base unit internally (if pattern exists)
  - Avoid mixing units without clear conversion rules

- Selection:
  - Can be per-entity (e.g., feeding in kg, production in liters)
  - Or contextual (choose based on existing patterns)

- Conversion:
  - Must be handled consistently
  - Avoid duplicating conversion logic across layers

- UI:
  - Labels must reflect selected unit
  - Inputs must be clear and unambiguous

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Identify all fields representing measurable quantities
   - Identify where these values are used (forms, listings, analytics)

2. Backend:

- Extend domain where needed to support unit information
- Ensure calculations:
  - Use consistent units internally
  - Apply conversion logic where necessary

- Update services:
  - Handle unit selection and validation
  - Ensure compatibility with existing logic

3. Frontend:

- Add unit selection where relevant:
  - Forms (feeding, production, etc.)
  - Possibly global/contextual selector if pattern fits

- Update UI:
  - Show unit labels consistently
  - Ensure values match selected unit

- Ensure:
  - All API calls go through service layer
  - No duplicated conversion logic

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Supported units
  - Storage and conversion strategy
  - UI conventions

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- Unit selection works across relevant features
- Values are consistent between input, storage, and display
- Calculations remain correct
- No regression in existing flows
- UI clearly reflects selected units

====================================
ANTI-PATTERNS TO AVOID
====================================

- Mixing units without conversion rules
- Duplicating conversion logic across layers
- Hardcoding units in UI
- Breaking existing calculations

====================================
OUTPUT REQUIREMENTS
====================================

- Show only necessary changes
- Keep explanation minimal
- Always return a Conventional Commit message
- Provide git commands if possible

====================================
GOAL
====================================

Deliver a consistent and extensible unit handling system that integrates cleanly with domain logic, calculations, and UI.