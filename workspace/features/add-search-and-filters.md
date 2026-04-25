You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - Listing patterns
  - API filtering capabilities
  - Pagination/search conventions
  - Existing UI components

- Do NOT assume how listings are implemented
- Do NOT guess filtering behavior
- Validate everything against the codebase

====================================
TASK
====================================

Add search and filtering capabilities to all listing screens in the system (e.g., animals, users, feedings, productions, etc.).

====================================
GOAL
====================================

- Allow users to quickly find specific records
- Provide consistent UX across all listings
- Ensure filters and search integrate with existing data fetching
- Create reusable and scalable components

====================================
CONSTRAINTS
====================================

- Do NOT refactor unrelated parts of the system
- Do NOT break existing listing endpoints
- Keep changes minimal and incremental
- Respect existing architecture (domain/application/infra separation)
- Controllers must remain thin
- Business logic must stay in services
- Frontend must use service layer

====================================
DESIGN GUIDELINES
====================================

- Search:
  - Single input field
  - Must support searching by relevant fields (e.g., name, email, label)
  - Backend-driven filtering (avoid filtering only in frontend)

- Filters:
  - Based on each domain (status, type, date, etc.)
  - Must be extensible

- Reusability:
  - Create reusable search + filter components
  - Avoid duplicating logic across screens

- Consistency:
  - All listings must follow the same interaction pattern

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Identify how current listings are implemented
   - Identify existing pagination and query patterns

2. Backend:

- Extend listing endpoints (if needed):
  - Support search parameter (q or similar)
  - Support filters (status, date, etc.)

- Ensure:
  - Filtering is done at query level (not in memory)
  - Consistent parameter naming across endpoints

3. Frontend:

- Create reusable components:
  - Search input component
  - Filter component (configurable)

- Integrate into all listing pages:
  - Animals
  - Users
  - Feedings
  - Productions
  - Others as applicable

- Behavior:
  - Trigger API calls via service layer
  - Respect existing pagination
  - Debounce search if necessary

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Standard search/filter behavior
  - Query parameter conventions
  - Reusable component patterns

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- Search works correctly across all listings
- Filters return expected results
- Backend queries are efficient
- UI is consistent across screens
- No regression in existing listing behavior
- Components are reusable and not tightly coupled

====================================
ANTI-PATTERNS TO AVOID
====================================

- Filtering only in frontend
- Duplicating search/filter logic per page
- Hardcoding filters
- Breaking existing pagination behavior

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

Deliver a consistent, reusable, and scalable search and filtering experience across all listing screens.