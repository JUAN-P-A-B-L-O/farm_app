You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - Analytics and dashboard behavior
  - Monetary fields and calculations
  - API contracts
  - Existing UI/state patterns

- Do NOT assume how values are calculated
- Do NOT guess where currency is applied
- Validate everything against the codebase

====================================
TASK
====================================

Add support for selecting the currency used in calculations (e.g., BRL, USD) and reflect this choice in UI labels across listings, dashboard, and analytics.

====================================
GOAL
====================================

- Allow users to choose the currency context
- Ensure all monetary values respect the selected currency
- Reflect currency in labels and formatting consistently
- Keep calculations accurate and consistent with existing logic

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

- Currency is a presentation and calculation context

- Selection:
  - Can be global (per session) or per-request (choose based on existing patterns)

- Calculations:
  - Must respect existing logic (e.g., revenue, profit)
  - Must not alter stored base values unless pattern already exists

- Formatting:
  - Labels must reflect selected currency (symbol, formatting)
  - Ensure consistency across all screens

- Avoid:
  - Hardcoding currency in UI
  - Mixing multiple currencies without clear context

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Identify where monetary values are calculated and displayed
   - Identify how dashboard and analytics consume these values

2. Backend:

- Support currency context:
  - Accept currency parameter or derive from context

- Ensure calculations:
  - Use consistent base values
  - Apply currency logic consistently

- Keep:
  - Existing endpoints compatible
  - Logic centralized in services

3. Frontend:

- Add currency selector:
  - Accessible from relevant screens (dashboard/analytics/global)

- State:
  - Store selected currency in a consistent place (aligned with current patterns)

- UI:
  - Update labels and formatting dynamically
  - Ensure all monetary values reflect selected currency

- Ensure:
  - All API calls include or respect selected currency
  - No direct API calls outside service layer

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Currency selection behavior
  - How calculations adapt to currency
  - UI/label conventions

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- Currency selection updates values across all screens
- Labels reflect correct currency symbol/format
- Calculations remain consistent and correct
- No regression in analytics/dashboard behavior
- No inconsistencies between backend and frontend

====================================
ANTI-PATTERNS TO AVOID
====================================

- Hardcoding currency in UI
- Duplicating currency logic across components
- Mixing calculation and formatting concerns incorrectly
- Ignoring existing calculation rules

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

Deliver a consistent and flexible currency selection feature that integrates cleanly with existing calculations and UI across the system.