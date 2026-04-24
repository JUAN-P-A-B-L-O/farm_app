You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - Listing patterns
  - Pagination conventions
  - API contracts
  - Existing UI patterns

- Do NOT assume how listings are implemented
- Do NOT guess pagination behavior
- Validate everything against the codebase

====================================
TASK
====================================

Add pagination to all listing screens and related resources (e.g., animals, users, feedings, productions, etc.).

====================================
GOAL
====================================

- Improve performance and scalability of listings
- Ensure consistent pagination behavior across the system
- Integrate pagination with existing search and filters
- Maintain a clean and reusable implementation

====================================
CONSTRAINTS
====================================

- Do NOT refactor unrelated parts of the system
- Do NOT break existing endpoints
- Keep changes minimal and incremental
- Respect existing architecture (domain/application/infra separation)
- Controllers must remain thin
- Business logic must stay in services
- Frontend must use service layer

====================================
DESIGN GUIDELINES
====================================

- Pagination must be backend-driven

- Use standard parameters:
  - page
  - size

- Only apply pagination when both parameters are provided (respect existing pattern if defined)

- Response should include:
  - Data list
  - Metadata (total items, total pages, current page)

- Ensure compatibility with:
  - Search
  - Filters
  - Farm scoping (if applicable)

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Identify current listing endpoints
   - Identify existing patterns for pagination (if any)

2. Backend:

- Update listing endpoints:
  - Accept pagination parameters
  - Return paginated response structure

- Ensure:
  - Queries are efficient (database-level pagination)
  - Consistent response format across endpoints

3. Frontend:

- Create reusable pagination component

- Integrate into all listing screens:
  - Animals
  - Users
  - Feedings
  - Productions
  - Others as applicable

- Behavior:
  - Trigger API calls via service layer
  - Preserve filters and search state across pages

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Pagination behavior
  - Query parameters
  - Response structure
  - UI interaction pattern

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- Pagination works across all listings
- Page navigation updates data correctly
- Filters and search persist across pages
- Backend queries are efficient
- No regression in existing listing behavior

====================================
ANTI-PATTERNS TO AVOID
====================================

- Paginating only in frontend
- Breaking existing endpoints without fallback
- Inconsistent pagination parameters
- Duplicating pagination logic per screen

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

Deliver a consistent, efficient, and reusable pagination system across all listing features.