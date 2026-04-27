You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - UI patterns and components
  - Existing filter/search implementations
  - Design conventions
  - State management approach

- Do NOT assume how filters are currently implemented
- Do NOT guess UI patterns
- Validate everything against the codebase

====================================
TASK
====================================

Standardize all filter components across the application.

This includes:
- Multi-selection filters
- Single selection filters
- Checkboxes
- Dropdowns
- Date filters
- Any other filtering UI elements

====================================
GOAL
====================================

- Provide a consistent and clean UX for all filters
- Improve usability and visual coherence
- Ensure filters are reusable and scalable
- Reduce duplication and inconsistencies

====================================
CONSTRAINTS
====================================

- Do NOT refactor unrelated business logic
- Do NOT break existing filtering behavior
- Keep changes incremental and isolated
- Respect existing frontend architecture
- All API interactions must continue via service layer

====================================
DESIGN GUIDELINES
====================================

- Standardization:

  - Multi-select → use dropdown (select component with multi-selection)
  - Single select → use dropdown
  - Boolean filters → use styled toggle or checkbox
  - Date filters → use consistent date picker

- Visual consistency:
  - Same spacing, sizing, and interaction pattern
  - Same labeling style across all filters

- Reusability:
  - Create reusable filter components
  - Components must be configurable and decoupled

- UX:
  - Clear labels and placeholders
  - Easy to reset/clear filters
  - Consistent behavior across all screens

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Identify all filter implementations across the app
   - Map inconsistencies (UI, behavior, structure)

2. Frontend:

- Create reusable components:
  - Dropdown (single/multi)
  - Checkbox/toggle
  - Date filter
  - Filter container/layout

- Replace existing implementations:
  - Gradually migrate screens to standardized components
  - Keep behavior intact

- Ensure:
  - Filters integrate with existing API calls
  - No duplication of logic

3. Backend:

- Ensure filter parameters remain consistent
- Do NOT change API contracts unless strictly necessary

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Standard filter patterns
  - Component usage guidelines
  - Naming conventions

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- All filters follow the same visual and interaction pattern
- Filtering behavior remains correct
- No regression in search/filter functionality
- Components are reusable and configurable
- UI is consistent across all listing and dashboard screens

====================================
ANTI-PATTERNS TO AVOID
====================================

- Duplicating filter logic per screen
- Mixing different UI patterns for same type of filter
- Hardcoding filter behavior
- Breaking existing API integration

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

Deliver a unified, reusable, and scalable filtering system with consistent UX across the entire application.