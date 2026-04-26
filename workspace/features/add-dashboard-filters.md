You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - Dashboard and analytics behavior
  - Available domain filters (animals, dates, farms, etc.)
  - API contracts and query patterns
  - Existing UI/state management patterns

- Do NOT assume available fields or filters
- Do NOT guess how dashboard data is aggregated
- Validate everything against the codebase

====================================
TASK
====================================

Add filtering capabilities to the dashboard page.

Filters must include:
- Time period (date range)
- Animal(s)

Also identify and include other relevant filters based on the domain.

====================================
GOAL
====================================

- Allow users to refine dashboard data dynamically
- Ensure filters are consistent with analytics and listings
- Keep UX simple and scalable
- Maintain alignment with backend data aggregation

====================================
CONSTRAINTS
====================================

- Do NOT refactor unrelated parts of the system
- Do NOT break existing dashboard endpoints
- Keep changes minimal and incremental
- Respect existing architecture (domain/application/infra separation)
- Controllers must remain thin
- Business logic must stay in services
- Frontend must use service layer

====================================
DESIGN GUIDELINES
====================================

- Filters must be backend-driven (no frontend-only filtering)

- Time filter:
  - Support date range
  - Default behavior must be defined (e.g., all time or recent period)

- Animal filter:
  - Single or multi-select (based on existing patterns)

- Additional filters:
  - Identify relevant ones from domain (e.g., farm, status, type)
  - Keep extensible

- Consistency:
  - Align with filters used in analytics and listings

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Inspect how dashboard data is currently fetched
   - Identify existing filter/query patterns

2. Backend:

- Extend dashboard endpoint(s):
  - Accept filter parameters (date range, animalId(s), others)

- Ensure:
  - Filtering is applied at query/service level
  - Aggregations respect filters
  - Parameter naming is consistent with other endpoints

3. Frontend:

- Add filter UI:
  - Date range picker
  - Animal selector
  - Additional filters as identified

- State:
  - Store filters in a consistent way
  - Trigger data reload on change

- Reusability:
  - Reuse existing filter/search components if available
  - Keep components configurable for future reuse

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Dashboard filtering behavior
  - Supported parameters
  - Default filter logic

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- Dashboard updates correctly when filters change
- Data reflects selected period and animals
- Filters integrate correctly with backend aggregation
- No regression in existing dashboard behavior
- Performance remains acceptable

====================================
ANTI-PATTERNS TO AVOID
====================================

- Filtering only in frontend
- Hardcoding filter values
- Breaking existing dashboard logic
- Duplicating filter logic unnecessarily

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

Deliver a flexible and consistent filtering system for the dashboard, aligned with domain behavior and scalable for future extensions.