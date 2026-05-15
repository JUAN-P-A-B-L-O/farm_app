OpenAI Codex v0.128.0 (research preview)
--------
workdir: /home/juaneugenio/programmig/farm_app
model: gpt-5.4
provider: openai
approval: never
sandbox: workspace-write [workdir, /tmp, /home/juaneugenio/.codex/memories]
reasoning effort: high
reasoning summaries: none
session id: 019e2c5d-e41c-7a30-bd44-b0930dc4cfb0
--------
user
 SCOPE
  Active backend: backend/farmapp. Active frontend: frontend/web/farm_web. Farm management system for farms, animals,
  feedings, productions, feed types, milk prices, users, dashboard, analytics, CSV export. Runtime DB: PostgreSQL. Local
  fallback: Spring local profile with H2. Tests use H2. Schema mode: spring.jpa.hibernate.ddl-auto=create.

  ARCHITECTURE
  Backend: Spring Boot modular monolith. Preserve current layers: controller, service, repository, mapper, entity/dto,
  shared, auth. Controllers thin; services own validation, business rules, orchestration, transactions. Persistence is
  scalar-ID based, not relation-heavy. Do not assume strict Clean Architecture package split; evolve incrementally only.
  Shared backend utilities: milkprice.service.MilkPriceService resolves current milk price; shared.util.CsvExportUtils
  generates CSV rows; shared.util.CsvResponseFactory builds CSV responses; shared.currency centralizes BRL/USD
  conversion.
  Frontend: React + TypeScript + Vite. Preserve separation: src/pages, src/components, src/services, src/types, src/
  layout, src/context, src/hooks. Use typed service layer for API calls. Farm selection via FarmContext. Display
  currency via CurrencyContext. JWT attachment and 401 handling centralized in Axios layer. First authenticated 401
  clears auth state and forces logout; /auth/login failures do not auto-logout. Reuse shared listing filter/pagination/
  export patterns. CSV downloads go through service layer and csvExportService.ts. Use shared currency helpers, not
  hardcoded formatting.

  GLOBAL RULES
  Base URL: http://localhost:8080. Content type: JSON. Protected endpoints require Authorization: Bearer <jwt>. Public
  paths: /auth/, /v3/api-docs/, /swagger-ui/**, /swagger-ui.html.
  Error envelope: timestamp, status, error, path.
  Pagination applies only when both page and size are provided; paginated payload is content, page, size, totalElements,
  totalPages.
  Delete semantics: animals/feedings/productions soft-delete via status=INACTIVE; feed types soft-delete via
  active=false; users hard-delete row plus farm assignments.
  MANAGER role is required for dashboard, analytics, all DELETE requests, and user create/update/activate/inactivate/
  delete.
  Financial values are derived at read/export time, not persisted. Current reporting uses current effective farm milk
  price by default.
  Do not change API contracts, validations, JWT protection, or financial logic without explicit approval.

  DOMAIN RULES
  Farms
  Endpoints: GET /farms, POST /farms.
  POST requires nonblank name; authenticated creator becomes owner.
  GET /farms returns farms accessible byScope:
  Active backend: backend/farmapp
  Active frontend: frontend/web/farm_web
  System: farm/cattle management for farms, animals, feedings, productions, feed types, milk prices, users, dashboard,
  analytics, CSV export
  Runtime DB: PostgreSQL; local fallback profile: local with H2; tests use H2
  Use backend/farmapp as the single backend source of truth

  Architecture:
  Backend is a Spring Boot 3.4.x modular monolith
  Preserve current layering: controller, service, repository, mapper, entity/dto, shared, auth
  Controllers stay thin; services own validation, business rules, orchestration, transactions
  Persistence is scalar-ID oriented; avoid relation-heavy JPA modeling
  Do not assume strict Clean Architecture package separation exists; evolve incrementally only
  CSV backend utilities are centralized in shared.util.CsvExportUtils and shared.util.CsvResponseFactory
  Currency conversion is centralized in shared.currency; supported currencies: BRL, USD
  Milk price resolution is centralized in milkprice.service.MilkPriceService

  Frontend:
  React + TypeScript + Vite
  Keep separation: pages, components, services, types, layout, context, hooks
  Use typed service layer for HTTP; do not hardcode API contracts in components
  Default visible language is pt-BR; avoid mixed-language UI
  Frontend user-facing labels live in frontend/web/farm_web/src/i18n and should be consumed via LanguageContext/useTranslation
  Enum values such as MANAGER, WORKER, ACTIVE, INACTIVE, SOLD, PURCHASED, and BORN remain internal/API values and must be mapped to localized labels in the UI
  Backend error responses returned to the frontend are localized centrally in shared.exception so services can keep internal canonical messages when needed
  JWT attachment is centralized in Axios service layer
  On first authenticated 401, clear auth state and force logout; /auth/login failures do not trigger auto-logout
  Farm selection is centralized in FarmContext
  Display currency is centralized in CurrencyContext
  Reuse shared listing filters/pagination/export patterns; do not duplicate filter/query wiring
  Listing filters use page-local draft state and separate applied state so pagination/refresh/export reuse the same
  query params
  Dashboard filters follow the same draft/applied pattern so refresh and CSV export reuse the exact selected scope
  Dashboard animal filtering supports a single animalId or a multi-select animalIds scope; the selected farm still comes from FarmContext
  Shared listing filter component: frontend/web/farm_web/src/components/common/ListingFiltersBar.tsx
  CSV downloads are triggered from service layer; frontend helper is centralized in frontend/web/farm_web/src/services/
  csvExportService.ts
  Use shared currency helpers; do not hardcode currency symbols or Intl currency config in components

  Security and cross-cutting behavior:
  All endpoints except /auth/, /v3/api-docs/, /swagger-ui/**, /swagger-ui.html require JWT
  Authorization header: Bearer <jwt>
  JWT auth is stateless; filter runs before UsernamePasswordAuthenticationFilter; passwords use BCrypt
  Inactive users are rejected by login and JWT-authenticated access
  Standard error envelope: timestamp, status, error, path
  List pagination is optional and only applies when both page and size are provided; paginated response shape: content,
  page, size, totalElements, totalPages
  Non-manager authenticated users get 403 for dashboard, analytics, user creation, and delete operations
  MANAGER is required for dashboard, analytics, all DELETE requests, and user create/update/activate/inactivate/delete
  Any authenticated user may call PUT /users/me/password

  Domain rules:
  Farm is the operating boundary for animals, feeding, production, feed types, dashboard, and analytics
  Farms:
  GET /farms returns farms accessible to the authenticated user via ownership or explicit assignment
  GET /farms?ownedOnly=true returns only farms owned by the authenticated user
  POST /farms requires nonblank name; authenticated user becomes owner

  Animals:
  Endpoints: POST /animals, GET /animals, GET /animals/export, GET /animals/{id}, PUT /animals/{id}, POST /animals/{id}/
  sell, DELETE /animals/{id}
  Required create fields: tag, breed, birthDate, origin, farmId
  origin must be PURCHASED or BORN
  acquisitionCost is required and > 0 when origin=PURCHASED; cleared when origin=BORN
  tag is unique
  new animals default to ACTIVE
  PUT /animals/{id} is partial; provided string fields must be nonblank
  Selling must use POST /animals/{id}/sell; generic update cannot replace sell flow
  Sell stores salePrice, optional saleDate default current date, and changes status to SOLD
  Only ACTIVE animals can be sold
  Sold animals cannot transition back through generic update
  DELETE /animals/{id} is soft delete to INACTIVE and requires MANAGER
  GET /animals supports optional farmId, search, status, origin, page, size
  Responses include optional salePrice and saleDate

  Productions:
  Endpoints: GET /productions, GET /productions/export, GET /productions/{id}, GET /productions/summary/by-animal, GET /
  productions/summary/profit/by-animal, POST /productions, PUT /productions/{id}, DELETE /productions/{id}
  Required create fields: animalId, date, quantity, userId
  animalId and userId nonblank; userId must be valid UUID and existing user
  date nonnull and not future
  quantity > 0
  animal must exist and be ACTIVE
  Authenticated user context may override/fill createdBy
  If authenticated role is WORKER, create ignores incoming date and stores current server date
  If authenticated role is MANAGER, create still requires explicit date
  GET /productions supports optional search, animalId, date, farmId, page, size
  PUT /productions/{id} may change animalId, date, quantity
  DELETE /productions/{id} is soft delete to INACTIVE and requires MANAGER
  Profit summary includeAcquisitionCost defaults to true
  Profit summary uses current milk price for the animal farm
  Responses include embedded animal summary and do not expose createdBy

  Feedings:
  Endpoints: POST /feedings, GET /feedings, GET /feedings/export, GET /feedings/{id}, PUT /feedings/{id}, DELETE /
  feedings/{id}
  Required create fields: animalId, feedTypeId, date, quantity, userId
  All IDs nonblank; userId must be valid UUID and existing user
  date nonnull
  quantity > 0
  animal must exist and be ACTIVE
  feed type must exist
  Authenticated user context may override/fill createdBy
  If authenticated role is WORKER, create ignores incoming date and stores current server date
  If authenticated role is MANAGER, create still requires explicit date
  GET /feedings supports optional search, animalId, feedTypeId, date, farmId, page, size
  PUT /feedings/{id} may change animalId, feedTypeId, date, quantity
  DELETE /feedings/{id} is soft delete to INACTIVE and requires MANAGER
  Responses include embedded animal summary and feed type summary and do not expose calculated feeding cost

  Feed types:
  Endpoints: POST /feed-types, GET /feed-types, GET /feed-types/export, GET /feed-types/{id}, PUT /feed-types/{id},
  DELETE /feed-types/{id}
  POST requires farmId query param plus name and costPerKg
  name nonblank
  costPerKg > 0
  New feed types default active=true
  List and read return active feed types
  GET /feed-types supports optional search, page, size
  PUT /feed-types/{id} updates name and costPerKg
  DELETE /feed-types/{id} is soft delete to active=false and requires MANAGER

  Milk prices:
  Endpoints: POST /milk-prices?farmId=..., GET /milk-prices/current?farmId=..., GET /milk-prices?farmId=..., GET /milk-
  prices/export?farmId=...
  POST requires accessible existing farmId, price, effectiveDate
  price > 0 and max 2 decimal places
  effectiveDate nonnull
  Create is append-only history; never overwrite previous prices
  Current price is the latest record effective on or before today
  If no effective price exists, current returns default price 2.0 with fallbackDefault=true
  GET /milk-prices returns full history newest to oldest and supports optional search, effectiveDate, page, size
  Response fields: id, farmId, price, effectiveDate, createdAt, createdBy, fallbackDefault

  Users:
  Endpoints: POST /users, GET /users, GET /users/export, GET /users/{id}, PUT /users/{id}, PATCH /users/{id}/activate,
  PATCH /users/{id}/inactivate, DELETE /users/{id}, PUT /users/me/password
  Required fields on create/update: name, email, role, active, farmIds
  password is required if active=true on create
  avatarUrl is optional
  name, email, role nonblank
  active nonnull
  farmIds must contain at least one value
  GET /users/{id} requires valid UUID
  email is unique
  Only authenticated MANAGER can call POST /users, PUT /users/{id}, PATCH /users/{id}/activate, PATCH /users/{id}/
  inactivate, DELETE /users/{id}
  Every assigned farmId must belong to the authenticated manager
  PATCH /users/{id}/activate reactivates the user and may replace the password
  PATCH /users/{id}/inactivate sets active=false
  DELETE /users/{id} removes the user row and its farm assignments
  Users who own farms cannot be inactivated or deleted
  Users who own farms cannot be changed from MANAGER to another role
  PUT /users/me/password is self-service only; currentPassword and newPassword must be nonblank; current must match
  stored password; new must differ from current
  GET /users supports optional search, active, role, page, size
  GET /users and GET /users/{id} require JWT

  Auth:
  Endpoint: POST /auth/login
  Required fields: email, password; both nonblank
  Invalid credentials return 401
  Success returns accessToken and user object with id, name, email, role
  Default admin is created on startup if user table is empty
  Default admin env/fallback: ADMIN_EMAIL default admin@farmapp.com; ADMIN_PASSWORD default admin123

  Dashboard, analytics, CSV, financial behavior:
  GET /dashboard and GET /dashboard/export require MANAGER
  All /analytics/** endpoints require MANAGER
  GET /dashboard and GET /analytics/profit accept includeAcquisitionCost; default true
  GET /dashboard and GET /dashboard/export support optional startDate, endDate, animalId, animalIds, and status filters
  Dashboard default filter scope is all time, all animals, all statuses within the selected farm context
  Dashboard date range filters production, feeding cost, milk revenue, and sale revenue; animal count and acquisition
  cost remain scoped by farm/animal/status because acquisition date is not modeled
  GET /dashboard, GET /dashboard/export, GET /analytics/feeding, GET /analytics/feeding/export, GET /analytics/profit,
  GET /analytics/profit/export accept optional currency
  Dashboard revenue includes milk revenue plus sold-animal revenue
  Dashboard total profit subtracts feeding cost and, when enabled, acquisition cost
  Analytics profit series applies acquisition cost once to the earliest returned period
  Analytics profit series recognizes sold-animal revenue on each animal saleDate
  Current dashboard and analytics milk revenue use the current effective milk price for each farm
  Historical milk prices are preserved; current reporting still uses latest effective farm price by default
  CSV export exists for animals, users, feedings, productions, feed types, milk prices, dashboard, analytics
  Listing/dashboard/analytics exports must reuse the same farm/filter/currency scope as the corresponding screen or JSON
  request

  Data model and persistence:
  Main entities:
  User(id, name, email, role, password, active)
  Farm(id, name, ownerId)
  UserFarmAssignment(id, userId, farmId)
  Animal(id, tag, breed, birthDate, status, origin, acquisitionCost, salePrice, saleDate, farmId)
  Production(id, animalId, date, quantity, createdBy, farmId, status)
  Feeding(id, animalId, feedTypeId, date, quantity, createdBy, farmId, status)
  MilkPrice(id, farmId, price, effectiveDate, createdAt, createdBy)
  FeedType(id, name, costPerKg, active, farmId)
  DB constraints:
  animals.tag unique
  users.email unique
  Relational integrity checks are primarily enforced in services, not rich JPA associations
  User-farm access is persisted via scalar-ID assignment table
  Feeding and production delete behavior uses status=INACTIVE
  Feed type delete behavior uses active=false
  Feeding cost is derived from quantity * costPerKg
  Milk price history is append-only
  Revenue and profit are derived at query/export time, not persisted

  Development rules:
  Do not break existing functionality
  Do not refactor the entire codebase
  Make incremental changes only
  Always respect existing API contracts unless a contract change is explicitly approved
  Preserve module boundaries and naming
  Prefer extending existing services/controllers over parallel patterns
  Account for backend security being stricter than some frontend assumptions
  Validate all API-boundary input
  Prefer compatibility over novelty
  Follow existing patterns
  If frontend and backend are out of sync, fix the mismatch with the smallest coherent change
  Verify frontend requests against real backend endpoints before changing UI code
  If a requested feature assumes missing infrastructure, document the gap first and implement the narrowest viable step

  Testing:
  Backend tests include Spring Boot integration tests, controller contract tests, service/unit tests, auth integration
  tests
  Protected endpoint tests must use JWT
  When adding or changing protected features, include authenticated test coverage
  Manual script exists at backend/test/animal/manual test/test-animals.sh

  Do not:
  Do not change API contracts without explicit approval
  Do not remove existing validations
  Do not introduce breaking changes
  Do not replace thin controllers with business-heavy controllers
  Do not bypass the service layer for business operations
  Do not couple business logic to JPA annotations or entity graph behavior
  Do not introduce rich JPA relation graphs unless explicitly required
  Do not assume missing backend endpoints exist because frontend stubs reference them
  Do not introduce large-scale refactors to force stricter architecture in one pass
  Do not remove JWT protection from protected endpoints
  Do not silently change derived financial logic, especially milk price, revenue, or profit behavior
  Do not work in a parallel backend tree unless explicitly required

  Version control:
  Every meaningful change must result in a commit
  Use Conventional Commits
  Commits must be atomic and focused
  Never push automatically
  Before committing, ensure no breaking changes and code compiles logically

You are a senior software planner. Create a concise implementation plan. Do not write code. Focus on target areas, steps, risks, and validation.

Create a concise implementation plan for the feature below.

Feature:
# Feature: Upgrade Flow for Free Users

## Goal
Ensure Free users are consistently prompted to upgrade when accessing restricted features, with a clear path to a plans/pricing page.

## Scope
- Frontend: feature gating UI, modal/popup, plans/pricing page
- Backend: plan awareness (no payment logic)

## Requirements
- Detect when a Free user attempts to access a restricted feature
- Show a modal/popup explaining the limitation
- Provide clear CTA to “Upgrade Plan”
- Redirect to a dedicated plans/pricing page
- Plans page must present available plans (Free vs Paid conceptually)
- Ensure upgrade prompts are consistent across the application

## Constraints
- Do NOT implement payment, Stripe, or checkout
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized
- Follow existing architecture and UI patterns

## Implementation Notes
- Centralize feature access checks (reuse existing plan/feature gating logic)
- Trigger upgrade modal from a shared component/hook (avoid duplication)
- Keep modal lightweight with clear messaging and actions
- Implement a reusable plans/pricing page
- Ensure navigation to plans page is consistent from all entry points

## Validation
- Free users see upgrade prompt when accessing restricted features
- Modal displays correct messaging and CTA
- Navigation to plans page works correctly
- No regression in accessible features for Free users

## Done Criteria
- Upgrade prompt is consistently triggered across restricted features
- Plans/pricing page is accessible and informative
- Flow from restriction → modal → plans page works end-to-end
- No payment logic introduced

Rules:
- Do not write code.
- Keep the plan concise.
- Identify target files/modules if possible.
- Identify risks.
- Identify validation steps.
- Prefer incremental execution.
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
I’m mapping the current frontend/backend structure first so the plan targets real modules and existing patterns instead of inventing new ones. After that I’ll outline the smallest coherent implementation path for gating, modal reuse, and the plans page.
exec
/bin/bash -lc 'rg -n "LanguageContext|useTranslation|CurrencyContext|FarmContext|AuthContext|axios|401|modal|dialog|plan|pricing|subscription|feature gate|role|MANAGER|WORKER" frontend/web/farm_web/src' in /home/juaneugenio/programmig/farm_app
exec
/bin/bash -lc "rg --files backend/farmapp frontend/web/farm_web | rg 'src/.+|pom.xml|package.json|vite.config|router|routes|context|i18n|plan|pricing|subscription|feature|modal|dialog|auth|layout|services|hooks'" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
frontend/web/farm_web/src/App.tsx:22:import { hasFeatureAccess } from './utils/planAccess'
frontend/web/farm_web/src/components/user/UserForm.tsx:3:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/user/UserForm.tsx:35:  const { t } = useTranslation()
frontend/web/farm_web/src/components/user/UserForm.tsx:106:      role: formData.role,
frontend/web/farm_web/src/components/user/UserForm.tsx:123:    if (!payload.role) {
frontend/web/farm_web/src/components/user/UserForm.tsx:124:      setValidationMessage(t('accessControl.errors.roleRequired'))
frontend/web/farm_web/src/components/user/UserForm.tsx:175:          <span>{t('accessControl.form.role')}</span>
frontend/web/farm_web/src/components/user/UserForm.tsx:176:          <select name="role" value={formData.role} onChange={handleChange} required>
frontend/web/farm_web/src/components/user/UserForm.tsx:178:            {USER_ROLES.map((role) => (
frontend/web/farm_web/src/components/user/UserForm.tsx:179:              <option key={role} value={role}>
frontend/web/farm_web/src/components/user/UserForm.tsx:180:                {getUserRoleLabel(t, role)}
frontend/web/farm_web/src/hooks/useTranslation.ts:1:import { useLanguage, translations } from '../context/LanguageContext'
frontend/web/farm_web/src/hooks/useTranslation.ts:27:export function useTranslation() {
frontend/web/farm_web/src/hooks/useCurrency.ts:1:export { useCurrency } from '../context/CurrencyContext'
frontend/web/farm_web/src/assets/react.svg:1:<svg xmlns="http://www.w3.org/2000/svg" xmlns:xlink="http://www.w3.org/1999/xlink" aria-hidden="true" role="img" class="iconify iconify--logos" width="35.93" height="32" preserveAspectRatio="xMidYMid meet" viewBox="0 0 256 228"><path fill="#00D8FF" d="M210.483 73.824a171.49 171.49 0 0 0-8.24-2.597c.465-1.9.893-3.777 1.273-5.621c6.238-30.281 2.16-54.676-11.769-62.708c-13.355-7.7-35.196.329-57.254 19.526a171.23 171.23 0 0 0-6.375 5.848a155.866 155.866 0 0 0-4.241-3.917C100.759 3.829 77.587-4.822 63.673 3.233C50.33 10.957 46.379 33.89 51.995 62.588a170.974 170.974 0 0 0 1.892 8.48c-3.28.932-6.445 1.924-9.474 2.98C17.309 83.498 0 98.307 0 113.668c0 15.865 18.582 31.778 46.812 41.427a145.52 145.52 0 0 0 6.921 2.165a167.467 167.467 0 0 0-2.01 9.138c-5.354 28.2-1.173 50.591 12.134 58.266c13.744 7.926 36.812-.22 59.273-19.855a145.567 145.567 0 0 0 5.342-4.923a168.064 168.064 0 0 0 6.92 6.314c21.758 18.722 43.246 26.282 56.54 18.586c13.731-7.949 18.194-32.003 12.4-61.268a145.016 145.016 0 0 0-1.535-6.842c1.62-.48 3.21-.974 4.76-1.488c29.348-9.723 48.443-25.443 48.443-41.52c0-15.417-17.868-30.326-45.517-39.844Zm-6.365 70.984c-1.4.463-2.836.91-4.3 1.345c-3.24-10.257-7.612-21.163-12.963-32.432c5.106-11 9.31-21.767 12.459-31.957c2.619.758 5.16 1.557 7.61 2.4c23.69 8.156 38.14 20.213 38.14 29.504c0 9.896-15.606 22.743-40.946 31.14Zm-10.514 20.834c2.562 12.94 2.927 24.64 1.23 33.787c-1.524 8.219-4.59 13.698-8.382 15.893c-8.067 4.67-25.32-1.4-43.927-17.412a156.726 156.726 0 0 1-6.437-5.87c7.214-7.889 14.423-17.06 21.459-27.246c12.376-1.098 24.068-2.894 34.671-5.345a134.17 134.17 0 0 1 1.386 6.193ZM87.276 214.515c-7.882 2.783-14.16 2.863-17.955.675c-8.075-4.657-11.432-22.636-6.853-46.752a156.923 156.923 0 0 1 1.869-8.499c10.486 2.32 22.093 3.988 34.498 4.994c7.084 9.967 14.501 19.128 21.976 27.15a134.668 134.668 0 0 1-4.877 4.492c-9.933 8.682-19.886 14.842-28.658 17.94ZM50.35 144.747c-12.483-4.267-22.792-9.812-29.858-15.863c-6.35-5.437-9.555-10.836-9.555-15.216c0-9.322 13.897-21.212 37.076-29.293c2.813-.98 5.757-1.905 8.812-2.773c3.204 10.42 7.406 21.315 12.477 32.332c-5.137 11.18-9.399 22.249-12.634 32.792a134.718 134.718 0 0 1-6.318-1.979Zm12.378-84.26c-4.811-24.587-1.616-43.134 6.425-47.789c8.564-4.958 27.502 2.111 47.463 19.835a144.318 144.318 0 0 1 3.841 3.545c-7.438 7.987-14.787 17.08-21.808 26.988c-12.04 1.116-23.565 2.908-34.161 5.309a160.342 160.342 0 0 1-1.76-7.887Zm110.427 27.268a347.8 347.8 0 0 0-7.785-12.803c8.168 1.033 15.994 2.404 23.343 4.08c-2.206 7.072-4.956 14.465-8.193 22.045a381.151 381.151 0 0 0-7.365-13.322Zm-45.032-43.861c5.044 5.465 10.096 11.566 15.065 18.186a322.04 322.04 0 0 0-30.257-.006c4.974-6.559 10.069-12.652 15.192-18.18ZM82.802 87.83a323.167 323.167 0 0 0-7.227 13.238c-3.184-7.553-5.909-14.98-8.134-22.152c7.304-1.634 15.093-2.97 23.209-3.984a321.524 321.524 0 0 0-7.848 12.897Zm8.081 65.352c-8.385-.936-16.291-2.203-23.593-3.793c2.26-7.3 5.045-14.885 8.298-22.6a321.187 321.187 0 0 0 7.257 13.246c2.594 4.48 5.28 8.868 8.038 13.147Zm37.542 31.03c-5.184-5.592-10.354-11.779-15.403-18.433c4.902.192 9.899.29 14.978.29c5.218 0 10.376-.117 15.453-.343c-4.985 6.774-10.018 12.97-15.028 18.486Zm52.198-57.817c3.422 7.8 6.306 15.345 8.596 22.52c-7.422 1.694-15.436 3.058-23.88 4.071a382.417 382.417 0 0 0 7.859-13.026a347.403 347.403 0 0 0 7.425-13.565Zm-16.898 8.101a358.557 358.557 0 0 1-12.281 19.815a329.4 329.4 0 0 1-23.444.823c-7.967 0-15.716-.248-23.178-.732a310.202 310.202 0 0 1-12.513-19.846h.001a307.41 307.41 0 0 1-10.923-20.627a310.278 310.278 0 0 1 10.89-20.637l-.001.001a307.318 307.318 0 0 1 12.413-19.761c7.613-.576 15.42-.876 23.31-.876H128c7.926 0 15.743.303 23.354.883a329.357 329.357 0 0 1 12.335 19.695a358.489 358.489 0 0 1 11.036 20.54a329.472 329.472 0 0 1-11 20.722Zm22.56-122.124c8.572 4.944 11.906 24.881 6.52 51.026c-.344 1.668-.73 3.367-1.15 5.09c-10.622-2.452-22.155-4.275-34.23-5.408c-7.034-10.017-14.323-19.124-21.64-27.008a160.789 160.789 0 0 1 5.888-5.4c18.9-16.447 36.564-22.941 44.612-18.3ZM128 90.808c12.625 0 22.86 10.235 22.86 22.86s-10.235 22.86-22.86 22.86s-22.86-10.235-22.86-22.86s10.235-22.86 22.86-22.86Z"></path></svg>
frontend/web/farm_web/src/hooks/useAuth.ts:2:import { AuthContext } from '../context/authContext'
frontend/web/farm_web/src/hooks/useAuth.ts:5:  const context = useContext(AuthContext)
frontend/web/farm_web/src/hooks/useFarm.ts:2:import { FarmContext } from '../context/farmContext'
frontend/web/farm_web/src/hooks/useFarm.ts:5:  const context = useContext(FarmContext)
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx:2:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx:3:import { getFeatureAccessState } from '../../utils/planAccess'
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx:23:  const { t } = useTranslation()
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx:29:      ? `${label} (${t('plan.badge')})`
frontend/web/farm_web/src/utils/currency.ts:1:import type { CurrencyCode } from '../context/CurrencyContext'
frontend/web/farm_web/src/utils/currency.ts:2:import type { Language } from '../context/LanguageContext'
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:1:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:2:import { getFeatureMetadata, type AppFeature } from '../../utils/planAccess'
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:9:  const { t } = useTranslation()
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:15:        <p className="animals-page__eyebrow">{t('plan.eyebrow')}</p>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:16:        <h1>{t('plan.title')}</h1>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:17:        <p className="animals-page__description">{t('plan.description')}</p>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:20:      <section className="animals-panel plan-upgrade-notice">
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:21:        <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:24:        <p>{t('plan.upgradeHint')}</p>
frontend/web/farm_web/src/pages/milk-price/MilkPricePage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/milk-price/MilkPricePage.tsx:9:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/milk-price/MilkPricePage.tsx:39:  if (axios.isAxiosError<MilkPriceApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/milk-price/MilkPricePage.tsx:48:  const { t, language } = useTranslation()
frontend/web/farm_web/src/components/common/FeedbackToast.tsx:2:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/common/FeedbackToast.tsx:28:  const { t } = useTranslation()
frontend/web/farm_web/src/components/common/FeedbackToast.tsx:34:      role="status"
frontend/web/farm_web/src/utils/planAccess.ts:25:const planMetadata: Record<UserPlan, PlanMetadata> = {
frontend/web/farm_web/src/utils/planAccess.ts:27:    labelKey: 'plan.labels.FREE',
frontend/web/farm_web/src/utils/planAccess.ts:32:    labelKey: 'plan.labels.PRO',
frontend/web/farm_web/src/utils/planAccess.ts:41:    titleKey: 'plan.features.dashboard.title',
frontend/web/farm_web/src/utils/planAccess.ts:42:    descriptionKey: 'plan.features.dashboard.description',
frontend/web/farm_web/src/utils/planAccess.ts:46:    titleKey: 'plan.features.analytics.title',
frontend/web/farm_web/src/utils/planAccess.ts:47:    descriptionKey: 'plan.features.analytics.description',
frontend/web/farm_web/src/utils/planAccess.ts:51:    titleKey: 'plan.features.csvExport.title',
frontend/web/farm_web/src/utils/planAccess.ts:52:    descriptionKey: 'plan.features.csvExport.description',
frontend/web/farm_web/src/utils/planAccess.ts:56:export function resolvePlan(user: Pick<User, 'plan'> | null | undefined): UserPlan {
frontend/web/farm_web/src/utils/planAccess.ts:57:  return user?.plan ?? 'FREE'
frontend/web/farm_web/src/utils/planAccess.ts:60:export function getPlanMetadata(plan: UserPlan) {
frontend/web/farm_web/src/utils/planAccess.ts:61:  return planMetadata[plan]
frontend/web/farm_web/src/utils/planAccess.ts:64:export function getCurrentPlanMetadata(user: Pick<User, 'plan'> | null | undefined) {
frontend/web/farm_web/src/utils/planAccess.ts:69:  user: Pick<User, 'plan'> | null | undefined,
frontend/web/farm_web/src/utils/planAccess.ts:76:    allowed: planMetadata[currentPlan].rank >= planMetadata[metadata.minimumPlan].rank,
frontend/web/farm_web/src/utils/planAccess.ts:84:export function hasFeatureAccess(user: Pick<User, 'plan'> | null | undefined, feature: AppFeature) {
frontend/web/farm_web/src/pages/analytics/AnalyticsPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/analytics/AnalyticsPage.tsx:12:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/analytics/AnalyticsPage.tsx:33:  if (axios.isAxiosError<ApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/analytics/AnalyticsPage.tsx:77:  const { t, language } = useTranslation()
frontend/web/farm_web/src/utils/authorization.ts:3:export const MANAGER_ROLE = 'MANAGER'
frontend/web/farm_web/src/utils/authorization.ts:5:export function isManager(user: Pick<User, 'role'> | null | undefined) {
frontend/web/farm_web/src/utils/authorization.ts:6:  return user?.role?.trim().toUpperCase() === MANAGER_ROLE
frontend/web/farm_web/src/context/farmContext.ts:4:export interface FarmContextValue {
frontend/web/farm_web/src/context/farmContext.ts:16:export const FarmContext = createContext<FarmContextValue | undefined>(undefined)
frontend/web/farm_web/src/main.tsx:6:import { AuthProvider } from './context/AuthContext.tsx'
frontend/web/farm_web/src/main.tsx:7:import { CurrencyProvider } from './context/CurrencyContext.tsx'
frontend/web/farm_web/src/main.tsx:9:import { FarmProvider } from './context/FarmContext.tsx'
frontend/web/farm_web/src/main.tsx:10:import { LanguageProvider } from './context/LanguageContext.tsx'
frontend/web/farm_web/src/context/FarmContext.tsx:12:import { FarmContext } from './farmContext'
frontend/web/farm_web/src/context/FarmContext.tsx:114:  return <FarmContext.Provider value={value}>{children}</FarmContext.Provider>
frontend/web/farm_web/src/context/CurrencyContext.tsx:12:interface CurrencyContextValue {
frontend/web/farm_web/src/context/CurrencyContext.tsx:20:const CurrencyContext = createContext<CurrencyContextValue | undefined>(undefined)
frontend/web/farm_web/src/context/CurrencyContext.tsx:49:  return <CurrencyContext.Provider value={value}>{children}</CurrencyContext.Provider>
frontend/web/farm_web/src/context/CurrencyContext.tsx:53:  const context = useContext(CurrencyContext)
frontend/web/farm_web/src/components/common/PaginationControls.tsx:1:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/common/PaginationControls.tsx:19:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/feed-type/FeedTypePage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/feed-type/FeedTypePage.tsx:12:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/feed-type/FeedTypePage.tsx:42:  if (axios.isAxiosError<FeedTypeApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/feed-type/FeedTypePage.tsx:67:  const { t, language } = useTranslation()
frontend/web/farm_web/src/context/AuthContext.tsx:13:import { AuthContext } from './authContext'
frontend/web/farm_web/src/context/AuthContext.tsx:76:  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
frontend/web/farm_web/src/context/LanguageContext.tsx:14:interface LanguageContextValue {
frontend/web/farm_web/src/context/LanguageContext.tsx:27:const LanguageContext = createContext<LanguageContextValue | undefined>(undefined)
frontend/web/farm_web/src/context/LanguageContext.tsx:56:  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>
frontend/web/farm_web/src/context/LanguageContext.tsx:60:  const context = useContext(LanguageContext)
frontend/web/farm_web/src/context/authContext.ts:4:export interface AuthContextValue {
frontend/web/farm_web/src/context/authContext.ts:12:export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:4:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:9:  const { t } = useTranslation()
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:4:import { getFeatureAccessState, type AppFeature } from '../../utils/planAccess'
frontend/web/farm_web/src/pages/users/UsersPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/users/UsersPage.tsx:9:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/users/UsersPage.tsx:29:  role: '',
frontend/web/farm_web/src/pages/users/UsersPage.tsx:39:  role: '',
frontend/web/farm_web/src/pages/users/UsersPage.tsx:45:  if (axios.isAxiosError<UserApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/users/UsersPage.tsx:79:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/users/UsersPage.tsx:194:      role: user.role,
frontend/web/farm_web/src/pages/users/UsersPage.tsx:400:                id: 'users-role-filter',
frontend/web/farm_web/src/pages/users/UsersPage.tsx:401:                label: t('accessControl.filters.roleLabel'),
frontend/web/farm_web/src/pages/users/UsersPage.tsx:402:                value: filters.role,
frontend/web/farm_web/src/pages/users/UsersPage.tsx:403:                onChange: (value) => setFilters((current) => ({ ...current, role: value as UserListFilters['role'] })),
frontend/web/farm_web/src/pages/users/UsersPage.tsx:406:                  ...USER_ROLES.map((role) => ({
frontend/web/farm_web/src/pages/users/UsersPage.tsx:407:                    value: role,
frontend/web/farm_web/src/pages/users/UsersPage.tsx:408:                    label: getUserRoleLabel(t, role),
frontend/web/farm_web/src/pages/users/UsersPage.tsx:472:                    <th>{t('accessControl.table.role')}</th>
frontend/web/farm_web/src/pages/users/UsersPage.tsx:492:                      <td>{getUserRoleLabel(t, user.role)}</td>
frontend/web/farm_web/src/components/farm/FarmForm.tsx:2:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/farm/FarmForm.tsx:20:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/settings/SettingsPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/settings/SettingsPage.tsx:3:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/settings/SettingsPage.tsx:9:  if (axios.isAxiosError<UserApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/settings/SettingsPage.tsx:17:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/login/LoginPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/login/LoginPage.tsx:6:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/login/LoginPage.tsx:31:  const { t, language } = useTranslation()
frontend/web/farm_web/src/pages/login/LoginPage.tsx:86:        if (axios.isAxiosError<ApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/login/LoginPage.tsx:138:      if (isSignupMode && axios.isAxiosError<ApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/login/LoginPage.tsx:140:      } else if (axios.isAxiosError(error) && error.response?.status === 401) {
frontend/web/farm_web/src/pages/login/LoginPage.tsx:142:      } else if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.status === 403) {
frontend/web/farm_web/src/pages/login/LoginPage.tsx:145:      } else if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.error) {
frontend/web/farm_web/src/pages/login/LoginPage.tsx:165:      if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.error) {
frontend/web/farm_web/src/components/feed-type/FeedTypeForm.tsx:4:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/feed-type/FeedTypeForm.tsx:32:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/dashboard/DashboardPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/dashboard/DashboardPage.tsx:11:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/dashboard/DashboardPage.tsx:53:  if (axios.isAxiosError<ApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/dashboard/DashboardPage.tsx:93:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/production/ProductionPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/production/ProductionPage.tsx:11:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/production/ProductionPage.tsx:60:  if (axios.isAxiosError<ProductionApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/production/ProductionPage.tsx:94:  const { t, language } = useTranslation()
frontend/web/farm_web/src/pages/animals/AnimalsPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/animals/AnimalsPage.tsx:11:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/animals/AnimalsPage.tsx:48:  if (axios.isAxiosError<ApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/animals/AnimalsPage.tsx:69:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/feeding/FeedingPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/feeding/FeedingPage.tsx:11:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/feeding/FeedingPage.tsx:64:  if (axios.isAxiosError<FeedingApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/feeding/FeedingPage.tsx:98:  const { t, language } = useTranslation()
frontend/web/farm_web/src/components/batch/AnimalBatchForm.tsx:2:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/batch/AnimalBatchForm.tsx:25:  const { t } = useTranslation()
frontend/web/farm_web/src/pages/animals/AnimalDetailsPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/animals/AnimalDetailsPage.tsx:7:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/animals/AnimalDetailsPage.tsx:28:  if (axios.isAxiosError<ApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/animals/AnimalDetailsPage.tsx:49:  const { t, language } = useTranslation()
frontend/web/farm_web/src/App.css:202:.app-layout__session-plan {
frontend/web/farm_web/src/App.css:209:.app-layout__plan-badge {
frontend/web/farm_web/src/App.css:218:.app-layout__plan-badge--premium {
frontend/web/farm_web/src/App.css:577:.plan-upgrade-notice {
frontend/web/farm_web/src/App.css:582:.plan-upgrade-notice__badge {
frontend/web/farm_web/src/components/dashboard/StatCard.tsx:3:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/dashboard/StatCard.tsx:34:  const { language } = useTranslation()
frontend/web/farm_web/src/components/animal/AnimalForm.tsx:3:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/animal/AnimalForm.tsx:27:  const { t } = useTranslation()
frontend/web/farm_web/src/types/user.ts:1:export type UserRole = 'MANAGER' | 'WORKER'
frontend/web/farm_web/src/types/user.ts:8:  role: UserRole
frontend/web/farm_web/src/types/user.ts:11:  plan: UserPlan
frontend/web/farm_web/src/types/user.ts:18:  role: UserRole | ''
frontend/web/farm_web/src/types/user.ts:28:  role: UserRole | ''
frontend/web/farm_web/src/pages/farm/FarmCreatePage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/farm/FarmCreatePage.tsx:7:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/farm/FarmCreatePage.tsx:18:  if (axios.isAxiosError<FarmApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/farm/FarmCreatePage.tsx:26:    if (status === 401) {
frontend/web/farm_web/src/pages/farm/FarmCreatePage.tsx:40:  const { t } = useTranslation()
frontend/web/farm_web/src/components/production/ProductionForm.tsx:3:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/production/ProductionForm.tsx:41:  const { t, language } = useTranslation()
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:7:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:10:import { hasFeatureAccess } from '../../utils/planAccess'
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:19:  if (axios.isAxiosError<FarmApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:27:    if (status === 401) {
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:41:  const { t } = useTranslation()
frontend/web/farm_web/src/layout/AppLayout.tsx:7:import { useLanguage, type Language } from '../context/LanguageContext'
frontend/web/farm_web/src/layout/AppLayout.tsx:8:import { useTranslation } from '../hooks/useTranslation'
frontend/web/farm_web/src/layout/AppLayout.tsx:9:import { getCurrentPlanMetadata, getFeatureAccessState, type AppFeature } from '../utils/planAccess'
frontend/web/farm_web/src/layout/AppLayout.tsx:53:  const { t } = useTranslation()
frontend/web/farm_web/src/layout/AppLayout.tsx:119:                    <span className="app-layout__nav-badge">{t('plan.badge')}</span>
frontend/web/farm_web/src/layout/AppLayout.tsx:174:          <div className="app-layout__language-switcher" role="group" aria-label={t('layout.languageLabel')}>
frontend/web/farm_web/src/layout/AppLayout.tsx:243:            <div className="app-layout__session-plan">
frontend/web/farm_web/src/layout/AppLayout.tsx:244:              <span className="app-layout__language-label">{t('plan.currentLabel')}</span>
frontend/web/farm_web/src/layout/AppLayout.tsx:246:                className={`app-layout__plan-badge${currentPlanMetadata.paid ? ' app-layout__plan-badge--premium' : ''}`}
frontend/web/farm_web/src/components/feeding/FeedingForm.tsx:3:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/components/feeding/FeedingForm.tsx:48:  const { t, language } = useTranslation()
frontend/web/farm_web/src/i18n/en.ts:80:  plan: {
frontend/web/farm_web/src/i18n/en.ts:83:    description: 'The free plan keeps the basic operational workflows available, while advanced capabilities stay on the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:85:    currentLabel: 'Current plan',
frontend/web/farm_web/src/i18n/en.ts:90:    upgradeHint: 'Upgrade the account plan to unlock this feature without changing the rest of the workflow.',
frontend/web/farm_web/src/i18n/en.ts:94:        description: 'The consolidated financial and operational dashboard is part of the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:98:        description: 'The analytical time series and visual comparisons require the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:102:        description: 'Data and report exports in CSV are available only on the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:650:    createDescription: 'Define the role, login status, password, and farms assigned to the new user.',
frontend/web/farm_web/src/i18n/en.ts:651:    updateDescription: 'Adjust role, contact details, and farm access for the selected user.',
frontend/web/farm_web/src/i18n/en.ts:676:      role: 'Role',
frontend/web/farm_web/src/i18n/en.ts:685:      roleLabel: 'Role',
frontend/web/farm_web/src/i18n/en.ts:687:      allRoles: 'All roles',
frontend/web/farm_web/src/i18n/en.ts:696:      role: 'Role',
frontend/web/farm_web/src/i18n/en.ts:709:      selectRole: 'Select a role',
frontend/web/farm_web/src/i18n/en.ts:717:    roles: {
frontend/web/farm_web/src/i18n/en.ts:718:      MANAGER: 'Manager',
frontend/web/farm_web/src/i18n/en.ts:719:      WORKER: 'Worker',
frontend/web/farm_web/src/i18n/en.ts:743:      roleRequired: 'Role is required.',
frontend/web/farm_web/src/i18n/domainLabels.ts:6:const USER_ROLES: UserRole[] = ['MANAGER', 'WORKER']
frontend/web/farm_web/src/i18n/domainLabels.ts:16:export function getUserRoleLabel(t: Translate, role: string) {
frontend/web/farm_web/src/i18n/domainLabels.ts:17:  return hasValue(USER_ROLES, role) ? t(`accessControl.roles.${role}`) : role
frontend/web/farm_web/src/services/feedTypeService.ts:4:import type { CurrencyCode } from '../context/CurrencyContext'
frontend/web/farm_web/src/i18n/pt-BR.ts:80:  plan: {
frontend/web/farm_web/src/i18n/pt-BR.ts:83:    description: 'O plano gratuito mantém o uso operacional básico, enquanto os recursos avançados ficam reservados ao plano Premium.',
frontend/web/farm_web/src/i18n/pt-BR.ts:90:    upgradeHint: 'Atualize o plano da conta para liberar este recurso sem alterar o restante do seu fluxo.',
frontend/web/farm_web/src/i18n/pt-BR.ts:94:        description: 'O painel consolidado com indicadores financeiros e operacionais faz parte do plano Premium.',
frontend/web/farm_web/src/i18n/pt-BR.ts:98:        description: 'As séries analíticas e comparativos visuais exigem o plano Premium.',
frontend/web/farm_web/src/i18n/pt-BR.ts:102:        description: 'A exportação de dados e relatórios em CSV está disponível apenas no plano Premium.',
frontend/web/farm_web/src/i18n/pt-BR.ts:224:    eyebrow: 'Controle do Rebanho',
frontend/web/farm_web/src/i18n/pt-BR.ts:328:    eyebrow: 'Controle de produção',
frontend/web/farm_web/src/i18n/pt-BR.ts:405:    eyebrow: 'Controle de preços',
frontend/web/farm_web/src/i18n/pt-BR.ts:452:    eyebrow: 'Controle de alimentação',
frontend/web/farm_web/src/i18n/pt-BR.ts:534:    eyebrow: 'Controle de ração',
frontend/web/farm_web/src/i18n/pt-BR.ts:588:    eyebrow: 'Controle de lotes',
frontend/web/farm_web/src/i18n/pt-BR.ts:645:    eyebrow: 'Controle de Acesso',
frontend/web/farm_web/src/i18n/pt-BR.ts:676:      role: 'Função',
frontend/web/farm_web/src/i18n/pt-BR.ts:685:      roleLabel: 'Função',
frontend/web/farm_web/src/i18n/pt-BR.ts:696:      role: 'Função',
frontend/web/farm_web/src/i18n/pt-BR.ts:717:    roles: {
frontend/web/farm_web/src/i18n/pt-BR.ts:718:      MANAGER: 'Gerente',
frontend/web/farm_web/src/i18n/pt-BR.ts:719:      WORKER: 'Trabalhador',
frontend/web/farm_web/src/i18n/pt-BR.ts:743:      roleRequired: 'Função é obrigatória.',
frontend/web/farm_web/src/services/api.ts:1:import axios from 'axios'
frontend/web/farm_web/src/services/api.ts:19:  if (!axios.isAxiosError(error)) {
frontend/web/farm_web/src/services/api.ts:27:  return hasStoredToken && !isLoginRequest && error.response?.status === 401
frontend/web/farm_web/src/services/api.ts:34:const api = axios.create({
frontend/web/farm_web/src/services/analyticsService.ts:3:import type { CurrencyCode } from '../context/CurrencyContext'
frontend/web/farm_web/src/services/milkPriceService.ts:4:import type { CurrencyCode } from '../context/CurrencyContext'
frontend/web/farm_web/src/services/authStorage.ts:32:      role?: UserRole
frontend/web/farm_web/src/services/authStorage.ts:35:      plan?: UserPlan
frontend/web/farm_web/src/services/authStorage.ts:42:      typeof parsedUser.role !== 'string' ||
frontend/web/farm_web/src/services/authStorage.ts:53:      plan: parsedUser.plan ?? 'FREE',
frontend/web/farm_web/src/services/userService.ts:11:    ...(filters?.role ? { role: filters.role } : {}),
frontend/web/farm_web/src/services/userService.ts:42:    role: data.role,
frontend/web/farm_web/src/services/userService.ts:59:    role: data.role,
frontend/web/farm_web/src/services/animalService.ts:4:import type { CurrencyCode } from '../context/CurrencyContext'
frontend/web/farm_web/src/pages/batch/AnimalBatchPage.tsx:2:import axios from 'axios'
frontend/web/farm_web/src/pages/batch/AnimalBatchPage.tsx:9:import { useTranslation } from '../../hooks/useTranslation'
frontend/web/farm_web/src/pages/batch/AnimalBatchPage.tsx:40:  if (axios.isAxiosError<AnimalBatchApiErrorResponse>(error)) {
frontend/web/farm_web/src/pages/batch/AnimalBatchPage.tsx:61:  const { t } = useTranslation()
frontend/web/farm_web/src/services/dashboardService.ts:3:import type { CurrencyCode } from '../context/CurrencyContext'

 succeeded in 0ms:
frontend/web/farm_web/package.json
frontend/web/farm_web/tests/app-layout-navigation.test.mjs
frontend/web/farm_web/tests/i18n-catalog.test.mjs
frontend/web/farm_web/tests/auth.service.test.mjs
frontend/web/farm_web/tests/plan-access-ui.test.mjs
frontend/web/farm_web/tests/api.unauthorized.test.mjs
frontend/web/farm_web/tests/export-context.service.test.mjs
frontend/web/farm_web/vite.config.ts
frontend/web/farm_web/src/components/user/UserForm.tsx
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx
frontend/web/farm_web/src/components/common/FeedbackToast.tsx
frontend/web/farm_web/src/components/common/FilterFields.tsx
frontend/web/farm_web/src/components/common/PaginationControls.tsx
frontend/web/farm_web/src/components/common/ListingFiltersBar.tsx
frontend/web/farm_web/src/components/common/FeedbackToast.css
backend/farmapp/pom.xml
backend/farmapp/src/main/resources/application-dev.properties
backend/farmapp/src/main/resources/db/migration/V1__baseline_schema.sql
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql
backend/farmapp/src/main/resources/application-local.properties
backend/farmapp/src/main/resources/application.properties
frontend/web/farm_web/src/components/analytics/ChartErrorBoundary.tsx
frontend/web/farm_web/src/components/analytics/BarChart.tsx
frontend/web/farm_web/src/components/analytics/LineChart.tsx
frontend/web/farm_web/src/components/feed-type/FeedTypeForm.tsx
frontend/web/farm_web/src/components/batch/AnimalBatchForm.tsx
frontend/web/farm_web/src/components/animal/AnimalForm.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/package-info.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionProfitResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionSummaryResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/UpdateProductionRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java
frontend/web/farm_web/src/components/feeding/FeedingForm.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/entity/ProductionEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/mapper/ProductionMapper.java
backend/farmapp/src/test/resources/mockito-extensions/org.mockito.plugins.MockMaker
backend/farmapp/src/test/resources/application.properties
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/repository/ProductionRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdatePasswordRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/ActivateUserRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/package-info.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/FarmAppApplication.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserPlan.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java
frontend/web/farm_web/src/components/dashboard/StatCard.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailConfiguration.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java
frontend/web/farm_web/src/components/dashboard/StatCard.css
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/TokenService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java
frontend/web/farm_web/src/components/production/ProductionForm.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java
frontend/web/farm_web/src/i18n/en.ts
frontend/web/farm_web/src/i18n/pt-BR.ts
frontend/web/farm_web/src/i18n/domainLabels.ts
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/model/AuthenticatedUser.java
frontend/web/farm_web/src/index.css
frontend/web/farm_web/src/components/auth/ManagerRoute.tsx
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx
frontend/web/farm_web/src/components/auth/PlanRoute.tsx
frontend/web/farm_web/src/components/farm/FarmForm.tsx
backend/farmapp/src/test/java/com/jpsoftware/farmapp/FarmAppApplicationTests.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java
frontend/web/farm_web/src/layout/AppLayout.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/shared/util/CsvExportUtilsTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandlerTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/dto/FarmResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslatorTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/shared/exception/LocalizedErrorAttributesTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/fixture/AnimalFixture.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/dto/CreateFarmRequest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/config/FlywayPostgreSqlSupportClasspathTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/config/FlywaySchemaValidationIntegrationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/config/LocalProfilePropertiesTest.java
frontend/web/farm_web/src/services/api.ts
frontend/web/farm_web/src/services/csvExportService.ts
frontend/web/farm_web/src/services/farmService.ts
frontend/web/farm_web/src/services/animalService.ts
frontend/web/farm_web/src/services/feedingService.ts
frontend/web/farm_web/src/services/farmStorage.ts
frontend/web/farm_web/src/services/userService.ts
frontend/web/farm_web/src/services/feedbackService.ts
frontend/web/farm_web/src/services/analyticsService.ts
frontend/web/farm_web/src/services/milkPriceService.ts
backend/farmapp/src/test/java/com/jpsoftware/farmapp/fixture/FeedingFixture.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/fixture/ProductionFixture.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/entity/FarmEntity.java
frontend/web/farm_web/src/services/authService.ts
frontend/web/farm_web/src/services/dashboardService.ts
frontend/web/farm_web/src/services/authStorage.ts
frontend/web/farm_web/src/services/animalBatchService.ts
frontend/web/farm_web/src/services/feedTypeService.ts
frontend/web/farm_web/src/services/productionService.ts
frontend/web/farm_web/src/App.css
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/animal/AnimalIntegrationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/base/BaseIntegrationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/animalbatch/AnimalBatchControllerContractTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/feeding/FeedingIntegrationTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/dto/FeedTypeSummaryResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/dto/CreateFeedTypeRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/dto/FeedTypeResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/repository/FarmRepository.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/production/ProductionIntegrationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/entity/FeedTypeEntity.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserMapperTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/controller/FarmController.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/mapper/FeedTypeMapper.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/feed/FeedTypeControllerContractTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/feed/FeedTypeServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/repository/FeedTypeRepository.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/farm/FarmOnboardingAccessIntegrationTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/analytics/AnalyticsServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/analytics/AnalyticsExportControllerContractTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/animal/AnimalServiceTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/milkprice/MilkPriceControllerContractTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/milkprice/MilkPriceExportControllerContractTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/animalbatch/AnimalBatchIntegrationTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/dto/DashboardResponse.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/animal/AnimalControllerContractTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/animal/AnimalExportControllerContractTest.java
frontend/web/farm_web/src/types/production.ts
frontend/web/farm_web/src/types/feeding.ts
frontend/web/farm_web/src/types/dashboard.ts
frontend/web/farm_web/src/types/milkPrice.ts
frontend/web/farm_web/src/types/user.ts
frontend/web/farm_web/src/types/animalBatch.ts
frontend/web/farm_web/src/types/animal.ts
frontend/web/farm_web/src/types/farm.ts
frontend/web/farm_web/src/types/feedType.ts
frontend/web/farm_web/src/types/analytics.ts
frontend/web/farm_web/src/types/pagination.ts
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/feeding/FeedingControllerContractTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsProfitPointResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsAnimalProductionPointResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsGroupBy.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsTimeSeriesPointResponse.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/feeding/FeedingServiceTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/production/ProductionControllerContractTest.java
frontend/web/farm_web/src/hooks/useTranslation.ts
frontend/web/farm_web/src/hooks/useAutoAppliedFilters.ts
frontend/web/farm_web/src/hooks/useFeedback.ts
frontend/web/farm_web/src/hooks/useCurrency.ts
frontend/web/farm_web/src/hooks/useAuth.ts
frontend/web/farm_web/src/hooks/useFarm.ts
frontend/web/farm_web/src/hooks/useMeasurementUnits.ts
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/dashboard/DashboardControllerContractTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/dashboard/DashboardExportControllerContractTest.java
frontend/web/farm_web/src/utils/currency.ts
frontend/web/farm_web/src/utils/avatar.ts
frontend/web/farm_web/src/utils/planAccess.ts
frontend/web/farm_web/src/utils/authorization.ts
frontend/web/farm_web/src/utils/decimal.ts
frontend/web/farm_web/src/utils/format.ts
frontend/web/farm_web/src/utils/measurementUnits.ts
frontend/web/farm_web/src/utils/pagination.ts
frontend/web/farm_web/src/App.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/shared/plan/PlanAccessPolicyTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/dto/MilkPriceResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/dto/CreateMilkPriceRequest.java
frontend/web/farm_web/src/assets/vite.svg
frontend/web/farm_web/src/assets/react.svg
frontend/web/farm_web/src/assets/hero.png
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/shared/payment/PaymentRecordTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/entity/MilkPriceEntity.java
frontend/web/farm_web/src/context/feedbackContext.ts
frontend/web/farm_web/src/context/farmContext.ts
frontend/web/farm_web/src/context/FarmContext.tsx
frontend/web/farm_web/src/context/LanguageContext.tsx
frontend/web/farm_web/src/context/authContext.ts
frontend/web/farm_web/src/context/MeasurementUnitContext.tsx
frontend/web/farm_web/src/context/CurrencyContext.tsx
frontend/web/farm_web/src/context/AuthContext.tsx
frontend/web/farm_web/src/context/FeedbackContext.tsx
frontend/web/farm_web/src/main.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/shared/config/DefaultAdminInitializerTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/repository/MilkPriceRepository.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/production/ProductionServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/SellAnimalRequest.java
frontend/web/farm_web/src/pages/feeding/FeedingPage.tsx
frontend/web/farm_web/src/pages/milk-price/MilkPricePage.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/UpdateAnimalRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/AnimalResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/AnimalSummaryResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/CreateAnimalRequest.java
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx
frontend/web/farm_web/src/pages/farm/FarmCreatePage.tsx
frontend/web/farm_web/src/pages/animals/AnimalsPage.tsx
frontend/web/farm_web/src/pages/animals/AnimalDetailsPage.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/entity/AnimalEntity.java
frontend/web/farm_web/src/pages/production/ProductionPage.tsx
frontend/web/farm_web/src/pages/login/LoginPage.tsx
frontend/web/farm_web/src/pages/analytics/AnalyticsPage.tsx
frontend/web/farm_web/src/pages/users/UsersPage.tsx
frontend/web/farm_web/src/pages/batch/AnimalBatchPage.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/mapper/AnimalMapper.java
frontend/web/farm_web/src/pages/dashboard/DashboardPage.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/repository/AnimalRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java
frontend/web/farm_web/src/pages/feed-type/FeedTypePage.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/mapper/AnimalBatchMapper.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/repository/AnimalBatchRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/repository/AnimalBatchMemberRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingRequiredException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/service/AnimalBatchService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/entity/AnimalBatchEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/entity/AnimalBatchMemberEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/dto/MessageResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/dto/package-info.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/dto/PaginatedResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/CreateAnimalBatchRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/AnimalBatchResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/UpdateAnimalBatchRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessDecision.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/dashboard/DashboardServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlementResolver.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanActivationStatus.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessDeniedException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanActivationSource.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/util/CsvColumn.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/util/CsvExportUtils.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/util/DecimalScaleUtils.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/util/CsvResponseFactory.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/RequiresPlanFeature.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/payment/PaymentStatus.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/entity/FeedingEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/mapper/FeedingMapper.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/controller/AnimalBatchController.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/payment/PaymentRecord.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/payment/PaymentProvider.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/JwtTokenServiceTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/UpdateFeedingRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/FeedingResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/OpenApiConfig.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/FarmOnboardingAccessConfig.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/CorsConfig.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/PlanAccessConfig.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/measurement/MeasurementUnitConverter.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/measurement/MeasurementUnit.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/BusinessException.java
frontend/web/farm_web/src/pages/settings/SettingsPage.tsx
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailConfirmationRequiredException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/model/EmailMessage.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/InvalidCredentialsException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/currency/CurrencyCode.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/currency/CurrencyConversionUtils.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/LocalizedErrorAttributes.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ValidationException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailDispatchException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ResourceNotFoundException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ConflictException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/repository/FeedingRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java

exec
/bin/bash -lc 'rg -n "plan|pricing|subscription|feature|role|MANAGER|WORKER|auth|user|dto|enum|security|access" backend/farmapp/src/main/java' in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:1:package com.jpsoftware.farmapp.user.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:3:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:5:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:11:import com.jpsoftware.farmapp.user.dto.ActivateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:12:import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:13:import com.jpsoftware.farmapp.user.dto.UpdatePasswordRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:14:import com.jpsoftware.farmapp.user.dto.UpdateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:15:import com.jpsoftware.farmapp.user.dto.UserResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:16:import com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:17:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:18:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:19:import com.jpsoftware.farmapp.user.mapper.UserMapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:20:import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:21:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:31:import org.springframework.security.crypto.password.PasswordEncoder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:44:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:45:    private final UserFarmAssignmentRepository userFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:47:    private final UserMapper userMapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:49:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:52:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:53:            UserFarmAssignmentRepository userFarmAssignmentRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:55:            UserMapper userMapper,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:57:            AuthenticationContextService authenticationContextService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:58:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:59:        this.userFarmAssignmentRepository = userFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:61:        this.userMapper = userMapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:63:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:71:        UUID creatorId = authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:82:        UserEntity userEntity = userMapper.toEntity(request);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:83:        userEntity.setName(request.getName().trim());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:84:        userEntity.setEmail(normalizeEmail(request.getEmail()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:85:        userEntity.setRole(normalizeRole(request.getRole()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:86:        userEntity.setActive(Boolean.TRUE.equals(request.getActive()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:87:        userEntity.setEmailConfirmed(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:88:        userEntity.setAvatarUrl(normalizeAvatarUrl(request.getAvatarUrl()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:89:        userEntity.setPlan(UserPlan.defaultPlan());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:90:        userEntity.setPassword(passwordEncoder.encode(resolveRawPassword(request)));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:91:        UserEntity savedUser = userRepository.save(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:98:    public List<UserResponse> findAll(String search, Boolean active, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:100:        String normalizedRole = normalizeFilterRole(role);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:102:        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "name", "email")).stream()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:103:                .filter(userEntity -> matchesSearch(userEntity, normalizedSearch))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:104:                .filter(userEntity -> matchesActive(userEntity, active))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:105:                .filter(userEntity -> matchesRole(userEntity, normalizedRole))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:111:    public PaginatedResponse<UserResponse> findAllPaginated(String search, Boolean active, String role, int page, int size) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:112:        Page<UserEntity> users = userRepository.findAll(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:113:                buildUserSpecification(search, active, role),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:115:        Page<UserResponse> responses = users.map(this::buildUserResponse);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:125:    public String exportAll(String search, Boolean active, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:126:        return CsvExportUtils.write(findAll(search, active, role), List.of(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:127:                new CsvColumn<>("id", user -> user.getId() != null ? user.getId().toString() : null),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:130:                new CsvColumn<>("role", UserResponse::getRole),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:133:                new CsvColumn<>("farmIds", user -> user.getFarmIds() != null ? String.join(";", user.getFarmIds()) : "")));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:138:        UserEntity userEntity = findUserEntity(validateId(id));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:140:        return buildUserResponse(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:148:        UUID creatorId = authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:150:        UUID userId = validateId(id);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:151:        UserEntity userEntity = findUserEntity(userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:158:                userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:159:        ensureFarmOwnerRemainsActiveManager(userEntity, normalizeRole(request.getRole()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:161:        userEntity.setName(request.getName().trim());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:162:        userEntity.setEmail(normalizeEmail(request.getEmail()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:163:        userEntity.setRole(normalizeRole(request.getRole()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:164:        userEntity.setAvatarUrl(normalizeAvatarUrl(request.getAvatarUrl()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:165:        UserEntity savedUser = userRepository.save(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:173:        UserEntity userEntity = findUserEntity(validateId(id));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:174:        ensureUserDoesNotOwnFarms(userEntity.getId(), "Cannot inactivate user who owns farms");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:176:        userEntity.setActive(false);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:177:        return buildUserResponse(userRepository.save(userEntity));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:182:        UserEntity userEntity = findUserEntity(validateId(id));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:184:        userEntity.setActive(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:186:            userEntity.setPassword(passwordEncoder.encode(request.getPassword().trim()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:189:        return buildUserResponse(userRepository.save(userEntity));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:194:        UserEntity userEntity = findUserEntity(validateId(id));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:195:        ensureUserDoesNotOwnFarms(userEntity.getId(), "Cannot delete user who owns farms");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:197:        userFarmAssignmentRepository.deleteByUserId(userEntity.getId());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:198:        userRepository.delete(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:213:        UUID authenticatedUserId = authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:214:                .orElseThrow(() -> new ValidationException("Authenticated user is required"));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:215:        UserEntity userEntity = findUserEntity(authenticatedUserId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:216:        if (!passwordEncoder.matches(request.getCurrentPassword(), userEntity.getPassword())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:223:        userEntity.setPassword(passwordEncoder.encode(request.getNewPassword()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:224:        userRepository.save(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:234:    private void persistFarmAssignments(UUID userId, LinkedHashSet<String> farmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:236:                userFarmAssignmentRepository.save(new UserFarmAssignmentEntity(null, userId, farmId)));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:239:    private void replaceFarmAssignments(UUID userId, LinkedHashSet<String> farmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:240:        userFarmAssignmentRepository.deleteByUserId(userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:241:        userFarmAssignmentRepository.flush();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:242:        persistFarmAssignments(userId, farmIds);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:245:    private UserResponse buildUserResponse(UserEntity userEntity) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:246:        List<String> farmIds = userFarmAssignmentRepository.findByUserId(userEntity.getId()).stream()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:249:        return userMapper.toResponse(userEntity, farmIds);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:255:            String role,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:261:        validateRequiredText(role, "role must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:270:            throw new ValidationException("farmIds must reference farms owned by the authenticated manager");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:274:        userRepository.findByEmail(normalizedEmail)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:275:                .filter(user -> existingUserId == null || !user.getId().equals(existingUserId))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:305:    private void ensureFarmOwnerRemainsActiveManager(UserEntity userEntity, String nextRole) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:306:        if (!ownsAnyFarm(userEntity.getId())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:309:        if (!"MANAGER".equals(nextRole)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:312:        if (!userEntity.isActive()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:313:            throw new ConflictException("Inactive user who owns farms cannot be updated");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:317:    private void ensureUserDoesNotOwnFarms(UUID userId, String message) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:318:        if (ownsAnyFarm(userId)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:323:    private boolean ownsAnyFarm(UUID userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:324:        return !farmRepository.findByOwnerId(userId).isEmpty();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:327:    private UserEntity findUserEntity(UUID userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:328:        return userRepository.findById(userId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:338:    private boolean matchesSearch(UserEntity userEntity, String normalizedSearch) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:343:        return userEntity.getName().toLowerCase(Locale.ROOT).contains(normalizedSearch)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:344:                || userEntity.getEmail().toLowerCase(Locale.ROOT).contains(normalizedSearch);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:347:    private boolean matchesActive(UserEntity userEntity, Boolean active) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:348:        return active == null || userEntity.isActive() == active;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:351:    private boolean matchesRole(UserEntity userEntity, String normalizedRole) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:353:                || normalizedRole.equalsIgnoreCase(userEntity.getRole());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:356:    private Specification<UserEntity> buildUserSpecification(String search, Boolean active, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:358:        String normalizedRole = normalizeFilterRole(role);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:378:                    criteriaBuilder.equal(criteriaBuilder.upper(root.get("role")), normalizedRole));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:391:    private String normalizeFilterRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:392:        if (!StringUtils.hasText(role)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:395:        return normalizeRole(role);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:402:    private String normalizeRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:403:        return role.trim().toUpperCase(Locale.ROOT);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdatePasswordRequest.java:1:package com.jpsoftware.farmapp.user.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdatePasswordRequest.java:6:@Schema(description = "Request payload for updating the authenticated user's password.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdatePasswordRequest.java:10:    @Schema(description = "Current user password.", example = "farmapp@123")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdatePasswordRequest.java:14:    @Schema(description = "New user password.", example = "farmapp@456")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:1:package com.jpsoftware.farmapp.user.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:8:@Schema(description = "Request payload for updating a user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:19:    @NotBlank(message = "role must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:20:    @Schema(description = "User role in the system.", example = "MANAGER")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:21:    private String role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:29:    @Schema(description = "Farm identifiers assigned to the user.", example = "[\"farm-001\"]")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:35:    public UpdateUserRequest(String name, String email, String role, String avatarUrl, List<String> farmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:38:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:60:        return role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:63:    public void setRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:64:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/ActivateUserRequest.java:1:package com.jpsoftware.farmapp.user.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/ActivateUserRequest.java:5:@Schema(description = "Request payload for activating a user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/ActivateUserRequest.java:9:            description = "Optional password to set when reactivating the user.",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:1:package com.jpsoftware.farmapp.user.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:19:    @Schema(description = "User role in the system.", example = "MANAGER")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:20:    private String role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:22:    @Schema(description = "Whether the user can authenticate.", example = "true")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:30:    @Schema(description = "User plan.", example = "FREE")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:31:    private String plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:33:    @Schema(description = "Farm identifiers assigned to the user.", example = "[\"farm-001\"]")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:39:    public UserResponse(UUID id, String name, String email, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:40:        this(id, name, email, role, null, null, null, null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:43:    public UserResponse(UUID id, String name, String email, String role, Boolean active, String avatarUrl, List<String> farmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:44:        this(id, name, email, role, active, avatarUrl, null, farmIds);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:51:            String role,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:54:            String plan,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:59:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:62:        this.plan = plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:91:        return role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:94:    public void setRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:95:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:115:        return plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:118:    public void setPlan(String plan) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:119:        this.plan = plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:1:package com.jpsoftware.farmapp.user.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:9:@Schema(description = "Request payload for creating a user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:20:    @NotBlank(message = "role must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:21:    @Schema(description = "User role in the system.", example = "MANAGER")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:22:    private String role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:24:    @Schema(description = "User password for authentication.", example = "farmapp@123")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:28:    @Schema(description = "Whether the user can authenticate.", example = "true")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:37:    @Schema(description = "Farm identifiers assigned to the user.", example = "[\"farm-001\"]")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:43:    public CreateUserRequest(String name, String email, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:44:        this(name, email, role, null, true, null, List.of());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:47:    public CreateUserRequest(String name, String email, String role, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:48:        this(name, email, role, password, true, null, List.of());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:54:            String role,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:61:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:85:        return role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:88:    public void setRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:89:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserPlan.java:1:package com.jpsoftware.farmapp.user.entity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserPlan.java:3:public enum UserPlan {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:1:package com.jpsoftware.farmapp.user.entity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:15:@Table(name = "users")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:30:    private String role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:52:    private UserPlan plan = UserPlan.defaultPlan();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:57:    public UserEntity(UUID id, String name, String email, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:58:        this(id, name, email, role, "", true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:61:    public UserEntity(UUID id, String name, String email, String role, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:62:        this(id, name, email, role, password, true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:65:    public UserEntity(UUID id, String name, String email, String role, String password, boolean active) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:69:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:72:        this.plan = UserPlan.defaultPlan();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:100:        return role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:103:    public void setRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:104:        this.role = role;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:156:        return plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:159:    public void setPlan(UserPlan plan) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:160:        this.plan = plan == null ? UserPlan.defaultPlan() : plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:1:package com.jpsoftware.farmapp.user.entity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:14:        name = "user_farm_assignments",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:15:        uniqueConstraints = @UniqueConstraint(columnNames = {"user_id", "farm_id"}))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:23:    @Column(name = "user_id", nullable = false)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:24:    private UUID userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:32:    public UserFarmAssignmentEntity(UUID id, UUID userId, String farmId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:34:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:47:        return userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:50:    public void setUserId(UUID userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java:51:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:1:package com.jpsoftware.farmapp.user.mapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:3:import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:4:import com.jpsoftware.farmapp.user.dto.UserResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:5:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:6:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:14:        UserEntity userEntity = new UserEntity();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:15:        userEntity.setName(request.getName());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:16:        userEntity.setEmail(request.getEmail());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:17:        userEntity.setRole(request.getRole());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:18:        userEntity.setAvatarUrl(request.getAvatarUrl());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:19:        return userEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserRepository.java:1:package com.jpsoftware.farmapp.user.repository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserRepository.java:3:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java:1:package com.jpsoftware.farmapp.user.repository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java:3:import com.jpsoftware.farmapp.user.entity.UserFarmAssignmentEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java:10:    boolean existsByUserIdAndFarmId(UUID userId, String farmId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java:12:    boolean existsByUserId(UUID userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java:14:    List<UserFarmAssignmentEntity> findByUserId(UUID userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java:16:    void deleteByUserId(UUID userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:1:package com.jpsoftware.farmapp.user.controller;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:3:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:5:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:6:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:8:import com.jpsoftware.farmapp.user.dto.ActivateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:9:import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:10:import com.jpsoftware.farmapp.user.dto.UpdatePasswordRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:11:import com.jpsoftware.farmapp.user.dto.UpdateUserRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:12:import com.jpsoftware.farmapp.user.dto.UserResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:13:import com.jpsoftware.farmapp.user.service.UserService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:36:@RequestMapping("/users")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:37:@Tag(name = "Users", description = "Operations for managing application users.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:40:    private final UserService userService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:42:    public UserController(UserService userService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:43:        this.userService = userService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:47:    @Operation(summary = "Create user", description = "Creates a new application user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:54:        UserResponse response = userService.create(request);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:59:    @Operation(summary = "List users", description = "Returns all registered users.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:66:            @RequestParam(required = false) String role,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:70:            PaginatedResponse<UserResponse> response = userService.findAllPaginated(search, active, role, page, size);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:74:        List<UserResponse> response = userService.findAll(search, active, role);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:80:    @Operation(summary = "Export users", description = "Exports users as CSV using the current search and filters.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:84:            @RequestParam(required = false) String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:85:        return CsvResponseFactory.buildDownload("users.csv", userService.exportAll(search, active, role));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:89:    @Operation(summary = "Get user by id", description = "Returns a user by its identifier.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:92:            @ApiResponse(responseCode = "400", description = "Invalid user identifier",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:98:        UserResponse response = userService.findById(id);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:103:    @Operation(summary = "Update user", description = "Updates an existing application user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:114:        UserResponse response = userService.update(id, request);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:119:    @Operation(summary = "Inactivate user", description = "Marks a user as inactive and blocks further authentication.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:128:        UserResponse response = userService.inactivate(id);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:133:    @Operation(summary = "Activate user", description = "Marks a user as active and optionally updates the password.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:140:        UserResponse response = userService.activate(id, request);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:145:    @Operation(summary = "Delete user", description = "Deletes a user and its farm assignments.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:154:        userService.delete(id);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:159:    @Operation(summary = "Update own password", description = "Updates the authenticated user's password.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:168:        userService.updateOwnPassword(request);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:3:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:16:    private static final String UPDATE_PASSWORD_PATH = "/users/me/password";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:17:    private static final String REQUIRED_MESSAGE = "Create a farm before accessing this feature";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:19:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:23:            AuthenticationContextService authenticationContextService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:25:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:39:        UUID authenticatedUserId = authenticationContextService.getAuthenticatedUserId().orElse(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/onboarding/FarmOnboardingAccessInterceptor.java:40:        if (authenticatedUserId == null || farmService.hasAccessibleFarm(authenticatedUserId)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:3:import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:4:import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:10:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:222:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:231:        } else if (accessibleFarmIds != null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:232:            specification = accessibleFarmIds.isEmpty()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/service/FeedTypeService.java:234:                    : specification.and((root, query, criteriaBuilder) -> root.get("farmId").in(accessibleFarmIds));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/RequiresPlanFeature.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/dto/FeedTypeSummaryResponse.java:1:package com.jpsoftware.farmapp.feed.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/dto/CreateFeedTypeRequest.java:1:package com.jpsoftware.farmapp.feed.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:3:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:4:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:15:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:16:    private final PlanAccessPolicy planAccessPolicy;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:19:            AuthenticationContextService authenticationContextService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:20:            PlanAccessPolicy planAccessPolicy) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:21:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:22:        this.planAccessPolicy = planAccessPolicy;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:44:        UserPlan userPlan = authenticationContextService.getAuthenticatedUser()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:45:                .map(authenticatedUser -> authenticatedUser.plan() == null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:47:                        : authenticatedUser.plan())
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:50:        planAccessPolicy.assertHasAccess(userPlan, requiresPlanFeature.value());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/dto/FeedTypeResponse.java:1:package com.jpsoftware.farmapp.feed.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java:3:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java:5:public enum PlanFeature {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java:20:    public boolean isAvailableFor(UserPlan plan) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java:21:        UserPlan resolvedPlan = plan == null ? UserPlan.defaultPlan() : plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanActivationSource.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanActivationSource.java:3:public enum PlanActivationSource {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessDeniedException.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/dto/MessageResponse.java:1:package com.jpsoftware.farmapp.shared.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlementResolver.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlementResolver.java:3:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlementResolver.java:9:    public PlanEntitlement resolve(UserPlan plan) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlementResolver.java:10:        return PlanEntitlement.internallyActivated(plan == null ? UserPlan.defaultPlan() : plan);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/dto/package-info.java:1:package com.jpsoftware.farmapp.shared.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanActivationStatus.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanActivationStatus.java:3:public enum PlanActivationStatus {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/dto/PaginatedResponse.java:1:package com.jpsoftware.farmapp.shared.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:3:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:9:    private final PlanEntitlementResolver planEntitlementResolver;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:11:    public PlanAccessPolicy(PlanEntitlementResolver planEntitlementResolver) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:12:        this.planEntitlementResolver = planEntitlementResolver;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:15:    public PlanAccessDecision evaluate(UserPlan plan, PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:16:        return evaluate(planEntitlementResolver.resolve(plan), feature);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:19:    public PlanAccessDecision evaluate(PlanEntitlement entitlement, PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:23:        if (feature == null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:25:                    resolvedEntitlement.plan(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:34:                resolvedEntitlement.plan(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:37:                feature,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:38:                feature.getMinimumPlan(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:39:                feature.isAvailableFor(resolvedEntitlement));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:42:    public boolean hasAccess(UserPlan plan, PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:43:        return evaluate(plan, feature).allowed();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:46:    public boolean hasAccess(PlanEntitlement entitlement, PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:47:        return evaluate(entitlement, feature).allowed();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:50:    public void assertHasAccess(UserPlan plan, PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:51:        assertHasAccess(planEntitlementResolver.resolve(plan), feature);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:54:    public void assertHasAccess(PlanEntitlement entitlement, PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:55:        PlanAccessDecision decision = evaluate(entitlement, feature);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessPolicy.java:57:            throw new PlanAccessDeniedException("This feature requires the PRO plan");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessDecision.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessDecision.java:3:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanAccessDecision.java:9:        PlanFeature feature,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:1:package com.jpsoftware.farmapp.shared.plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:3:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:6:        UserPlan plan,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:12:        plan = plan == null ? UserPlan.defaultPlan() : plan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:21:    public static PlanEntitlement internallyActivated(UserPlan plan) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:22:        return new PlanEntitlement(plan, PlanActivationStatus.ACTIVE, PlanActivationSource.INTERNAL_DEFAULT, null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:25:    public static PlanEntitlement pendingExternalConfirmation(UserPlan plan, String externalReference) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:27:                plan,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:33:    public static PlanEntitlement canceled(UserPlan plan, String externalReference) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:34:        return new PlanEntitlement(plan, PlanActivationStatus.CANCELED, PlanActivationSource.EXTERNAL_PROVIDER, externalReference);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:42:        return plan.includes(requiredPlan);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:45:    public boolean allows(PlanFeature feature) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlement.java:46:        return feature == null || (isActive() && includes(feature.getMinimumPlan()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/payment/PaymentStatus.java:3:public enum PaymentStatus {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/payment/PaymentProvider.java:3:public enum PaymentProvider {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/OpenApiConfig.java:18:                        .description("REST API for farm management operations, including animals, feeding, production, users, feed types, and dashboard metrics.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:18:import com.jpsoftware.farmapp.shared.plan.PlanAccessDeniedException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:5:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:6:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:7:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:14:import org.springframework.security.crypto.password.PasswordEncoder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:21:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:28:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:33:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:43:        if (userRepository.count() > 0) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:50:        admin.setRole("MANAGER");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:56:        userRepository.save(admin);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:58:        logger.info("Default admin user created");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:3:import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:4:import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:6:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:8:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:9:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/PlanAccessConfig.java:3:import com.jpsoftware.farmapp.shared.plan.PlanFeatureAccessInterceptor;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/PlanAccessConfig.java:11:    private final PlanFeatureAccessInterceptor planFeatureAccessInterceptor;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/PlanAccessConfig.java:13:    public PlanAccessConfig(PlanFeatureAccessInterceptor planFeatureAccessInterceptor) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/PlanAccessConfig.java:14:        this.planFeatureAccessInterceptor = planFeatureAccessInterceptor;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/PlanAccessConfig.java:19:        registry.addInterceptor(planFeatureAccessInterceptor);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:27:            Map.entry("Unauthorized", "Não autorizado."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:30:            Map.entry("Full authentication is required to access this resource", "É necessário estar autenticado para acessar este recurso."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:31:            Map.entry("This feature requires the PRO plan", "Este recurso está disponível apenas no plano Premium."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:32:            Map.entry("Create a farm before accessing this feature", "Crie uma fazenda antes de acessar este recurso."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:40:            Map.entry("Authenticated user is required", "É necessário estar autenticado."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:41:            Map.entry("Authenticated user is required to register milk price", "É necessário estar autenticado para registrar o preço do leite."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:51:            Map.entry("farmIds must reference farms owned by the authenticated manager", "Selecione apenas fazendas pertencentes ao gerente autenticado."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:55:            Map.entry("Inactive user who owns farms cannot be updated", "Não é possível atualizar um usuário inativo que possui fazendas."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:57:            Map.entry("userId must be a valid UUID", "Selecione um usuário válido."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:90:            Map.entry("Cannot inactivate user who owns farms", "Não é possível inativar um usuário que possui fazendas."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:91:            Map.entry("Cannot delete user who owns farms", "Não é possível excluir um usuário que possui fazendas."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:154:            case "role" -> "A função é obrigatória.";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:163:            case "userId" -> "Selecione um usuário.";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:203:    private enum TranslationKind {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/mapper/FeedTypeMapper.java:3:import com.jpsoftware.farmapp.feed.dto.CreateFeedTypeRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/mapper/FeedTypeMapper.java:4:import com.jpsoftware.farmapp.feed.dto.FeedTypeResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/measurement/MeasurementUnit.java:7:public enum MeasurementUnit {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:4:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:5:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:11:import com.jpsoftware.farmapp.production.dto.CreateBatchProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:12:import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:13:import com.jpsoftware.farmapp.production.dto.ProductionProfitResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:14:import com.jpsoftware.farmapp.production.dto.ProductionResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:15:import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:16:import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:20:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:30:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:48:    private static final String WORKER_ROLE = "WORKER";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:53:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:55:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:65:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:67:            AuthenticationContextService authenticationContextService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:74:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:76:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:86:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:88:            AuthenticationContextService authenticationContextService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:93:                userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:95:                authenticationContextService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:103:        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:123:        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:375:            throw new ValidationException("userId must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:397:            throw new ValidationException("userId must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:407:        if (!userRepository.existsById(parseUserId(createdBy))) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:417:        if (authenticationContextService.hasRole(WORKER_ROLE)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:425:        if (authenticationContextService.hasRole(WORKER_ROLE)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:449:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId) && farmAccessService != null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:458:        } else if (accessibleFarmIds != null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:459:            specification = accessibleFarmIds.isEmpty()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:461:                    : specification.and((root, query, criteriaBuilder) -> root.get("farmId").in(accessibleFarmIds));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:575:    private java.util.UUID parseUserId(String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:577:            return java.util.UUID.fromString(userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/service/ProductionService.java:579:            throw new ValidationException("userId must be a valid UUID");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/currency/CurrencyCode.java:6:public enum CurrencyCode {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:1:package com.jpsoftware.farmapp.auth.controller;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:3:import com.jpsoftware.farmapp.auth.dto.LoginRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:4:import com.jpsoftware.farmapp.auth.dto.LoginResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:5:import com.jpsoftware.farmapp.auth.dto.RegisterRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:6:import com.jpsoftware.farmapp.auth.dto.ResendConfirmationRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:7:import com.jpsoftware.farmapp.auth.service.AuthService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:8:import com.jpsoftware.farmapp.shared.dto.MessageResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:10:import com.jpsoftware.farmapp.user.dto.UserResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:31:@RequestMapping("/auth")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:35:    private final AuthService authService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:37:    public AuthController(AuthService authService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:38:        this.authService = authService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:42:    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT access token.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:51:        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:64:        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:68:    @Operation(summary = "Confirm email", description = "Validates the email confirmation token and activates email access.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:78:        return ResponseEntity.ok(authService.confirmEmail(token));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:93:        return ResponseEntity.ok(authService.resendConfirmation(request.getEmail()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:1:package com.jpsoftware.farmapp.auth.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:12:import org.springframework.security.core.AuthenticationException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:13:import org.springframework.security.web.AuthenticationEntryPoint;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:29:            AuthenticationException authException) throws IOException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:33:        String message = authException != null && authException.getMessage() != null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:34:                ? authException.getMessage()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java:35:                : "Unauthorized";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:1:package com.jpsoftware.farmapp.auth.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:18:    @Schema(description = "User password for authentication.", example = "farmapp@123")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:1:package com.jpsoftware.farmapp.auth.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:3:import com.jpsoftware.farmapp.user.dto.UserResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:7:    private String accessToken;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:8:    private UserResponse user;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:13:    public LoginResponse(String accessToken, UserResponse user) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:14:        this.accessToken = accessToken;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:15:        this.user = user;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:19:        return accessToken;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:22:    public void setAccessToken(String accessToken) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:23:        this.accessToken = accessToken;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:27:        return user;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:30:    public void setUser(UserResponse user) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java:31:        this.user = user;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:3:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:4:import com.jpsoftware.farmapp.farm.dto.CreateFarmRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:5:import com.jpsoftware.farmapp.farm.dto.FarmResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:9:import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:21:    private final UserFarmAssignmentRepository userFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:22:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:26:            UserFarmAssignmentRepository userFarmAssignmentRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:27:            AuthenticationContextService authenticationContextService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:29:        this.userFarmAssignmentRepository = userFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:30:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:35:        return authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:36:                .map(userId -> ownedOnly ? farmRepository.findByOwnerId(userId) : findAccessibleFarmEntities(userId))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:54:        farmEntity.setOwnerId(authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:55:                .orElseThrow(() -> new ValidationException("Authenticated user is required")));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:61:    public boolean hasAccessibleFarm(UUID userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:62:        return farmRepository.existsByOwnerId(userId) || userFarmAssignmentRepository.existsByUserId(userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:69:    private List<FarmEntity> findAccessibleFarmEntities(UUID userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:70:        LinkedHashMap<String, FarmEntity> accessibleFarms = new LinkedHashMap<>();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:72:        farmRepository.findByOwnerId(userId).forEach(farm -> accessibleFarms.put(farm.getId(), farm));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:73:        userFarmAssignmentRepository.findByUserId(userId).stream()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:77:                        .ifPresent(farm -> accessibleFarms.putIfAbsent(farm.getId(), farm)));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmService.java:79:        return List.copyOf(accessibleFarms.values());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:6:import org.springframework.security.authentication.AuthenticationManager;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:7:import org.springframework.security.config.Customizer;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:8:import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:9:import org.springframework.security.config.annotation.web.builders.HttpSecurity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:10:import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:11:import org.springframework.security.config.http.SessionCreationPolicy;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:12:import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:13:import org.springframework.security.crypto.password.PasswordEncoder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:14:import org.springframework.security.web.SecurityFilterChain;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:15:import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:20:    private static final String MANAGER_AUTHORITY = "MANAGER";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:23:    private final RestAuthenticationEntryPoint authenticationEntryPoint;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:24:    private final RestAccessDeniedHandler accessDeniedHandler;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:28:            RestAuthenticationEntryPoint authenticationEntryPoint,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:29:            RestAccessDeniedHandler accessDeniedHandler) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:31:        this.authenticationEntryPoint = authenticationEntryPoint;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:32:        this.accessDeniedHandler = accessDeniedHandler;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:36:    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:42:                        .authenticationEntryPoint(authenticationEntryPoint)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:43:                        .accessDeniedHandler(accessDeniedHandler))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:44:                .authorizeHttpRequests(authorize -> authorize
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:45:                        .requestMatchers("/auth/**", "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:46:                        .requestMatchers(HttpMethod.PUT, "/users/me/password").authenticated()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:47:                        .requestMatchers(HttpMethod.POST, "/users").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:48:                        .requestMatchers(HttpMethod.PUT, "/users/*").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:49:                        .requestMatchers(HttpMethod.PATCH, "/users/*/activate").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:50:                        .requestMatchers(HttpMethod.PATCH, "/users/*/inactivate").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:51:                        .requestMatchers(HttpMethod.GET, "/dashboard", "/dashboard/export").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:52:                        .requestMatchers("/analytics/**").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:53:                        .requestMatchers(HttpMethod.DELETE, "/**").hasAuthority(MANAGER_AUTHORITY)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:54:                        .anyRequest().authenticated())
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:65:    AuthenticationManager authenticationManager(AuthenticationConfiguration authenticationConfiguration) throws Exception {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java:66:        return authenticationConfiguration.getAuthenticationManager();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:1:package com.jpsoftware.farmapp.auth.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:3:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:7:import com.jpsoftware.farmapp.user.repository.UserFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:19:    private final UserFarmAssignmentRepository userFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:20:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:24:            UserFarmAssignmentRepository userFarmAssignmentRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:25:            AuthenticationContextService authenticationContextService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:27:        this.userFarmAssignmentRepository = userFarmAssignmentRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:28:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:44:        boolean hasAccess = authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:45:                .map(userId -> hasAccessToFarm(userId, farmId))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:56:        return authenticationContextService.getAuthenticatedUserId()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:65:        Optional<UUID> authenticatedUserId = authenticationContextService.getAuthenticatedUserId();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:66:        if (authenticatedUserId.isPresent() && !hasAccessToFarm(authenticatedUserId.get(), entityFarmId)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:77:    private boolean hasAccessToFarm(UUID userId, String farmId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:78:        return farmRepository.existsByIdAndOwnerId(farmId, userId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:79:                || userFarmAssignmentRepository.existsByUserIdAndFarmId(userId, farmId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:82:    private Set<String> collectAccessibleFarmIds(UUID userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:84:        farmRepository.findByOwnerId(userId).forEach(farm -> farmIds.add(farm.getId()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/service/FarmAccessService.java:85:        userFarmAssignmentRepository.findByUserId(userId).forEach(assignment -> farmIds.add(assignment.getFarmId()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:134:                    smtpSession.authenticate();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:167:                        "app.email.smtp.username and app.email.smtp.password must be configured when app.email.smtp.auth is true");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:204:        void authenticate() throws IOException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailConfiguration.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/model/AuthenticatedUser.java:1:package com.jpsoftware.farmapp.auth.model;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/model/AuthenticatedUser.java:3:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/model/AuthenticatedUser.java:7:public record AuthenticatedUser(UUID id, List<String> roles, UserPlan plan) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/dto/FarmResponse.java:1:package com.jpsoftware.farmapp.farm.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java:12:import org.springframework.security.access.AccessDeniedException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java:13:import org.springframework.security.web.access.AccessDeniedHandler;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java:29:            AccessDeniedException accessDeniedException) throws IOException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java:33:        String message = accessDeniedException != null && accessDeniedException.getMessage() != null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java:34:                ? accessDeniedException.getMessage()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/dto/CreateFarmRequest.java:1:package com.jpsoftware.farmapp.farm.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:3:import com.jpsoftware.farmapp.auth.model.AuthenticatedUser;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:4:import com.jpsoftware.farmapp.auth.service.TokenService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:5:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:6:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:14:import org.springframework.security.authentication.BadCredentialsException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:15:import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:16:import org.springframework.security.core.authority.SimpleGrantedAuthority;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:17:import org.springframework.security.core.context.SecurityContextHolder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:18:import org.springframework.security.web.AuthenticationEntryPoint;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:29:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:30:    private final AuthenticationEntryPoint authenticationEntryPoint;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:34:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:35:            AuthenticationEntryPoint authenticationEntryPoint) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:37:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:38:        this.authenticationEntryPoint = authenticationEntryPoint;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:56:            authenticationEntryPoint.commence(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:63:        UserEntity user = userRepository.findById(tokenService.extractUserId(token)).orElse(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:64:        if (user == null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:66:            authenticationEntryPoint.commence(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:73:        if (!user.isActive()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:75:            authenticationEntryPoint.commence(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:82:        AuthenticatedUser authenticatedUser = new AuthenticatedUser(user.getId(), List.of(user.getRole()), user.getPlan());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:83:        UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:84:                authenticatedUser,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:86:                List.of(new SimpleGrantedAuthority(user.getRole())));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java:87:        SecurityContextHolder.getContext().setAuthentication(authentication);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:54:        private String username;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:56:        private boolean auth = true;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:80:            return username;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:83:        public void setUsername(String username) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:84:            this.username = username;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:96:            return auth;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:99:        public void setAuth(boolean auth) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:100:            this.auth = auth;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:1:package com.jpsoftware.farmapp.auth.infrastructure;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:3:import com.jpsoftware.farmapp.auth.service.TokenService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:4:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:7:import io.jsonwebtoken.security.Keys;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:31:            @Value("${app.security.jwt.secret}") String secret,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:32:            @Value("${app.security.jwt.allow-default-secret:false}") boolean allowDefaultSecret,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:33:            @Value("${app.security.jwt.expiration}") long expirationInMillis) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:40:    public String generateToken(UserEntity user) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:43:                .subject(user.getId().toString())
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:44:                .claim("role", user.getRole())
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java:77:            throw new IllegalStateException("app.security.jwt.secret must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/mapper/FeedingMapper.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/mapper/FeedingMapper.java:4:import com.jpsoftware.farmapp.feed.dto.FeedTypeSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/mapper/FeedingMapper.java:5:import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/mapper/FeedingMapper.java:6:import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/TokenService.java:1:package com.jpsoftware.farmapp.auth.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/TokenService.java:3:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/TokenService.java:8:    String generateToken(UserEntity user);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:3:import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:4:import com.jpsoftware.farmapp.feeding.dto.CreateBatchFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:5:import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:6:import com.jpsoftware.farmapp.feeding.dto.UpdateFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:8:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:10:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:11:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/controller/FarmController.java:3:import com.jpsoftware.farmapp.farm.dto.CreateFarmRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/controller/FarmController.java:4:import com.jpsoftware.farmapp.farm.dto.FarmResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/controller/FarmController.java:26:@Tag(name = "Farms", description = "Operations for managing accessible farms.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/controller/FarmController.java:36:    @Operation(summary = "List farms", description = "Returns the farms accessible to the authenticated user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/farm/controller/FarmController.java:43:    @Operation(summary = "Create farm", description = "Creates a new farm for the authenticated user.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:1:package com.jpsoftware.farmapp.auth.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:3:import com.jpsoftware.farmapp.auth.model.AuthenticatedUser;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:4:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:7:import org.springframework.security.core.Authentication;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:8:import org.springframework.security.core.context.SecurityContextHolder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:16:        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:17:        if (authentication == null || !authentication.isAuthenticated()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:20:        Object principal = authentication.getPrincipal();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:21:        if (principal instanceof AuthenticatedUser authenticatedUser) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:22:            return Optional.of(authenticatedUser);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:32:        return getAuthenticatedUser().map(AuthenticatedUser::plan);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:41:    public boolean hasRole(String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:42:        if (!StringUtils.hasText(role)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:47:                .map(AuthenticatedUser::roles)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java:50:                .anyMatch(authenticatedRole -> role.equalsIgnoreCase(authenticatedRole));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:4:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:5:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:8:import com.jpsoftware.farmapp.feed.dto.FeedTypeSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:12:import com.jpsoftware.farmapp.feeding.dto.CreateBatchFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:13:import com.jpsoftware.farmapp.feeding.dto.CreateFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:14:import com.jpsoftware.farmapp.feeding.dto.FeedingResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:15:import com.jpsoftware.farmapp.feeding.dto.UpdateFeedingRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:19:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:28:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:46:    private static final String WORKER_ROLE = "WORKER";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:51:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:53:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:62:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:64:            AuthenticationContextService authenticationContextService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:70:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:72:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:81:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:83:            AuthenticationContextService authenticationContextService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:88:                userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:90:                authenticationContextService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:97:        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:118:        String createdBy = authenticationContextService.resolveUserId(request != null ? request.getUserId() : null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:357:            throw new ValidationException("userId must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:379:            throw new ValidationException("userId must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:390:        if (!userRepository.existsById(parseUserId(createdBy))) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:425:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId) && farmAccessService != null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:434:        } else if (accessibleFarmIds != null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:435:            specification = accessibleFarmIds.isEmpty()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:437:                    : specification.and((root, query, criteriaBuilder) -> root.get("farmId").in(accessibleFarmIds));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:560:    private java.util.UUID parseUserId(String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:562:            return java.util.UUID.fromString(userId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:564:            throw new ValidationException("userId must be a valid UUID");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:576:        if (authenticationContextService.hasRole(WORKER_ROLE)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/service/FeedingService.java:584:        if (authenticationContextService.hasRole(WORKER_ROLE)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/mapper/ProductionMapper.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/mapper/ProductionMapper.java:4:import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/mapper/ProductionMapper.java:5:import com.jpsoftware.farmapp.production.dto.ProductionResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:1:package com.jpsoftware.farmapp.feeding.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:32:    @NotBlank(message = "userId must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:34:    private String userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:39:    public CreateFeedingRequest(String animalId, String feedTypeId, LocalDate date, Double quantity, String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:44:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:80:        return userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:83:    public void setUserId(String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateFeedingRequest.java:84:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/UpdateFeedingRequest.java:1:package com.jpsoftware.farmapp.feeding.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java:3:import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java:5:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java:6:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/FeedingResponse.java:1:package com.jpsoftware.farmapp.feeding.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/FeedingResponse.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/FeedingResponse.java:4:import com.jpsoftware.farmapp.feed.dto.FeedTypeSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:1:package com.jpsoftware.farmapp.feeding.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:30:    @NotBlank(message = "userId must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:32:    private String userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:37:    public CreateBatchFeedingRequest(String batchId, String feedTypeId, LocalDate date, Double quantity, String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:42:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:78:        return userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:81:    public void setUserId(String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/dto/CreateBatchFeedingRequest.java:82:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:4:import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:5:import com.jpsoftware.farmapp.animal.dto.SellAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:6:import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:11:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:286:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:295:        } else if (accessibleFarmIds != null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:296:            specification = accessibleFarmIds.isEmpty()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/service/AnimalService.java:298:                    : specification.and((root, query, criteriaBuilder) -> root.get("farmId").in(accessibleFarmIds));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/mapper/AnimalMapper.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/mapper/AnimalMapper.java:4:import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:4:import com.jpsoftware.farmapp.animal.dto.CreateAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:5:import com.jpsoftware.farmapp.animal.dto.SellAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:6:import com.jpsoftware.farmapp.animal.dto.UpdateAnimalRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:8:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:10:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:11:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/service/AnimalBatchService.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/service/AnimalBatchService.java:6:import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/service/AnimalBatchService.java:7:import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/service/AnimalBatchService.java:8:import com.jpsoftware.farmapp.animalbatch.dto.UpdateAnimalBatchRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/service/AnimalBatchService.java:15:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionProfitResponse.java:1:package com.jpsoftware.farmapp.production.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/AnimalSummaryResponse.java:1:package com.jpsoftware.farmapp.animal.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/CreateAnimalRequest.java:1:package com.jpsoftware.farmapp.animal.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/CreateAnimalBatchRequest.java:1:package com.jpsoftware.farmapp.animalbatch.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:3:import com.jpsoftware.farmapp.production.dto.CreateProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:4:import com.jpsoftware.farmapp.production.dto.CreateBatchProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:5:import com.jpsoftware.farmapp.production.dto.ProductionProfitResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:6:import com.jpsoftware.farmapp.production.dto.ProductionResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:7:import com.jpsoftware.farmapp.production.dto.ProductionSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:8:import com.jpsoftware.farmapp.production.dto.UpdateProductionRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:10:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:12:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:13:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/SellAnimalRequest.java:1:package com.jpsoftware.farmapp.animal.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/AnimalBatchResponse.java:1:package com.jpsoftware.farmapp.animalbatch.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/AnimalBatchResponse.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/UpdateAnimalRequest.java:1:package com.jpsoftware.farmapp.animal.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/dto/AnimalResponse.java:1:package com.jpsoftware.farmapp.animal.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:3:import com.jpsoftware.farmapp.analytics.dto.AnalyticsAnimalProductionPointResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:4:import com.jpsoftware.farmapp.analytics.dto.AnalyticsGroupBy;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:5:import com.jpsoftware.farmapp.analytics.dto.AnalyticsProfitPointResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:6:import com.jpsoftware.farmapp.analytics.dto.AnalyticsTimeSeriesPointResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:391:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:397:                        .filter(production -> matchesFarmScope(production.getFarmId(), accessibleFarmIds))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:407:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:413:                        .filter(feeding -> matchesFarmScope(feeding.getFarmId(), accessibleFarmIds))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:483:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:493:                    .filter(animal -> matchesFarmScope(animal.getFarmId(), accessibleFarmIds))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:513:    private boolean matchesFarmScope(String entityFarmId, Set<String> accessibleFarmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/service/AnalyticsService.java:514:        return accessibleFarmIds == null || accessibleFarmIds.contains(entityFarmId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionSummaryResponse.java:1:package com.jpsoftware.farmapp.production.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/UpdateProductionRequest.java:1:package com.jpsoftware.farmapp.production.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionResponse.java:1:package com.jpsoftware.farmapp.production.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/ProductionResponse.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/controller/AnimalBatchController.java:3:import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/controller/AnimalBatchController.java:4:import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/controller/AnimalBatchController.java:5:import com.jpsoftware.farmapp.animalbatch.dto.UpdateAnimalBatchRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/controller/AnimalBatchController.java:7:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/dto/DashboardResponse.java:1:package com.jpsoftware.farmapp.dashboard.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:1:package com.jpsoftware.farmapp.production.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:26:    @NotBlank(message = "userId must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:28:    private String userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:33:    public CreateBatchProductionRequest(String batchId, LocalDate date, Double quantity, String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:37:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:65:        return userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:68:    public void setUserId(String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateBatchProductionRequest.java:69:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/mapper/AnimalBatchMapper.java:3:import com.jpsoftware.farmapp.animal.dto.AnimalSummaryResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/mapper/AnimalBatchMapper.java:4:import com.jpsoftware.farmapp.animalbatch.dto.AnimalBatchResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/mapper/AnimalBatchMapper.java:5:import com.jpsoftware.farmapp.animalbatch.dto.CreateAnimalBatchRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsTimeSeriesPointResponse.java:1:package com.jpsoftware.farmapp.analytics.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:1:package com.jpsoftware.farmapp.auth.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:8:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:9:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:21:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:29:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:32:            @Value("${app.auth.email-confirmation.frontend-base-url}") String frontendBaseUrl,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:34:            @Value("${app.auth.email-confirmation.token-expiration-hours}") long tokenExpirationHours) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:35:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:44:    public void initializePendingConfirmation(UserEntity userEntity) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:45:        issueConfirmationToken(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:50:        UserEntity userEntity = userRepository.findByEmail(normalizeEmail(email))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:53:        if (userEntity.isEmailConfirmed()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:57:        issueConfirmationToken(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:67:        UserEntity userEntity = userRepository.findByEmailConfirmationTokenHash(tokenHash)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:70:        if (userEntity.isEmailConfirmed()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:73:        if (userEntity.getEmailConfirmationTokenExpiresAt() == null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:74:                || userEntity.getEmailConfirmationTokenExpiresAt().isBefore(Instant.now())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:78:        userEntity.setEmailConfirmed(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:79:        userEntity.setEmailConfirmationTokenHash(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:80:        userEntity.setEmailConfirmationTokenExpiresAt(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:81:        userRepository.save(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:84:    private void issueConfirmationToken(UserEntity userEntity) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:86:        userEntity.setEmailConfirmed(false);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:87:        userEntity.setEmailConfirmationTokenHash(tokenService.hashToken(rawToken));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:88:        userEntity.setEmailConfirmationTokenExpiresAt(Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:89:        userRepository.save(userEntity);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:92:                userEntity.getEmail(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:94:                buildConfirmationBody(userEntity.getName(), buildConfirmationUrl(rawToken))));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsProfitPointResponse.java:1:package com.jpsoftware.farmapp.analytics.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:3:import com.jpsoftware.farmapp.analytics.dto.AnalyticsAnimalProductionPointResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:4:import com.jpsoftware.farmapp.analytics.dto.AnalyticsProfitPointResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:5:import com.jpsoftware.farmapp.analytics.dto.AnalyticsTimeSeriesPointResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:7:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:8:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsAnimalProductionPointResponse.java:1:package com.jpsoftware.farmapp.analytics.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java:1:package com.jpsoftware.farmapp.auth.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java:4:import java.security.MessageDigest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java:5:import java.security.NoSuchAlgorithmException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java:6:import java.security.SecureRandom;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsGroupBy.java:1:package com.jpsoftware.farmapp.analytics.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/dto/AnalyticsGroupBy.java:3:public enum AnalyticsGroupBy {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:1:package com.jpsoftware.farmapp.production.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:30:    private String userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:35:    public CreateProductionRequest(String animalId, LocalDate date, Double quantity, String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:39:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:67:        return userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:70:    public void setUserId(String userId) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/dto/CreateProductionRequest.java:71:        this.userId = userId;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:1:package com.jpsoftware.farmapp.auth.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:3:import com.jpsoftware.farmapp.auth.dto.LoginResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:4:import com.jpsoftware.farmapp.auth.dto.RegisterRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:5:import com.jpsoftware.farmapp.shared.dto.MessageResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:10:import com.jpsoftware.farmapp.user.dto.UserResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:11:import com.jpsoftware.farmapp.user.entity.UserEntity;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:12:import com.jpsoftware.farmapp.user.entity.UserPlan;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:13:import com.jpsoftware.farmapp.user.mapper.UserMapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:14:import com.jpsoftware.farmapp.user.repository.UserRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:17:import org.springframework.security.crypto.password.PasswordEncoder;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:25:    private final UserRepository userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:26:    private final UserMapper userMapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:32:            UserRepository userRepository,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:33:            UserMapper userMapper,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:37:        this.userRepository = userRepository;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:38:        this.userMapper = userMapper;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:50:        UserEntity user = userRepository.findByEmail(normalizeEmail(email))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:53:        if (!user.isActive() || !passwordEncoder.matches(password, user.getPassword())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:56:        if (!user.isEmailConfirmed()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:60:        String accessToken = tokenService.generateToken(user);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:61:        UserResponse userResponse = userMapper.toResponse(user);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:62:        return new LoginResponse(accessToken, userResponse);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:72:        userRepository.findByEmail(normalizedEmail)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:77:        UserEntity user = new UserEntity();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:78:        user.setName(request.getName().trim());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:79:        user.setEmail(normalizedEmail);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:80:        user.setRole("MANAGER");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:81:        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:82:        user.setActive(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:83:        user.setEmailConfirmed(false);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:84:        user.setPlan(UserPlan.defaultPlan());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:86:        UserEntity savedUser = userRepository.save(user);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:88:        return userMapper.toResponse(savedUser, List.of());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animalbatch/dto/UpdateAnimalBatchRequest.java:1:package com.jpsoftware.farmapp.animalbatch.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java:5:import com.jpsoftware.farmapp.dashboard.dto.DashboardResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java:315:        Set<String> accessibleFarmIds = !StringUtils.hasText(farmId)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java:321:                    .filter(animal -> matchesFarmScope(animal.getFarmId(), farmId, accessibleFarmIds))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java:327:                    .filter(animal -> matchesFarmScope(animal.getFarmId(), null, accessibleFarmIds))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java:441:    private boolean matchesFarmScope(String entityFarmId, String farmId, Set<String> accessibleFarmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/service/DashboardService.java:445:        return accessibleFarmIds == null || accessibleFarmIds.contains(entityFarmId);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:3:import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:6:import com.jpsoftware.farmapp.milkprice.dto.CreateMilkPriceRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:7:import com.jpsoftware.farmapp.milkprice.dto.MilkPriceResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:11:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:36:    private final AuthenticationContextService authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:42:            AuthenticationContextService authenticationContextService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:46:        this.authenticationContextService = authenticationContextService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:54:        String createdBy = authenticationContextService.resolveUserId(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/service/MilkPriceService.java:56:            throw new ValidationException("Authenticated user is required to register milk price");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/dto/MilkPriceResponse.java:1:package com.jpsoftware.farmapp.milkprice.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/dto/CreateMilkPriceRequest.java:1:package com.jpsoftware.farmapp.milkprice.dto;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:3:import com.jpsoftware.farmapp.milkprice.dto.CreateMilkPriceRequest;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:4:import com.jpsoftware.farmapp.milkprice.dto.MilkPriceResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:6:import com.jpsoftware.farmapp.shared.dto.PaginatedResponse;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:8:import com.jpsoftware.farmapp.shared.plan.PlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:9:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;

codex
There’s already plan infrastructure on both sides, so the plan should extend that rather than introduce a new concept. I’m reading the existing plan-access path now to anchor the proposed target files and keep the change localized.
exec
/bin/bash -lc "sed -n '1,220p' frontend/web/farm_web/src/utils/planAccess.ts" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import type { User, UserPlan } from '../types/user'

export type AppFeature = 'DASHBOARD' | 'ANALYTICS' | 'CSV_EXPORT'

interface FeatureMetadata {
  descriptionKey: string
  minimumPlan: UserPlan
  titleKey: string
}

interface PlanMetadata {
  labelKey: string
  paid: boolean
  rank: number
}

export interface FeatureAccessState {
  allowed: boolean
  currentPlan: UserPlan
  feature: AppFeature
  metadata: FeatureMetadata
  minimumPlan: UserPlan
}

const planMetadata: Record<UserPlan, PlanMetadata> = {
  FREE: {
    labelKey: 'plan.labels.FREE',
    paid: false,
    rank: 0,
  },
  PRO: {
    labelKey: 'plan.labels.PRO',
    paid: true,
    rank: 1,
  },
}

const featureMetadata: Record<AppFeature, FeatureMetadata> = {
  DASHBOARD: {
    minimumPlan: 'PRO',
    titleKey: 'plan.features.dashboard.title',
    descriptionKey: 'plan.features.dashboard.description',
  },
  ANALYTICS: {
    minimumPlan: 'PRO',
    titleKey: 'plan.features.analytics.title',
    descriptionKey: 'plan.features.analytics.description',
  },
  CSV_EXPORT: {
    minimumPlan: 'PRO',
    titleKey: 'plan.features.csvExport.title',
    descriptionKey: 'plan.features.csvExport.description',
  },
}

export function resolvePlan(user: Pick<User, 'plan'> | null | undefined): UserPlan {
  return user?.plan ?? 'FREE'
}

export function getPlanMetadata(plan: UserPlan) {
  return planMetadata[plan]
}

export function getCurrentPlanMetadata(user: Pick<User, 'plan'> | null | undefined) {
  return getPlanMetadata(resolvePlan(user))
}

export function getFeatureAccessState(
  user: Pick<User, 'plan'> | null | undefined,
  feature: AppFeature,
): FeatureAccessState {
  const currentPlan = resolvePlan(user)
  const metadata = featureMetadata[feature]

  return {
    allowed: planMetadata[currentPlan].rank >= planMetadata[metadata.minimumPlan].rank,
    currentPlan,
    feature,
    metadata,
    minimumPlan: metadata.minimumPlan,
  }
}

export function hasFeatureAccess(user: Pick<User, 'plan'> | null | undefined, feature: AppFeature) {
  return getFeatureAccessState(user, feature).allowed
}

export function getFeatureMetadata(feature: AppFeature) {
  return featureMetadata[feature]
}

exec
/bin/bash -lc "sed -n '1,220p' frontend/web/farm_web/src/components/auth/PlanRoute.tsx" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import type { ReactElement } from 'react'
import { useAuth } from '../../hooks/useAuth'
import PlanUpgradeNotice from '../common/PlanUpgradeNotice'
import { getFeatureAccessState, type AppFeature } from '../../utils/planAccess'

interface PlanRouteProps {
  children: ReactElement
  feature: AppFeature
}

function PlanRoute({ children, feature }: PlanRouteProps) {
  const { user } = useAuth()
  const accessState = getFeatureAccessState(user, feature)

  if (!accessState.allowed) {
    return <PlanUpgradeNotice feature={feature} />
  }

  return children
}

export default PlanRoute

exec
/bin/bash -lc "sed -n '1,220p' frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import { useTranslation } from '../../hooks/useTranslation'
import { getFeatureMetadata, type AppFeature } from '../../utils/planAccess'

interface PlanUpgradeNoticeProps {
  feature: AppFeature
}

function PlanUpgradeNotice({ feature }: PlanUpgradeNoticeProps) {
  const { t } = useTranslation()
  const metadata = getFeatureMetadata(feature)

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('plan.eyebrow')}</p>
        <h1>{t('plan.title')}</h1>
        <p className="animals-page__description">{t('plan.description')}</p>
      </section>

      <section className="animals-panel plan-upgrade-notice">
        <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
        <h2>{t(metadata.titleKey)}</h2>
        <p>{t(metadata.descriptionKey)}</p>
        <p>{t('plan.upgradeHint')}</p>
      </section>
    </main>
  )
}

export default PlanUpgradeNotice

exec
/bin/bash -lc "sed -n '1,240p' frontend/web/farm_web/src/App.tsx" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import './App.css'
import DashboardPage from './pages/dashboard/DashboardPage'
import AnimalsPage from './pages/animals/AnimalsPage'
import AnimalDetailsPage from './pages/animals/AnimalDetailsPage'
import AnalyticsPage from './pages/analytics/AnalyticsPage'
import AnimalBatchPage from './pages/batch/AnimalBatchPage'
import FeedingPage from './pages/feeding/FeedingPage'
import FeedTypePage from './pages/feed-type/FeedTypePage'
import ProductionPage from './pages/production/ProductionPage'
import MilkPricePage from './pages/milk-price/MilkPricePage'
import UsersPage from './pages/users/UsersPage'
import AppLayout from './layout/AppLayout'
import LoginPage from './pages/login/LoginPage'
import ProtectedRoute from './components/auth/ProtectedRoute'
import ManagerRoute from './components/auth/ManagerRoute'
import PlanRoute from './components/auth/PlanRoute'
import FarmCreatePage from './pages/farm/FarmCreatePage'
import FarmOnboardingPage from './pages/farm/FarmOnboardingPage'
import SettingsPage from './pages/settings/SettingsPage'
import { useAuth } from './hooks/useAuth'
import { hasFeatureAccess } from './utils/planAccess'
import { isManager } from './utils/authorization'

function AnimalsRoute() {
  const navigate = useNavigate()

  return <AnimalsPage onOpenDetails={(animalId) => navigate(`/animals/${animalId}`)} />
}

function AnimalDetailsRoute() {
  const navigate = useNavigate()
  const { id = '' } = useParams()

  return <AnimalDetailsPage animalId={id} onBackToAnimals={() => navigate('/animals')} />
}

function DefaultRoute() {
  const { user } = useAuth()

  return <Navigate to={isManager(user) && hasFeatureAccess(user, 'DASHBOARD') ? '/dashboard' : '/animals'} replace />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route path="/onboarding/farm" element={<FarmOnboardingPage />} />
        <Route element={<AppLayout />}>
          <Route index element={<DefaultRoute />} />
          <Route
            path="/dashboard"
            element={(
              <ManagerRoute>
                <PlanRoute feature="DASHBOARD">
                  <DashboardPage />
                </PlanRoute>
              </ManagerRoute>
            )}
          />
          <Route path="/animals" element={<AnimalsRoute />} />
          <Route path="/animals/:id" element={<AnimalDetailsRoute />} />
          <Route path="/batches" element={<AnimalBatchPage />} />
          <Route path="/production" element={<ProductionPage />} />
          <Route path="/milk-prices" element={<MilkPricePage />} />
          <Route path="/feeding" element={<FeedingPage />} />
          <Route path="/feed-types" element={<FeedTypePage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route
            path="/users"
            element={(
              <ManagerRoute>
                <UsersPage />
              </ManagerRoute>
            )}
          />
          <Route
            path="/analytics"
            element={(
              <ManagerRoute>
                <PlanRoute feature="ANALYTICS">
                  <AnalyticsPage />
                </PlanRoute>
              </ManagerRoute>
            )}
          />
          <Route path="/farms/new" element={<FarmCreatePage />} />
          <Route path="/productions" element={<Navigate to="/production" replace />} />
          <Route path="/feedings" element={<Navigate to="/feeding" replace />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanEntitlementResolver.java" in /home/juaneugenio/programmig/farm_app
exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeature.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;

public enum PlanFeature {
    DASHBOARD(UserPlan.PRO),
    ANALYTICS(UserPlan.PRO),
    CSV_EXPORT(UserPlan.PRO);

    private final UserPlan minimumPlan;

    PlanFeature(UserPlan minimumPlan) {
        this.minimumPlan = minimumPlan;
    }

    public UserPlan getMinimumPlan() {
        return minimumPlan;
    }

    public boolean isAvailableFor(UserPlan plan) {
        UserPlan resolvedPlan = plan == null ? UserPlan.defaultPlan() : plan;
        return resolvedPlan.includes(minimumPlan);
    }

    public boolean isAvailableFor(PlanEntitlement entitlement) {
        PlanEntitlement resolvedEntitlement = entitlement == null
                ? PlanEntitlement.defaultEntitlement()
                : entitlement;
        return resolvedEntitlement.allows(this);
    }
}

 succeeded in 0ms:
package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.springframework.stereotype.Service;

@Service
public class PlanEntitlementResolver {

    public PlanEntitlement resolve(UserPlan plan) {
        return PlanEntitlement.internallyActivated(plan == null ? UserPlan.defaultPlan() : plan);
    }
}

codex
The app already blocks some premium routes and CSV exports, but it currently renders an inline notice instead of a shared upgrade flow. I’m checking how broad that existing coverage is, because the plan should convert that pattern rather than add a second gating system.
exec
/bin/bash -lc "sed -n '1,240p' frontend/web/farm_web/tests/plan-access-ui.test.mjs" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')

function readSource(relativePath) {
  return readFileSync(path.join(projectRoot, relativePath), 'utf8')
}

test('app routes source gates dashboard and analytics behind the shared plan route', () => {
  const source = readSource('src/App.tsx')

  assert.match(source, /hasFeatureAccess\(user, 'DASHBOARD'\)/)
  assert.match(source, /<PlanRoute feature="DASHBOARD">[\s\S]*<DashboardPage \/>[\s\S]*<\/PlanRoute>/)
  assert.match(source, /<PlanRoute feature="ANALYTICS">[\s\S]*<AnalyticsPage \/>[\s\S]*<\/PlanRoute>/)
})

test('shared export button source centralizes csv plan checks', () => {
  const source = readSource('src/components/common/ExportCsvButton.tsx')

  assert.match(source, /getFeatureAccessState\(user, 'CSV_EXPORT'\)/)
  assert.match(source, /disabled=\{disabled \|\| isLoading \|\| isPlanRestricted\}/)
  assert.match(source, /title=\{isPlanRestricted \? t\(accessState\.metadata\.descriptionKey\) : undefined\}/)
})

test('shared access utility source resolves free-plan defaults and compares plan rank centrally', () => {
  const source = readSource('src/utils/planAccess.ts')

  assert.match(source, /export function resolvePlan\(user: Pick<User, 'plan'> \| null \| undefined\): UserPlan \{\s*return user\?\.plan \?\? 'FREE'/)
  assert.match(source, /allowed: planMetadata\[currentPlan\]\.rank >= planMetadata\[metadata\.minimumPlan\]\.rank/)
  assert.match(source, /minimumPlan: metadata\.minimumPlan/)
})

test('plan route source uses the shared access state decision', () => {
  const source = readSource('src/components/auth/PlanRoute.tsx')

  assert.match(source, /getFeatureAccessState, type AppFeature/)
  assert.match(source, /const accessState = getFeatureAccessState\(user, feature\)/)
  assert.match(source, /if \(!accessState\.allowed\) \{/)
})

test('app layout source exposes premium navigation items without duplicating rules', () => {
  const source = readSource('src/layout/AppLayout.tsx')

  assert.match(source, /feature: 'DASHBOARD'/)
  assert.match(source, /feature: 'ANALYTICS'/)
  assert.match(source, /getFeatureAccessState\(user, restrictedFeature\)/)
  assert.match(source, /featureAccessState !== null && !featureAccessState\.allowed/)
  assert.match(source, /className="app-layout__nav-link app-layout__nav-link--disabled"/)
  assert.match(source, /t\('plan\.badge'\)/)
  assert.match(source, /t\('plan\.currentLabel'\)/)
})

exec
/bin/bash -lc "sed -n '1,220p' frontend/web/farm_web/src/components/common/ExportCsvButton.tsx" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import { useAuth } from '../../hooks/useAuth'
import { useTranslation } from '../../hooks/useTranslation'
import { getFeatureAccessState } from '../../utils/planAccess'

interface ExportCsvButtonProps {
  onClick: () => void
  label: string
  loadingLabel: string
  isLoading?: boolean
  disabled?: boolean
  className?: string
}

function ExportCsvButton({
  onClick,
  label,
  loadingLabel,
  isLoading = false,
  disabled = false,
  className = 'animals-table__action-button animals-table__action-button--secondary',
}: ExportCsvButtonProps) {
  const { user } = useAuth()
  const { t } = useTranslation()
  const accessState = getFeatureAccessState(user, 'CSV_EXPORT')
  const isPlanRestricted = !accessState.allowed
  const visibleLabel = isLoading
    ? loadingLabel
    : isPlanRestricted
      ? `${label} (${t('plan.badge')})`
      : label

  return (
    <button
      type="button"
      className={className}
      onClick={onClick}
      disabled={disabled || isLoading || isPlanRestricted}
      title={isPlanRestricted ? t(accessState.metadata.descriptionKey) : undefined}
    >
      {visibleLabel}
    </button>
  )
}

export default ExportCsvButton

exec
/bin/bash -lc "sed -n '1,260p' frontend/web/farm_web/src/layout/AppLayout.tsx" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import { useState } from 'react'
import { NavLink, Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useCurrency } from '../hooks/useCurrency'
import { useFarm } from '../hooks/useFarm'
import { useMeasurementUnits } from '../hooks/useMeasurementUnits'
import { useLanguage, type Language } from '../context/LanguageContext'
import { useTranslation } from '../hooks/useTranslation'
import { getCurrentPlanMetadata, getFeatureAccessState, type AppFeature } from '../utils/planAccess'
import { isManager } from '../utils/authorization'

interface NavigationItem {
  to: string
  labelKey: string
  managerOnly?: boolean
  feature?: AppFeature
}

const navigationItems: NavigationItem[] = [
  { to: '/dashboard', labelKey: 'layout.navigation.dashboard', managerOnly: true, feature: 'DASHBOARD' },
  { to: '/animals', labelKey: 'layout.navigation.animals' },
  { to: '/batches', labelKey: 'layout.navigation.batches' },
  { to: '/production', labelKey: 'layout.navigation.production' },
  { to: '/milk-prices', labelKey: 'layout.navigation.milkPrices' },
  { to: '/feeding', labelKey: 'layout.navigation.feeding' },
  { to: '/feed-types', labelKey: 'layout.navigation.feedTypes' },
  { to: '/users', labelKey: 'layout.navigation.users', managerOnly: true },
  { to: '/analytics', labelKey: 'layout.navigation.analytics', managerOnly: true, feature: 'ANALYTICS' },
  { to: '/settings', labelKey: 'layout.navigation.settings' },
]

function AppLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()
  const [isMobileNavigationOpen, setIsMobileNavigationOpen] = useState(false)
  const {
    farms,
    selectedFarmId,
    selectedFarm,
    isLoading: isFarmsLoading,
    errorMessage: farmsErrorMessage,
    setSelectedFarmId,
  } = useFarm()
  const { currency, setCurrency } = useCurrency()
  const {
    productionUnit,
    feedingUnit,
    setProductionUnit,
    setFeedingUnit,
  } = useMeasurementUnits()
  const { language, setLanguage } = useLanguage()
  const { t } = useTranslation()
  const canManageRestrictedFeatures = isManager(user)
  const currentPlanMetadata = getCurrentPlanMetadata(user)
  const activeNavigationItem = navigationItems.find(
    (item) => location.pathname === item.to || location.pathname.startsWith(`${item.to}/`),
  )

  function handleLanguageChange(nextLanguage: Language) {
    setLanguage(nextLanguage)
  }

  function handleLogout() {
    setIsMobileNavigationOpen(false)
    logout()
    navigate('/login', { replace: true })
  }

  const isFarmSelectionOptionalRoute =
    location.pathname === '/farms/new' || location.pathname === '/settings'

  return (
    <div className={`app-layout${isMobileNavigationOpen ? ' app-layout--nav-open' : ''}`}>
      <button
        type="button"
        className="app-layout__backdrop"
        aria-label={t('layout.closeNavigation')}
        onClick={() => setIsMobileNavigationOpen(false)}
      />

      <aside
        id="app-layout-sidebar-navigation"
        className="app-layout__sidebar"
        aria-label={t('layout.ariaLabel')}
      >
        <div className="app-layout__sidebar-header">
          <div className="app-layout__brand">
            <span className="app-layout__eyebrow">{t('layout.brandEyebrow')}</span>
            <strong className="app-layout__title">{t('layout.brandTitle')}</strong>
          </div>

          <button
            type="button"
            className="app-layout__sidebar-close"
            onClick={() => setIsMobileNavigationOpen(false)}
          >
            {t('layout.closeNavigation')}
          </button>
        </div>

        <div className="app-layout__sidebar-scroll">
          <nav className="app-layout__nav">
            {navigationItems.filter((item) => !item.managerOnly || canManageRestrictedFeatures).map((item) => {
              const restrictedFeature = item.feature
              const featureAccessState = restrictedFeature
                ? getFeatureAccessState(user, restrictedFeature)
                : null
              const isPlanRestricted = featureAccessState !== null && !featureAccessState.allowed

              if (isPlanRestricted) {
                return (
                  <span
                    key={item.to}
                    className="app-layout__nav-link app-layout__nav-link--disabled"
                    title={featureAccessState ? t(featureAccessState.metadata.descriptionKey) : undefined}
                  >
                    <span>{t(item.labelKey)}</span>
                    <span className="app-layout__nav-badge">{t('plan.badge')}</span>
                  </span>
                )
              }

              return (
                <NavLink
                  key={item.to}
                  to={item.to}
                  onClick={() => setIsMobileNavigationOpen(false)}
                  className={({ isActive }) =>
                    `app-layout__nav-link${isActive ? ' app-layout__nav-link--active' : ''}`
                  }
                >
                  {t(item.labelKey)}
                </NavLink>
              )
            })}
          </nav>

          <div className="app-layout__farm-selector">
            <label className="app-layout__language-label" htmlFor="farm-selector">
              {t('layout.farmLabel')}
            </label>
            <select
              id="farm-selector"
              className="app-layout__farm-select"
              value={selectedFarmId}
              onChange={(event) => setSelectedFarmId(event.target.value)}
              disabled={isFarmsLoading || farms.length === 0}
            >
              <option value="">
                {isFarmsLoading ? t('layout.loadingFarms') : t('layout.selectFarm')}
              </option>
              {farms.map((farm) => (
                <option key={farm.id} value={farm.id}>
                  {farm.name}
                </option>
              ))}
            </select>
            {farmsErrorMessage && (
              <p className="animals-page__status animals-page__status--error">{t(farmsErrorMessage)}</p>
            )}
            <button
              type="button"
              className="animals-table__action-button animals-table__action-button--secondary"
              onClick={() => {
                setIsMobileNavigationOpen(false)
                navigate('/farms/new')
              }}
            >
              {t('farm.createAction')}
            </button>
          </div>

          <div className="app-layout__language-switcher" role="group" aria-label={t('layout.languageLabel')}>
            <span className="app-layout__language-label">{t('layout.languageLabel')}</span>
            <div className="app-layout__language-options">
              <button
                type="button"
                className={`app-layout__language-button${language === 'pt-BR' ? ' app-layout__language-button--active' : ''}`}
                onClick={() => handleLanguageChange('pt-BR')}
                aria-pressed={language === 'pt-BR'}
              >
                {t('layout.languageOptions.pt-BR')}
              </button>
              <button
                type="button"
                className={`app-layout__language-button${language === 'en' ? ' app-layout__language-button--active' : ''}`}
                onClick={() => handleLanguageChange('en')}
                aria-pressed={language === 'en'}
              >
                {t('layout.languageOptions.en')}
              </button>
            </div>
          </div>

          <div className="app-layout__farm-selector">
            <label className="app-layout__language-label" htmlFor="currency-selector">
              {t('layout.currencyLabel')}
            </label>
            <select
              id="currency-selector"
              className="app-layout__farm-select"
              value={currency}
              onChange={(event) => setCurrency(event.target.value as 'BRL' | 'USD')}
            >
              <option value="BRL">{t('layout.currencyOptions.BRL')}</option>
              <option value="USD">{t('layout.currencyOptions.USD')}</option>
            </select>
          </div>

          <div className="app-layout__farm-selector">
            <label className="app-layout__language-label" htmlFor="production-unit-selector">
              {t('measurementUnits.productionLabel')}
            </label>
            <select
              id="production-unit-selector"
              className="app-layout__farm-select"
              value={productionUnit}
              onChange={(event) => setProductionUnit(event.target.value as typeof productionUnit)}
            >
              <option value="LITER">{t('measurementUnits.options.LITER')}</option>
              <option value="MILLILITER">{t('measurementUnits.options.MILLILITER')}</option>
            </select>
          </div>

          <div className="app-layout__farm-selector">
            <label className="app-layout__language-label" htmlFor="feeding-unit-selector">
              {t('measurementUnits.feedingLabel')}
            </label>
            <select
              id="feeding-unit-selector"
              className="app-layout__farm-select"
              value={feedingUnit}
              onChange={(event) => setFeedingUnit(event.target.value as typeof feedingUnit)}
            >
              <option value="KILOGRAM">{t('measurementUnits.options.KILOGRAM')}</option>
              <option value="GRAM">{t('measurementUnits.options.GRAM')}</option>
            </select>
          </div>

          <div className="app-layout__session">
            <p className="app-layout__session-user">{user?.name ?? user?.email}</p>
            <div className="app-layout__session-plan">
              <span className="app-layout__language-label">{t('plan.currentLabel')}</span>
              <span
                className={`app-layout__plan-badge${currentPlanMetadata.paid ? ' app-layout__plan-badge--premium' : ''}`}
              >
                {t(currentPlanMetadata.labelKey)}
              </span>
            </div>
            <button type="button" className="app-layout__logout-button" onClick={handleLogout}>
              {t('layout.logout')}
            </button>
          </div>
        </div>
      </aside>

      <div className="app-layout__content">
        <header className="app-layout__mobile-bar">
          <button

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.shared.plan;

import com.jpsoftware.farmapp.auth.service.AuthenticationContextService;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.core.annotation.AnnotatedElementUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class PlanFeatureAccessInterceptor implements HandlerInterceptor {

    private final AuthenticationContextService authenticationContextService;
    private final PlanAccessPolicy planAccessPolicy;

    public PlanFeatureAccessInterceptor(
            AuthenticationContextService authenticationContextService,
            PlanAccessPolicy planAccessPolicy) {
        this.authenticationContextService = authenticationContextService;
        this.planAccessPolicy = planAccessPolicy;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequiresPlanFeature requiresPlanFeature = AnnotatedElementUtils.findMergedAnnotation(
                handlerMethod.getMethod(),
                RequiresPlanFeature.class);
        if (requiresPlanFeature == null) {
            requiresPlanFeature = AnnotatedElementUtils.findMergedAnnotation(
                    handlerMethod.getBeanType(),
                    RequiresPlanFeature.class);
        }

        if (requiresPlanFeature == null) {
            return true;
        }

        UserPlan userPlan = authenticationContextService.getAuthenticatedUser()
                .map(authenticatedUser -> authenticatedUser.plan() == null
                        ? UserPlan.defaultPlan()
                        : authenticatedUser.plan())
                .orElse(UserPlan.defaultPlan());

        planAccessPolicy.assertHasAccess(userPlan, requiresPlanFeature.value());
        return true;
    }
}

exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.integration.plan;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.farm.entity.FarmEntity;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.mock.mockito.MockBean;

class PlanFeatureAccessIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private EmailSender emailSender;

    @Test
    void shouldBlockDashboardForFreeManager() throws Exception {
        UserEntity freeManager = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        FarmEntity farm = createFarmOwnedBy(freeManager, "Fazenda Premium");

        mockMvc.perform(get("/dashboard")
                        .param("farmId", farm.getId().toString())
                        .header("Authorization", bearerToken(freeManager)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Este recurso está disponível apenas no plano Premium."));
    }

    @Test
    void shouldAllowDashboardForProManager() throws Exception {
        UserEntity proManager = createAuthenticatedUser("MANAGER", UserPlan.PRO);
        FarmEntity farm = createFarmOwnedBy(proManager, "Fazenda Premium");

        mockMvc.perform(get("/dashboard")
                        .param("farmId", farm.getId().toString())
                        .header("Authorization", bearerToken(proManager)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalProduction").exists())
                .andExpect(jsonPath("$.animalCount").exists());
    }

    @Test
    void shouldBlockCsvExportForFreeUser() throws Exception {
        UserEntity freeWorker = createAuthenticatedUser("WORKER", UserPlan.FREE);
        UserEntity owner = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        FarmEntity farm = createFarmOwnedBy(owner, "Fazenda Worker");
        assignUserToFarm(freeWorker, farm);

        mockMvc.perform(get("/animals/export")
                        .header("Authorization", bearerToken(freeWorker)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Este recurso está disponível apenas no plano Premium."));
    }

    @Test
    void shouldAllowCsvExportForProUser() throws Exception {
        UserEntity proWorker = createAuthenticatedUser("WORKER", UserPlan.PRO);
        UserEntity owner = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        FarmEntity farm = createFarmOwnedBy(owner, "Fazenda Worker");
        assignUserToFarm(proWorker, farm);

        mockMvc.perform(get("/animals/export")
                        .header("Authorization", bearerToken(proWorker)))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", Matchers.containsString("animals.csv")));
    }

    @Test
    void shouldBlockAnalyticsForFreeManager() throws Exception {
        UserEntity freeManager = createAuthenticatedUser("MANAGER", UserPlan.FREE);
        createFarmOwnedBy(freeManager, "Fazenda Analytics");

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(freeManager)))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Este recurso está disponível apenas no plano Premium."));
    }

    @Test
    void shouldAllowAnalyticsForProManager() throws Exception {
        UserEntity proManager = createAuthenticatedUser("MANAGER", UserPlan.PRO);
        createFarmOwnedBy(proManager, "Fazenda Analytics");

        mockMvc.perform(get("/analytics/production")
                        .header("Authorization", bearerToken(proManager)))
                .andExpect(status().isOk());
    }
}

exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserPlan.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.user.entity;

public enum UserPlan {
    FREE(0),
    PRO(1);

    private final int rank;

    UserPlan(int rank) {
        this.rank = rank;
    }

    public int getRank() {
        return rank;
    }

    public boolean includes(UserPlan requiredPlan) {
        UserPlan resolvedRequiredPlan = requiredPlan == null ? defaultPlan() : requiredPlan;
        return rank >= resolvedRequiredPlan.rank;
    }

    public static UserPlan defaultPlan() {
        return FREE;
    }
}

codex
The backend already knows `FREE` vs `PRO`, and the frontend already receives `user.plan`. That means the narrow plan is probably frontend-heavy, with backend limited to preserving consistent 403 behavior for direct API access and adding tests only if any new restricted feature is introduced.
exec
/bin/bash -lc "sed -n '1,220p' frontend/web/farm_web/src/types/user.ts" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
export type UserRole = 'MANAGER' | 'WORKER'
export type UserPlan = 'FREE' | 'PRO'

export interface User {
  id: string
  name: string
  email: string
  role: UserRole
  active: boolean
  avatarUrl?: string | null
  plan: UserPlan
  farmIds: string[]
}

export interface UserFormData {
  name: string
  email: string
  role: UserRole | ''
  password: string
  active: boolean
  avatarUrl: string
  farmIds: string[]
}

export interface UserListFilters {
  search: string
  active: '' | 'true' | 'false'
  role: UserRole | ''
}

export interface UserApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}

exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.List;
import java.util.UUID;

@Schema(description = "User data returned by the API.")
public class UserResponse {

    @Schema(description = "User identifier.", example = "550e8400-e29b-41d4-a716-446655440000")
    private UUID id;

    @Schema(description = "User full name.", example = "Maria Silva")
    private String name;

    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
    private String email;

    @Schema(description = "User role in the system.", example = "MANAGER")
    private String role;

    @Schema(description = "Whether the user can authenticate.", example = "true")
    private Boolean active;

    @Schema(
            description = "User avatar image as a URL or data URL.",
            example = "https://example.com/avatar.png")
    private String avatarUrl;

    @Schema(description = "User plan.", example = "FREE")
    private String plan;

    @Schema(description = "Farm identifiers assigned to the user.", example = "[\"farm-001\"]")
    private List<String> farmIds;

    public UserResponse() {
    }

    public UserResponse(UUID id, String name, String email, String role) {
        this(id, name, email, role, null, null, null, null);
    }

    public UserResponse(UUID id, String name, String email, String role, Boolean active, String avatarUrl, List<String> farmIds) {
        this(id, name, email, role, active, avatarUrl, null, farmIds);
    }

    public UserResponse(
            UUID id,
            String name,
            String email,
            String role,
            Boolean active,
            String avatarUrl,
            String plan,
            List<String> farmIds) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.role = role;
        this.active = active;
        this.avatarUrl = avatarUrl;
        this.plan = plan;
        this.farmIds = farmIds;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }

    public String getPlan() {
        return plan;
    }

    public void setPlan(String plan) {
        this.plan = plan;
    }

    public List<String> getFarmIds() {
        return farmIds;
    }

    public void setFarmIds(List<String> farmIds) {
        this.farmIds = farmIds;
    }
}

exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.user.mapper;

import com.jpsoftware.farmapp.user.dto.CreateUserRequest;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserEntity toEntity(CreateUserRequest request) {
        UserEntity userEntity = new UserEntity();
        userEntity.setName(request.getName());
        userEntity.setEmail(request.getEmail());
        userEntity.setRole(request.getRole());
        userEntity.setAvatarUrl(request.getAvatarUrl());
        return userEntity;
    }

    public UserResponse toResponse(UserEntity entity) {
        return toResponse(entity, List.of());
    }

    public UserResponse toResponse(UserEntity entity, List<String> farmIds) {
        return new UserResponse(
                entity.getId(),
                entity.getName(),
                entity.getEmail(),
                entity.getRole(),
                entity.isActive(),
                entity.getAvatarUrl(),
                resolvePlan(entity).name(),
                farmIds);
    }

    private UserPlan resolvePlan(UserEntity entity) {
        return entity.getPlan() != null ? entity.getPlan() : UserPlan.defaultPlan();
    }
}

exec
/bin/bash -lc "rg -n \"RequiresPlanFeature|PlanFeature\\.CSV_EXPORT|PlanFeature\\.DASHBOARD|PlanFeature\\.ANALYTICS\" backend/farmapp/src/main/java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:6:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java:79:    @RequiresPlanFeature(PlanFeature.CSV_EXPORT)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:13:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/production/controller/ProductionController.java:73:    @RequiresPlanFeature(PlanFeature.CSV_EXPORT)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:9:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feed/controller/FeedTypeController.java:77:    @RequiresPlanFeature(PlanFeature.CSV_EXPORT)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java:6:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java:32:    @RequiresPlanFeature(PlanFeature.DASHBOARD)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/dashboard/controller/DashboardController.java:71:    @RequiresPlanFeature(PlanFeature.DASHBOARD)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:9:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/milkprice/controller/MilkPriceController.java:90:    @RequiresPlanFeature(PlanFeature.CSV_EXPORT)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:8:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/analytics/controller/AnalyticsController.java:25:@RequiresPlanFeature(PlanFeature.ANALYTICS)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:11:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/animal/controller/AnimalController.java:79:    @RequiresPlanFeature(PlanFeature.CSV_EXPORT)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/RequiresPlanFeature.java:10:public @interface RequiresPlanFeature {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:31:        RequiresPlanFeature requiresPlanFeature = AnnotatedElementUtils.findMergedAnnotation(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:33:                RequiresPlanFeature.class);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan/PlanFeatureAccessInterceptor.java:37:                    RequiresPlanFeature.class);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:11:import com.jpsoftware.farmapp.shared.plan.RequiresPlanFeature;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/feeding/controller/FeedingController.java:102:    @RequiresPlanFeature(PlanFeature.CSV_EXPORT)

exec
/bin/bash -lc "rg -n \"plan\\.|pricing|upgrade|PlanUpgradeNotice|PlanRoute|CSV_EXPORT\" frontend/web/farm_web/src/i18n frontend/web/farm_web/src/pages frontend/web/farm_web/src/components" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
frontend/web/farm_web/src/i18n/en.ts:83:    description: 'The free plan keeps the basic operational workflows available, while advanced capabilities stay on the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:90:    upgradeHint: 'Upgrade the account plan to unlock this feature without changing the rest of the workflow.',
frontend/web/farm_web/src/i18n/en.ts:94:        description: 'The consolidated financial and operational dashboard is part of the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:98:        description: 'The analytical time series and visual comparisons require the Premium plan.',
frontend/web/farm_web/src/i18n/en.ts:102:        description: 'Data and report exports in CSV are available only on the Premium plan.',
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx:24:  const accessState = getFeatureAccessState(user, 'CSV_EXPORT')
frontend/web/farm_web/src/components/common/ExportCsvButton.tsx:29:      ? `${label} (${t('plan.badge')})`
frontend/web/farm_web/src/i18n/pt-BR.ts:90:    upgradeHint: 'Atualize o plano da conta para liberar este recurso sem alterar o restante do seu fluxo.',
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:4:interface PlanUpgradeNoticeProps {
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:8:function PlanUpgradeNotice({ feature }: PlanUpgradeNoticeProps) {
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:15:        <p className="animals-page__eyebrow">{t('plan.eyebrow')}</p>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:16:        <h1>{t('plan.title')}</h1>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:17:        <p className="animals-page__description">{t('plan.description')}</p>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:20:      <section className="animals-panel plan-upgrade-notice">
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:21:        <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:24:        <p>{t('plan.upgradeHint')}</p>
frontend/web/farm_web/src/components/common/PlanUpgradeNotice.tsx:30:export default PlanUpgradeNotice
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:3:import PlanUpgradeNotice from '../common/PlanUpgradeNotice'
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:6:interface PlanRouteProps {
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:11:function PlanRoute({ children, feature }: PlanRouteProps) {
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:16:    return <PlanUpgradeNotice feature={feature} />
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:22:export default PlanRoute

codex
**Target Areas**

- Frontend access rules: [frontend/web/farm_web/src/utils/planAccess.ts](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/utils/planAccess.ts), [frontend/web/farm_web/src/components/auth/PlanRoute.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/components/auth/PlanRoute.tsx), [frontend/web/farm_web/src/components/common/ExportCsvButton.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/components/common/ExportCsvButton.tsx), [frontend/web/farm_web/src/layout/AppLayout.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/layout/AppLayout.tsx), [frontend/web/farm_web/src/App.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/App.tsx)
- Frontend shared UI/i18n: `src/components/common` for the modal, `src/hooks` or `src/context` for a shared trigger, and [frontend/web/farm_web/src/i18n/pt-BR.ts](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/i18n/pt-BR.ts) / [frontend/web/farm_web/src/i18n/en.ts](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/i18n/en.ts)
- Frontend new page: add a reusable `PlansPage` under `src/pages`, routed from `App.tsx`
- Backend audit only unless a gap is found: [backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan), controller `@RequiresPlanFeature` usage, and [backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java)

**Incremental Plan**

1. Confirm the restricted feature matrix and keep `planAccess.ts` as the single frontend source of truth.
2. Replace the current inline restriction notice pattern with a shared upgrade-flow trigger: one modal component plus one hook/context API such as `openUpgradePrompt(feature)`.
3. Wire the shared trigger into all existing premium entry points first: route guards (`PlanRoute`), restricted navigation items in `AppLayout`, and CSV export buttons. The goal is to open the same modal instead of silently disabling or rendering a dead-end notice.
4. Add a dedicated `/plans` page with localized copy and a simple Free vs Paid comparison. Keep it conceptual/static to avoid backend contract changes or payment work.
5. Make the modal CTA always navigate to `/plans`, preserving a consistent path regardless of where the restriction started.
6. Keep backend enforcement as-is for direct API access; only add backend changes if audit finds an endpoint missing `@RequiresPlanFeature` coverage.

**Risks**

- Current restricted nav/export affordances are disabled or replaced inline, so they do not count as a real “attempt” flow; converting them without harming accessibility is the main UI risk.
- Route-level access needs a clear behavior for deep links (`/dashboard`, `/analytics`) so users see a modal and still have a sane fallback page state.
- Role gating and plan gating overlap on manager-only features; messaging must avoid showing an upgrade prompt where the real reason is `403` by role.
- Backend `403` responses do not expose a structured “plan restriction” code, so frontend consistency should rely primarily on known client-side feature gates, not fragile error-message matching.

**Validation**

- Free user clicks restricted nav item, export action, or direct restricted route and always gets the same upgrade modal with the correct feature copy.
- Modal CTA routes to `/plans` successfully from every entry point.
- Plans page is accessible, localized, and clearly explains Free vs Paid without checkout/payment.
- Pro users retain normal access to dashboard, analytics, and CSV export.
- Existing backend plan tests remain green, and frontend plan-access tests are expanded to cover modal trigger plus plans-page navigation.
tokens used
54,634
**Target Areas**

- Frontend access rules: [frontend/web/farm_web/src/utils/planAccess.ts](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/utils/planAccess.ts), [frontend/web/farm_web/src/components/auth/PlanRoute.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/components/auth/PlanRoute.tsx), [frontend/web/farm_web/src/components/common/ExportCsvButton.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/components/common/ExportCsvButton.tsx), [frontend/web/farm_web/src/layout/AppLayout.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/layout/AppLayout.tsx), [frontend/web/farm_web/src/App.tsx](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/App.tsx)
- Frontend shared UI/i18n: `src/components/common` for the modal, `src/hooks` or `src/context` for a shared trigger, and [frontend/web/farm_web/src/i18n/pt-BR.ts](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/i18n/pt-BR.ts) / [frontend/web/farm_web/src/i18n/en.ts](/home/juaneugenio/programmig/farm_app/frontend/web/farm_web/src/i18n/en.ts)
- Frontend new page: add a reusable `PlansPage` under `src/pages`, routed from `App.tsx`
- Backend audit only unless a gap is found: [backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/plan), controller `@RequiresPlanFeature` usage, and [backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java)

**Incremental Plan**

1. Confirm the restricted feature matrix and keep `planAccess.ts` as the single frontend source of truth.
2. Replace the current inline restriction notice pattern with a shared upgrade-flow trigger: one modal component plus one hook/context API such as `openUpgradePrompt(feature)`.
3. Wire the shared trigger into all existing premium entry points first: route guards (`PlanRoute`), restricted navigation items in `AppLayout`, and CSV export buttons. The goal is to open the same modal instead of silently disabling or rendering a dead-end notice.
4. Add a dedicated `/plans` page with localized copy and a simple Free vs Paid comparison. Keep it conceptual/static to avoid backend contract changes or payment work.
5. Make the modal CTA always navigate to `/plans`, preserving a consistent path regardless of where the restriction started.
6. Keep backend enforcement as-is for direct API access; only add backend changes if audit finds an endpoint missing `@RequiresPlanFeature` coverage.

**Risks**

- Current restricted nav/export affordances are disabled or replaced inline, so they do not count as a real “attempt” flow; converting them without harming accessibility is the main UI risk.
- Route-level access needs a clear behavior for deep links (`/dashboard`, `/analytics`) so users see a modal and still have a sane fallback page state.
- Role gating and plan gating overlap on manager-only features; messaging must avoid showing an upgrade prompt where the real reason is `403` by role.
- Backend `403` responses do not expose a structured “plan restriction” code, so frontend consistency should rely primarily on known client-side feature gates, not fragile error-message matching.

**Validation**

- Free user clicks restricted nav item, export action, or direct restricted route and always gets the same upgrade modal with the correct feature copy.
- Modal CTA routes to `/plans` successfully from every entry point.
- Plans page is accessible, localized, and clearly explains Free vs Paid without checkout/payment.
- Pro users retain normal access to dashboard, analytics, and CSV export.
- Existing backend plan tests remain green, and frontend plan-access tests are expanded to cover modal trigger plus plans-page navigation.
