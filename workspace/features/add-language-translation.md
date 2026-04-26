You are a senior software engineer working on a fullstack project (React + Spring Boot).

====================================
MANDATORY CONTEXT USAGE
====================================

- BEFORE making any decision, you MUST read AI_CONTEXT.md
- Use it as the single source of truth for:
  - UI/label conventions
  - Error handling patterns
  - Enum/role representations
  - Existing localization (i18n) approach, if any

- Do NOT assume current language strategy
- Do NOT hardcode translations without verifying patterns
- Validate everything against the codebase

====================================
TASK
====================================

Identify and translate all user-facing labels, messages, and responses that are still in English, aligning them with the system’s chosen language.

This includes:
- UI labels (buttons, fields, titles)
- Validation/error messages
- API response messages (when exposed to frontend)
- Enum/role labels displayed in UI (e.g., MANAGER)

====================================
GOAL
====================================

- Ensure consistent language across the entire application
- Improve UX by eliminating mixed-language content
- Centralize and standardize text handling
- Keep implementation scalable for future localization

====================================
CONSTRAINTS
====================================

- Do NOT refactor unrelated business logic
- Do NOT break existing API contracts
- Keep changes minimal and controlled
- Respect existing architecture and patterns
- Controllers must remain thin
- Business logic must stay in services
- Frontend must use service layer

====================================
DESIGN GUIDELINES
====================================

- Treat all user-facing text as part of presentation layer

- Avoid hardcoding strings directly in:
  - Components
  - Controllers
  - Services (unless internal)

- Prefer centralized text management:
  - Frontend: i18n/config files or constants
  - Backend: message sources or consistent response mapping

- Enum/roles:
  - Keep internal values (e.g., MANAGER)
  - Map to user-friendly labels in UI

- Errors:
  - Ensure messages returned to frontend are user-friendly
  - Avoid exposing technical/internal messages

====================================
IMPLEMENTATION INSTRUCTIONS
====================================

1. Analyze:
   - Read AI_CONTEXT.md
   - Identify where labels/messages are defined (frontend and backend)
   - Search for English strings in:
     - UI components
     - API responses
     - Validation messages
     - Enums displayed in UI

2. Backend:

- Review response messages:
  - Ensure user-facing messages are localized
  - Keep internal logs/messages unchanged if appropriate

- Validate error handling:
  - Ensure consistent message format

3. Frontend:

- Replace hardcoded strings with centralized labels
- Translate:
  - Buttons, forms, titles
  - Error messages
  - Enum labels (e.g., MANAGER → localized label)

- Ensure:
  - Consistent terminology across screens
  - Reusable label structure

====================================
AI_CONTEXT.md MAINTENANCE (MANDATORY)
====================================

- Update AI_CONTEXT.md to reflect:
  - Language conventions
  - Where labels/messages are defined
  - Mapping strategy for enums and roles

- Keep updates concise and structured

====================================
VALIDATION (MANDATORY)
====================================

- No visible English labels remain (unless intentionally kept)
- UI is consistent in selected language
- Error messages are clear and user-friendly
- Enum values are properly mapped for display
- No regression in functionality

====================================
ANTI-PATTERNS TO AVOID
====================================

- Hardcoding translations in multiple places
- Translating internal system values (IDs, enums) directly
- Mixing languages across screens
- Breaking response formats

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

Deliver a consistent and scalable localization of all user-facing text, eliminating mixed-language issues and improving overall user experience.