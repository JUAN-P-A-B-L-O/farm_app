OpenAI Codex v0.128.0 (research preview)
--------
workdir: /home/juaneugenio/programmig/farm_app
model: gpt-5.4
provider: openai
approval: never
sandbox: workspace-write [workdir, /tmp, /home/juaneugenio/.codex/memories]
reasoning effort: high
reasoning summaries: none
session id: 019e2de0-320a-7203-885b-192e2763aeed
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
# Feature: Integrate Brevo SMTP for Email Confirmation

## Goal
Enable real email sending for account confirmation using Brevo SMTP, keeping the implementation simple, secure, and decoupled.

## Scope
- Backend: email sending infrastructure
- User registration flow (email confirmation trigger)

## Requirements
- Use Brevo SMTP:
  - Host: smtp-relay.brevo.com
  - Port: 587
  - TLS/STARTTLS enabled
- Credentials and configs must come from environment variables:
  - BREVO_SMTP_USERNAME
  - BREVO_SMTP_PASSWORD
  - APP_MAIL_FROM
  - APP_FRONTEND_URL
- Create abstraction:
  - Interface: EmailSender
  - Implementation: BrevoEmailSender or SmtpEmailSender
- Send confirmation email with link:
  `${APP_FRONTEND_URL}/confirm-email?token={token}`
- Do NOT log sensitive data (token, password)
- Handle email sending failures safely (do not break user creation flow)

## Constraints
- Do NOT use messaging/queues
- Do NOT couple user logic directly to Brevo implementation
- Do NOT hardcode credentials
- Keep changes incremental and localized
- Follow existing architecture and patterns

## Implementation Notes
- Add dependency: spring-boot-starter-mail (if missing)
- Configure SMTP via application.properties using env vars
- Implement EmailSender abstraction and isolate provider logic
- Integrate email sending into confirmation flow (post user creation)
- Ensure secure handling of tokens and credentials
- Keep implementation simple and replaceable in future

## Validation
- Confirmation email is sent successfully using Brevo SMTP
- Link contains correct token and frontend URL
- Credentials are not exposed in logs or code
- Application does not fail if email sending fails
- No regression in user creation flow

## Done Criteria
- EmailSender abstraction implemented
- Brevo SMTP integration working
- Confirmation emails are sent with correct link
- Configuration is fully externalized via environment variables
- System remains secure and decoupled

Rules:
- Do not write code.
- Keep the plan concise.
- Identify target files/modules if possible.
- Identify risks.
- Identify validation steps.
- Prefer incremental execution.
warning: Codex could not find bubblewrap on PATH. Install bubblewrap with your OS package manager. See the sandbox prerequisites: https://developers.openai.com/codex/concepts/sandboxing#prerequisites. Codex will use the vendored bubblewrap in the meantime.
codex
I’m mapping the existing registration and email-confirmation flow first so the plan targets the real modules, not assumptions. Then I’ll outline the smallest backend changes, risks, and validation steps.
exec
/bin/bash -lc 'rg -n "spring-boot-starter-mail|spring.mail|JavaMailSender" backend/farmapp/pom.xml backend/farmapp/src backend/farmapp/src/main/resources || true' in /home/juaneugenio/programmig/farm_app
exec
/bin/bash -lc 'rg -n "confirm-email|confirmation|EmailSender|JavaMailSender|mail" backend/farmapp/src || true' in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
exec
/bin/bash -lc 'rg --files backend/farmapp/src | rg "user|auth|mail|email|confirm|token|properties|yaml"' in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
backend/farmapp/src/test/resources/application.properties
backend/farmapp/src/main/resources/application-dev.properties
backend/farmapp/src/main/resources/application.properties
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql
backend/farmapp/src/main/resources/application-local.properties
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdatePasswordRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/ActivateUserRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserPlan.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserFarmAssignmentEntity.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserFarmAssignmentRepository.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/controller/UserController.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAuthenticationEntryPoint.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtAuthenticationFilter.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/JwtTokenService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/model/AuthenticatedUser.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SecurityConfig.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailConfiguration.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/RestAccessDeniedHandler.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/JwtTokenServiceTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthenticationContextService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/TokenService.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginResponse.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserMapperTest.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/model/EmailMessage.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailConfirmationRequiredException.java
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailDispatchException.java

 succeeded in 0ms:
backend/farmapp/src/main/resources/db/migration/V1__baseline_schema.sql:4:    email VARCHAR(255) NOT NULL,
backend/farmapp/src/main/resources/db/migration/V1__baseline_schema.sql:8:    email_confirmed BOOLEAN NOT NULL DEFAULT TRUE,
backend/farmapp/src/main/resources/db/migration/V1__baseline_schema.sql:9:    email_confirmation_token_hash VARCHAR(255),
backend/farmapp/src/main/resources/db/migration/V1__baseline_schema.sql:10:    email_confirmation_token_expires_at TIMESTAMP(6) WITH TIME ZONE,
backend/farmapp/src/main/resources/db/migration/V1__baseline_schema.sql:16:CREATE UNIQUE INDEX uk_users_email ON users (email);
backend/farmapp/src/main/resources/application-local.properties:10:app.email.smtp.host=localhost
backend/farmapp/src/main/resources/application-local.properties:11:app.email.smtp.port=1025
backend/farmapp/src/main/resources/application-local.properties:12:app.email.smtp.auth=false
backend/farmapp/src/main/resources/application-local.properties:13:app.email.smtp.starttls-enabled=false
backend/farmapp/src/main/resources/application.properties:17:app.auth.email-confirmation.frontend-base-url=${APP_AUTH_EMAIL_CONFIRMATION_FRONTEND_BASE_URL:http://localhost:5173}
backend/farmapp/src/main/resources/application.properties:18:app.auth.email-confirmation.token-expiration-hours=${APP_AUTH_EMAIL_CONFIRMATION_TOKEN_EXPIRATION_HOURS:24}
backend/farmapp/src/main/resources/application.properties:19:app.email.enabled=${APP_EMAIL_ENABLED:false}
backend/farmapp/src/main/resources/application.properties:20:app.email.from=${APP_EMAIL_FROM:no-reply@farmapp.local}
backend/farmapp/src/main/resources/application.properties:21:app.email.confirmation.subject=${APP_EMAIL_CONFIRMATION_SUBJECT:Confirme sua conta no Farm App}
backend/farmapp/src/main/resources/application.properties:22:app.email.smtp.host=${APP_EMAIL_SMTP_HOST:}
backend/farmapp/src/main/resources/application.properties:23:app.email.smtp.port=${APP_EMAIL_SMTP_PORT:587}
backend/farmapp/src/main/resources/application.properties:24:app.email.smtp.username=${APP_EMAIL_SMTP_USERNAME:}
backend/farmapp/src/main/resources/application.properties:25:app.email.smtp.password=${APP_EMAIL_SMTP_PASSWORD:}
backend/farmapp/src/main/resources/application.properties:26:app.email.smtp.auth=${APP_EMAIL_SMTP_AUTH:true}
backend/farmapp/src/main/resources/application.properties:27:app.email.smtp.starttls-enabled=${APP_EMAIL_SMTP_STARTTLS_ENABLED:true}
backend/farmapp/src/main/resources/application.properties:28:app.email.smtp.protocol=${APP_EMAIL_SMTP_PROTOCOL:smtp}
backend/farmapp/src/main/resources/application.properties:29:app.email.smtp.connection-timeout-ms=${APP_EMAIL_SMTP_CONNECTION_TIMEOUT_MS:5000}
backend/farmapp/src/main/resources/application.properties:30:app.email.smtp.timeout-ms=${APP_EMAIL_SMTP_TIMEOUT_MS:5000}
backend/farmapp/src/main/resources/application.properties:31:app.email.smtp.write-timeout-ms=${APP_EMAIL_SMTP_WRITE_TIMEOUT_MS:5000}
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:2:    ADD COLUMN IF NOT EXISTS email_confirmed BOOLEAN;
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:5:SET email_confirmed = TRUE
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:6:WHERE email_confirmed IS NULL;
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:9:    ALTER COLUMN email_confirmed SET DEFAULT TRUE;
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:12:    ALTER COLUMN email_confirmed SET NOT NULL;
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:15:    ADD COLUMN IF NOT EXISTS email_confirmation_token_hash VARCHAR(255);
backend/farmapp/src/main/resources/db/migration/V2__add_user_email_confirmation_columns.sql:18:    ADD COLUMN IF NOT EXISTS email_confirmation_token_expires_at TIMESTAMP(6) WITH TIME ZONE;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:75:                request.getEmail(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:84:        userEntity.setEmail(normalizeEmail(request.getEmail()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:87:        userEntity.setEmailConfirmed(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:102:        return userRepository.findAll(Sort.by(Sort.Direction.ASC, "name", "email")).stream()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:114:                PageRequest.of(page, size, Sort.by(Sort.Direction.ASC, "name", "email")));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:129:                new CsvColumn<>("email", UserResponse::getEmail),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:154:                request.getEmail(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:162:        userEntity.setEmail(normalizeEmail(request.getEmail()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:254:            String email,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:260:        validateRequiredText(email, "email must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:273:        String normalizedEmail = normalizeEmail(email);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:274:        userRepository.findByEmail(normalizedEmail)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:277:                    throw new ConflictException("User with this email already exists");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:344:                || userEntity.getEmail().toLowerCase(Locale.ROOT).contains(normalizedSearch);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:367:                        criteriaBuilder.like(criteriaBuilder.lower(root.get("email")), searchPattern));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:398:    private String normalizeEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/service/UserService.java:399:        return email.trim().toLowerCase(Locale.ROOT);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:16:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:27:    private EmailSender emailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:40:                                  "email": "worker.one@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:50:                .andExpect(jsonPath("$.email").value("worker.one@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:55:        UserEntity createdUser = userRepository.findByEmail("worker.one@farm.com").orElseThrow();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:63:                                  "email": "worker.one@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:106:                                  "email": "worker.two@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:117:    void shouldRejectDuplicateEmailDuringUserCreation() throws Exception {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:123:        existingUser.setEmail("worker.one@farm.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:135:                                  "email": "worker.one@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:144:                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:158:                                  "email": "worker.three@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:181:                                  "email": "worker.four@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:204:                                  "email": "worker.unsafe@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:230:                                  "email": "updated.worker@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:238:                .andExpect(jsonPath("$.email").value("updated.worker@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:244:        assertEquals("updated.worker@farm.com", updatedUser.getEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:265:                                  "email": "%s",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:268:                                """.formatted(worker.getEmail())))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:298:                                  "email": "%s",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:301:                                """.formatted(worker.getEmail())))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:350:                                  "email": "%s",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:353:                                """.formatted(worker.getEmail())))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:360:                                  "email": "%s",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:363:                                """.formatted(worker.getEmail())))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:373:        inactiveWorker.setEmail("pedro.worker@farm.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:384:                .andExpect(jsonPath("$[0].email").value("pedro.worker@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/user/UserIntegrationTest.java:400:                                  "email": "updated.worker@farm.com",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:15:    @NotBlank(message = "email must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:16:    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:17:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:35:    public UpdateUserRequest(String name, String email, String role, String avatarUrl, List<String> farmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:37:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:51:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:52:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:55:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UpdateUserRequest.java:56:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:16:    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:17:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:39:    public UserResponse(UUID id, String name, String email, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:40:        this(id, name, email, role, null, null, null, null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:43:    public UserResponse(UUID id, String name, String email, String role, Boolean active, String avatarUrl, List<String> farmIds) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:44:        this(id, name, email, role, active, avatarUrl, null, farmIds);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:50:            String email,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:58:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:82:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:83:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:86:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/UserResponse.java:87:        this.email = email;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java:10:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/plan/PlanFeatureAccessIntegrationTest.java:20:    private EmailSender emailSender;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:16:    @NotBlank(message = "email must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:17:    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:18:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:43:    public CreateUserRequest(String name, String email, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:44:        this(name, email, role, null, true, null, List.of());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:47:    public CreateUserRequest(String name, String email, String role, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:48:        this(name, email, role, password, true, null, List.of());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:53:            String email,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:60:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:76:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:77:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:80:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/dto/CreateUserRequest.java:81:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:3:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:4:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:5:import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:24:@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:25:public class SmtpEmailSender implements EmailSender {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:27:    private final EmailProperties emailProperties;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:31:    public SmtpEmailSender(EmailProperties emailProperties) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:32:        this(emailProperties, new SocketSmtpTransport());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:35:    public SmtpEmailSender(EmailProperties emailProperties, SmtpTransport smtpTransport) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:36:        this.emailProperties = emailProperties;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:41:    public void send(EmailMessage emailMessage) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:42:        validateEmailMessage(emailMessage);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:45:                    emailProperties,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:47:                    emailMessage.recipientEmail().trim(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:48:                    buildMessage(emailMessage));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:50:            throw new EmailDispatchException("Unable to send email", exception);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:54:    private void validateEmailMessage(EmailMessage emailMessage) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:55:        if (emailMessage == null) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:56:            throw new IllegalArgumentException("emailMessage must not be null");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:58:        if (!StringUtils.hasText(emailMessage.recipientEmail())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:59:            throw new IllegalArgumentException("recipientEmail must not be blank");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:61:        if (!StringUtils.hasText(emailMessage.subject())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:64:        if (!StringUtils.hasText(emailMessage.body())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:70:        if (!StringUtils.hasText(emailProperties.getFrom())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:71:            throw new IllegalStateException("app.email.from must be configured when app.email.enabled is true");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:73:        return emailProperties.getFrom().trim();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:76:    private String buildMessage(EmailMessage emailMessage) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:78:                + "To: <" + emailMessage.recipientEmail().trim() + ">\r\n"
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:79:                + "Subject: " + encodeHeader(emailMessage.subject().trim()) + "\r\n"
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:84:                + escapeMessageBody(encodeBody(emailMessage.body()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:115:        void send(EmailProperties emailProperties, String fromAddress, String recipientEmail, String message)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:122:        public void send(EmailProperties emailProperties, String fromAddress, String recipientEmail, String message)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:124:            try (SmtpSession smtpSession = SmtpSession.open(emailProperties)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:128:                if (emailProperties.getSmtp().isStarttlsEnabled()
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:129:                        && !"smtps".equalsIgnoreCase(emailProperties.getSmtp().getProtocol())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:133:                if (emailProperties.getSmtp().isAuth()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:138:                smtpSession.sendCommand("RCPT TO:<" + recipientEmail + ">", 250);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:152:        private final EmailProperties emailProperties;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:154:        private SmtpSession(Socket socket, EmailProperties emailProperties) throws IOException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:156:            this.emailProperties = emailProperties;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:160:        static SmtpSession open(EmailProperties emailProperties) throws IOException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:161:            EmailProperties.Smtp smtp = emailProperties.getSmtp();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:163:                throw new IllegalStateException("app.email.smtp.host must be configured when app.email.enabled is true");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:167:                        "app.email.smtp.username and app.email.smtp.password must be configured when app.email.smtp.auth is true");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:184:            return new SmtpSession(socket, emailProperties);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:196:                    emailProperties.getSmtp().getHost().trim(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:197:                    emailProperties.getSmtp().getPort(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:206:            sendCommand(base64(emailProperties.getSmtp().getUsername().trim()), 334);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java:207:            sendCommand(base64(emailProperties.getSmtp().getPassword()), 235);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailConfiguration.java:7:@EnableConfigurationProperties(EmailProperties.class)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailConfiguration.java:8:public class EmailConfiguration {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:5:@ConfigurationProperties(prefix = "app.email")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:6:public class EmailProperties {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:10:    private final Confirmation confirmation = new Confirmation();
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java:30:        return confirmation;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:3:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:4:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:11:@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "false", matchIfMissing = true)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:12:public class LoggingEmailSender implements EmailSender {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:14:    private static final Logger logger = LoggerFactory.getLogger(LoggingEmailSender.class);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:17:    public void send(EmailMessage emailMessage) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:19:                "SMTP email delivery is disabled. Transactional email to {} with subject '{}': {}",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:20:                emailMessage.recipientEmail(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:21:                emailMessage.subject(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java:22:                emailMessage.body());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:27:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:39:    private boolean emailConfirmed = true;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:42:    private String emailConfirmationTokenHash;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:45:    private Instant emailConfirmationTokenExpiresAt;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:57:    public UserEntity(UUID id, String name, String email, String role) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:58:        this(id, name, email, role, "", true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:61:    public UserEntity(UUID id, String name, String email, String role, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:62:        this(id, name, email, role, password, true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:65:    public UserEntity(UUID id, String name, String email, String role, String password, boolean active) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:68:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:91:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:92:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:95:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:96:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:123:    public boolean isEmailConfirmed() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:124:        return emailConfirmed;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:127:    public void setEmailConfirmed(boolean emailConfirmed) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:128:        this.emailConfirmed = emailConfirmed;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:131:    public String getEmailConfirmationTokenHash() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:132:        return emailConfirmationTokenHash;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:135:    public void setEmailConfirmationTokenHash(String emailConfirmationTokenHash) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:136:        this.emailConfirmationTokenHash = emailConfirmationTokenHash;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:139:    public Instant getEmailConfirmationTokenExpiresAt() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:140:        return emailConfirmationTokenExpiresAt;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:143:    public void setEmailConfirmationTokenExpiresAt(Instant emailConfirmationTokenExpiresAt) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/entity/UserEntity.java:144:        this.emailConfirmationTokenExpiresAt = emailConfirmationTokenExpiresAt;
backend/farmapp/src/test/resources/application.properties:13:app.auth.email-confirmation.frontend-base-url=http://localhost:5173
backend/farmapp/src/test/resources/application.properties:14:app.auth.email-confirmation.token-expiration-hours=24
backend/farmapp/src/test/resources/application.properties:15:app.email.enabled=false
backend/farmapp/src/test/resources/application.properties:16:app.email.confirmation.subject=Confirme sua conta no Farm App
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:12:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:13:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:30:    private EmailSender emailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:39:                                  "email": "maria@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:45:                .andExpect(jsonPath("$.email").value("maria@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:52:        UserEntity registeredUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:54:        Assertions.assertFalse(registeredUser.isEmailConfirmed());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:58:        Assertions.assertNotNull(registeredUser.getEmailConfirmationTokenHash());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:59:        Assertions.assertNotNull(registeredUser.getEmailConfirmationTokenExpiresAt());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:61:        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:62:        verify(emailSender).send(emailCaptor.capture());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:63:        EmailMessage sentEmail = emailCaptor.getValue();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:64:        Assertions.assertEquals("maria@farm.com", sentEmail.recipientEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:65:        Assertions.assertEquals("Confirme sua conta no Farm App", sentEmail.subject());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:66:        Assertions.assertTrue(sentEmail.body().contains("Olá Maria Silva,"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:67:        Assertions.assertTrue(sentEmail.body().contains("http://localhost:5173/login?mode=confirm&token="));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:68:        String rawToken = sentEmail.body()
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:69:                .substring(sentEmail.body().indexOf("token=") + "token=".length())
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:72:                emailConfirmationTokenService.hashToken(rawToken),
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:73:                registeredUser.getEmailConfirmationTokenHash());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:77:    void shouldRejectDuplicateEmailDuringRegistration() throws Exception {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:91:                                  "email": "maria@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:96:                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:100:    void shouldRejectDuplicateEmailDuringRegistrationIgnoringCaseAndWhitespace() throws Exception {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:114:                                  "email": "  MARIA@FARM.COM  ",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:119:                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:129:                                  "email": "  MARIA@FARM.COM  ",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:135:                .andExpect(jsonPath("$.email").value("maria@farm.com"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:137:        UserEntity registeredUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:140:        Assertions.assertFalse(registeredUser.isEmailConfirmed());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:150:                                  "email": "maria@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:168:        user.setEmailConfirmed(true);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:175:                                  "email": "jane@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:181:                .andExpect(jsonPath("$.user.email").value("jane@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:195:        user.setEmailConfirmed(false);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:196:        user.setEmailConfirmationTokenHash(emailConfirmationTokenService.hashToken("pending-token"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:197:        user.setEmailConfirmationTokenExpiresAt(Instant.now().plusSeconds(3600));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:204:                                  "email": "jane@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:209:                .andExpect(jsonPath("$.error").value("Confirme seu e-mail antes de entrar."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:221:        user.setEmailConfirmed(true);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:228:                                  "email": "jane@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:233:                .andExpect(jsonPath("$.error").value("E-mail ou senha inválidos."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:245:        user.setEmailConfirmed(true);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:252:                                  "email": "jane@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:257:                .andExpect(jsonPath("$.error").value("E-mail ou senha inválidos."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:261:    void shouldConfirmEmailAndAllowLogin() throws Exception {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:269:        user.setEmailConfirmed(false);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:270:        user.setEmailConfirmationTokenHash(emailConfirmationTokenService.hashToken("confirm-token"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:271:        user.setEmailConfirmationTokenExpiresAt(Instant.now().plusSeconds(3600));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:274:        mockMvc.perform(get("/auth/confirm-email")
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:277:                .andExpect(jsonPath("$.message").value("E-mail confirmado com sucesso."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:279:        UserEntity confirmedUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:280:        Assertions.assertTrue(confirmedUser.isEmailConfirmed());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:281:        Assertions.assertNull(confirmedUser.getEmailConfirmationTokenHash());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:282:        Assertions.assertNull(confirmedUser.getEmailConfirmationTokenExpiresAt());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:288:                                  "email": "maria@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:294:                .andExpect(jsonPath("$.user.email").value("maria@farm.com"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:299:        mockMvc.perform(get("/auth/confirm-email")
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:314:        user.setEmailConfirmed(false);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:315:        user.setEmailConfirmationTokenHash(emailConfirmationTokenService.hashToken("expired-token"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:316:        user.setEmailConfirmationTokenExpiresAt(Instant.now().minusSeconds(60));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:319:        mockMvc.perform(get("/auth/confirm-email")
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:326:    void shouldResendConfirmationEmail() throws Exception {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:334:        user.setEmailConfirmed(false);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:335:        user.setEmailConfirmationTokenHash(emailConfirmationTokenService.hashToken("old-token"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:336:        user.setEmailConfirmationTokenExpiresAt(Instant.now().plusSeconds(300));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:338:        String previousTokenHash = user.getEmailConfirmationTokenHash();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:340:        mockMvc.perform(post("/auth/confirm-email/resend")
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:344:                                  "email": "  MARIA@FARM.COM  "
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:348:                .andExpect(jsonPath("$.message").value("E-mail de confirmação enviado com sucesso."));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:350:        UserEntity updatedUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:351:        Assertions.assertFalse(updatedUser.isEmailConfirmed());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:352:        Assertions.assertNotEquals(previousTokenHash, updatedUser.getEmailConfirmationTokenHash());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:353:        Assertions.assertTrue(updatedUser.getEmailConfirmationTokenExpiresAt().isAfter(Instant.now()));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:355:        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:356:        verify(emailSender).send(emailCaptor.capture());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:357:        EmailMessage sentEmail = emailCaptor.getValue();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:358:        Assertions.assertEquals("maria@farm.com", sentEmail.recipientEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:359:        Assertions.assertTrue(sentEmail.body().contains("http://localhost:5173/login?mode=confirm&token="));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:360:        String rawToken = sentEmail.body()
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:361:                .substring(sentEmail.body().indexOf("token=") + "token=".length())
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:364:                emailConfirmationTokenService.hashToken(rawToken),
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:365:                updatedUser.getEmailConfirmationTokenHash());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:370:        mockMvc.perform(post("/auth/confirm-email/resend")
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:374:                                  "email": "missing@farm.com"
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:379:                .andExpect(jsonPath("$.path").value("/auth/confirm-email/resend"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:391:        user.setEmailConfirmed(true);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:394:        mockMvc.perform(post("/auth/confirm-email/resend")
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:398:                                  "email": "maria@farm.com"
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:402:                .andExpect(jsonPath("$.error").value("O e-mail já foi confirmado."))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java:403:                .andExpect(jsonPath("$.path").value("/auth/confirm-email/resend"));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationTokenService.java:11:public class EmailConfirmationTokenService {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:47:            @ApiResponse(responseCode = "403", description = "Email not confirmed",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:51:        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:55:    @Operation(summary = "Register account", description = "Creates a new manager account pending email confirmation.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:60:            @ApiResponse(responseCode = "409", description = "Email already in use",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:67:    @GetMapping("/confirm-email")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:68:    @Operation(summary = "Confirm email", description = "Validates the email confirmation token and activates email access.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:70:            @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:73:            @ApiResponse(responseCode = "409", description = "Email already confirmed",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:76:    public ResponseEntity<MessageResponse> confirmEmail(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:78:        return ResponseEntity.ok(authService.confirmEmail(token));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:81:    @PostMapping("/confirm-email/resend")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:82:    @Operation(summary = "Resend confirmation email", description = "Generates a new confirmation token and resends the confirmation email.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:84:            @ApiResponse(responseCode = "200", description = "Confirmation email resent successfully"),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:89:            @ApiResponse(responseCode = "409", description = "Email already confirmed",
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java:93:        return ResponseEntity.ok(authService.resendConfirmation(request.getEmail()));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserRepository.java:11:    Optional<UserEntity> findByEmail(String email);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/repository/UserRepository.java:13:    Optional<UserEntity> findByEmailConfirmationTokenHash(String emailConfirmationTokenHash);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/farm/FarmOnboardingAccessIntegrationTest.java:10:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/farm/FarmOnboardingAccessIntegrationTest.java:19:    private EmailSender emailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/farm/FarmOnboardingAccessIntegrationTest.java:72:                                  "email": "%s",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/farm/FarmOnboardingAccessIntegrationTest.java:75:                                """.formatted(user.getEmail())))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:3:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:4:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:19:public class EmailConfirmationService {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:22:    private final EmailConfirmationTokenService tokenService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:23:    private final EmailSender emailSender;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:25:    private final String confirmationSubject;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:28:    public EmailConfirmationService(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:30:            EmailConfirmationTokenService tokenService,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:31:            EmailSender emailSender,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:32:            @Value("${app.auth.email-confirmation.frontend-base-url}") String frontendBaseUrl,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:33:            @Value("${app.email.confirmation.subject}") String confirmationSubject,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:34:            @Value("${app.auth.email-confirmation.token-expiration-hours}") long tokenExpirationHours) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:37:        this.emailSender = emailSender;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:39:        this.confirmationSubject = confirmationSubject;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:49:    public void resendConfirmation(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:50:        UserEntity userEntity = userRepository.findByEmail(normalizeEmail(email))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:53:        if (userEntity.isEmailConfirmed()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:54:            throw new ConflictException("Email already confirmed");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:61:    public void confirmEmail(String token) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:67:        UserEntity userEntity = userRepository.findByEmailConfirmationTokenHash(tokenHash)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:68:                .orElseThrow(() -> new ValidationException("Invalid or expired email confirmation token"));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:70:        if (userEntity.isEmailConfirmed()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:71:            throw new ConflictException("Email already confirmed");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:73:        if (userEntity.getEmailConfirmationTokenExpiresAt() == null
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:74:                || userEntity.getEmailConfirmationTokenExpiresAt().isBefore(Instant.now())) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:75:            throw new ValidationException("Invalid or expired email confirmation token");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:78:        userEntity.setEmailConfirmed(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:79:        userEntity.setEmailConfirmationTokenHash(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:80:        userEntity.setEmailConfirmationTokenExpiresAt(null);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:86:        userEntity.setEmailConfirmed(false);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:87:        userEntity.setEmailConfirmationTokenHash(tokenService.hashToken(rawToken));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:88:        userEntity.setEmailConfirmationTokenExpiresAt(Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:91:        emailSender.send(new EmailMessage(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:92:                userEntity.getEmail(),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:93:                confirmationSubject,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:101:    private String buildConfirmationBody(String recipientName, String confirmationUrl) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:107:                Confirme seu e-mail acessando o link abaixo:
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:111:                """.formatted(resolvedName, confirmationUrl);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:114:    private String normalizeEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java:115:        return email.trim().toLowerCase(Locale.ROOT);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:7:import com.jpsoftware.farmapp.shared.exception.EmailConfirmationRequiredException;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:29:    private final EmailConfirmationService emailConfirmationService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:36:            EmailConfirmationService emailConfirmationService) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:41:        this.emailConfirmationService = emailConfirmationService;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:45:    public LoginResponse login(String email, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:46:        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:47:            throw new InvalidCredentialsException("Invalid email or password");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:50:        UserEntity user = userRepository.findByEmail(normalizeEmail(email))
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:51:                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:54:            throw new InvalidCredentialsException("Invalid email or password");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:56:        if (!user.isEmailConfirmed()) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:57:            throw new EmailConfirmationRequiredException("Email confirmation is required before login");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:71:        String normalizedEmail = normalizeEmail(request.getEmail());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:72:        userRepository.findByEmail(normalizedEmail)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:74:                    throw new ConflictException("User with this email already exists");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:79:        user.setEmail(normalizedEmail);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:83:        user.setEmailConfirmed(false);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:87:        emailConfirmationService.initializePendingConfirmation(savedUser);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:92:    public MessageResponse confirmEmail(String token) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:93:        emailConfirmationService.confirmEmail(token);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:94:        return new MessageResponse("E-mail confirmado com sucesso.");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:98:    public MessageResponse resendConfirmation(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:99:        emailConfirmationService.resendConfirmation(email);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:100:        return new MessageResponse("E-mail de confirmação enviado com sucesso.");
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:103:    private String normalizeEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java:104:        return email.trim().toLowerCase(Locale.ROOT);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:6:@Schema(description = "Request payload for resending the confirmation email.")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:9:    @NotBlank(message = "email must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:10:    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:11:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:16:    public ResendConfirmationRequest(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:17:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:20:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:21:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:24:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/ResendConfirmationRequest.java:25:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:7:    @NotBlank(message = "email must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:8:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:16:    public LoginRequest(String email, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:17:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:21:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:22:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:25:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/LoginRequest.java:26:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:13:    @NotBlank(message = "email must not be blank")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:14:    @Schema(description = "User email address.", example = "maria.silva@farmapp.com")
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:15:    private String email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:24:    public RegisterRequest(String name, String email, String password) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:26:        this.email = email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:38:    public String getEmail() {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:39:        return email;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:42:    public void setEmail(String email) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/dto/RegisterRequest.java:43:        this.email = email;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserMapperTest.java:23:        entity.setEmail("jane@farm.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserMapperTest.java:39:        entity.setEmail("legacy@farm.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:85:        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:107:        assertEquals("jane@farm.com", response.getEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:127:    void shouldFailWhenEmailIsBlank() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:132:        assertEquals("email must not be blank", exception.getMessage());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:154:    void shouldFailWhenEmailAlreadyExists() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:165:        when(userRepository.findByEmail("jane@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:170:        assertEquals("User with this email already exists", exception.getMessage());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:203:        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:222:        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:242:        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:272:        when(userRepository.findByEmail("jane@farm.com")).thenReturn(Optional.empty());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:287:        assertEquals("jane@farm.com", response.getEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:305:        when(userRepository.findByEmail("updated@farm.com")).thenReturn(Optional.empty());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:312:        assertEquals("updated@farm.com", response.getEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:426:        assertEquals("jane@farm.com", responses.get(0).getEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/user/UserServiceTest.java:489:        assertEquals("jane@farm.com", response.getEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:5:import com.jpsoftware.farmapp.auth.infrastructure.EmailConfiguration;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:6:import com.jpsoftware.farmapp.auth.infrastructure.LoggingEmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:7:import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:8:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:16:class EmailSenderConfigurationTest {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:19:    void shouldUseLoggingEmailSenderWhenEmailIsDisabled() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:20:        try (ConfigurableApplicationContext context = runContext("--app.email.enabled=false")) {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:21:            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:22:            assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:23:            assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:28:    void shouldUseLoggingEmailSenderWhenEmailPropertyIsMissing() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:30:            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:31:            assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:32:            assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:37:    void shouldUseSmtpEmailSenderWhenEmailIsEnabled() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:38:        try (ConfigurableApplicationContext context = runContext("--app.email.enabled=true")) {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:39:            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:40:            assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:41:            assertThat(context.getBeansOfType(LoggingEmailSender.class)).isEmpty();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:46:        return new SpringApplicationBuilder(EmailSenderTestApplication.class)
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:52:    @Import({EmailConfiguration.class, LoggingEmailSender.class, SmtpEmailSender.class})
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java:53:    static class EmailSenderTestApplication {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:7:import com.jpsoftware.farmapp.auth.infrastructure.EmailProperties;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:8:import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:9:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:10:import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:16:class SmtpEmailSenderTest {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:19:    void shouldSendTransactionalEmailWithConfiguredMetadata() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:20:        EmailProperties emailProperties = buildEmailProperties();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:22:        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, (properties, fromAddress, recipientEmail, message) -> {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:24:            sentMessage.recipientEmail = recipientEmail;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:28:        sender.send(new EmailMessage(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:34:        assertEquals("maria@farm.com", sentMessage.recipientEmail);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:42:    void shouldWrapMailFailuresAsEmailDispatchException() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:43:        EmailProperties emailProperties = buildEmailProperties();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:44:        SmtpEmailSender sender = new SmtpEmailSender(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:45:                emailProperties,
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:46:                (properties, fromAddress, recipientEmail, message) -> {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:50:        EmailDispatchException exception = assertThrows(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:51:                EmailDispatchException.class,
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:52:                () -> sender.send(new EmailMessage(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:57:        assertEquals("Unable to send email", exception.getMessage());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:61:    void shouldRejectInvalidEmailMessageBeforeSending() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:62:        EmailProperties emailProperties = buildEmailProperties();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:63:        SmtpEmailSender sender = new SmtpEmailSender(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:64:                emailProperties,
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:65:                (properties, fromAddress, recipientEmail, message) -> {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:70:        assertEquals("emailMessage must not be null", nullMessageException.getMessage());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:74:                () -> sender.send(new EmailMessage(" ", "Confirme sua conta", "Olá Maria")));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:75:        assertEquals("recipientEmail must not be blank", blankRecipientException.getMessage());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:79:                () -> sender.send(new EmailMessage("maria@farm.com", " ", "Olá Maria")));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:84:                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", " ")));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:90:        EmailProperties emailProperties = buildEmailProperties();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:91:        emailProperties.setFrom("   ");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:92:        SmtpEmailSender sender = new SmtpEmailSender(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:93:                emailProperties,
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:94:                (properties, fromAddress, recipientEmail, message) -> {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:100:                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", "Olá Maria")));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:102:        assertEquals("app.email.from must be configured when app.email.enabled is true", exception.getMessage());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:110:    private EmailProperties buildEmailProperties() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:111:        EmailProperties emailProperties = new EmailProperties();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:112:        emailProperties.setFrom("no-reply@farmapp.local");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:113:        emailProperties.getConfirmation().setSubject("Confirme sua conta no Farm App");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:114:        emailProperties.setEnabled(true);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:115:        emailProperties.getSmtp().setHost("smtp.example.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:116:        emailProperties.getSmtp().setPort(587);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:117:        emailProperties.getSmtp().setAuth(false);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:118:        emailProperties.getSmtp().setStarttlsEnabled(false);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:119:        return emailProperties;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java:124:        private String recipientEmail;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:10:import com.jpsoftware.farmapp.auth.service.EmailConfirmationService;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:11:import com.jpsoftware.farmapp.auth.service.EmailConfirmationTokenService;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:12:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:13:import com.jpsoftware.farmapp.shared.email.service.EmailSender;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:20:class EmailConfirmationServiceTest {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:23:    private final EmailConfirmationTokenService tokenService =
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:24:            org.mockito.Mockito.mock(EmailConfirmationTokenService.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:25:    private final EmailSender emailSender = org.mockito.Mockito.mock(EmailSender.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:28:    void shouldComposeConfirmationEmailWithConfiguredSubjectAndTrimmedFrontendBaseUrl() {
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:29:        EmailConfirmationService service = new EmailConfirmationService(
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:32:                emailSender,
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:38:        user.setEmail("maria@farm.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:47:        assertFalse(user.isEmailConfirmed());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:48:        assertEquals("hashed-token", user.getEmailConfirmationTokenHash());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:49:        assertNotNull(user.getEmailConfirmationTokenExpiresAt());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:50:        assertTrue(user.getEmailConfirmationTokenExpiresAt().isAfter(beforeIssuance));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:52:        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:53:        verify(emailSender).send(emailCaptor.capture());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:54:        EmailMessage sentEmail = emailCaptor.getValue();
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:55:        assertEquals("maria@farm.com", sentEmail.recipientEmail());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:56:        assertEquals("Confirme sua conta no Farm App", sentEmail.subject());
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:57:        assertTrue(sentEmail.body().contains("Olá usuário,"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java:58:        assertTrue(sentEmail.body().contains("http://localhost:5173/login?mode=confirm&token=raw-token"));
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:16:        userEntity.setEmail(request.getEmail());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/user/mapper/UserMapper.java:30:                entity.getEmail(),
backend/farmapp/src/test/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslatorTest.java:53:        assertEquals("Não foi possível enviar o e-mail.", ErrorMessageTranslator.translate("Unable to send email"));
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:59:                  "email": "jane@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:75:                .andExpect(jsonPath("$.email").value("jane@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:91:                .andExpect(jsonPath("$[0].email").value("jane@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:141:                .andExpect(jsonPath("$.email").value("jane@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:154:                  "email": "updated@farm.com",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:176:                .andExpect(jsonPath("$.email").value("updated@farm.com"))
backend/farmapp/src/test/java/com/jpsoftware/farmapp/contract/user/UserControllerContractTest.java:261:                  "email": " ",
backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/shared/config/DefaultAdminInitializerTest.java:52:        assertEquals("admin@farmapp.com", savedUser.getEmail());
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailDispatchException.java:3:public class EmailDispatchException extends RuntimeException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailDispatchException.java:5:    public EmailDispatchException(String message, Throwable cause) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:115:    @ExceptionHandler(EmailConfirmationRequiredException.class)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:116:    public ResponseEntity<ErrorResponse> handleEmailConfirmationRequiredException(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:117:            EmailConfirmationRequiredException exception,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:160:    @ExceptionHandler(EmailDispatchException.class)
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:161:    public ResponseEntity<ErrorResponse> handleEmailDispatchException(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:162:            EmailDispatchException exception,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:196:        if (message != null && message.toLowerCase().contains("email")) {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java:197:            return "User with this email already exists";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/model/EmailMessage.java:1:package com.jpsoftware.farmapp.shared.email.model;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/model/EmailMessage.java:3:public record EmailMessage(
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/model/EmailMessage.java:4:        String recipientEmail,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:24:    private final String adminEmail;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:31:            @Value("${ADMIN_EMAIL:admin@farmapp.com}") String adminEmail,
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:36:        this.adminEmail = adminEmail;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:49:        admin.setEmail(adminEmail);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/config/DefaultAdminInitializer.java:53:        admin.setEmailConfirmed(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:34:            Map.entry("Invalid or expired email confirmation token", "O token de confirmação é inválido ou expirou."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:42:            Map.entry("Email confirmation is required before login", "Confirme seu e-mail antes de entrar."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:43:            Map.entry("Email already confirmed", "O e-mail já foi confirmado."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:44:            Map.entry("Email confirmed successfully", "E-mail confirmado com sucesso."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:45:            Map.entry("Confirmation email sent successfully", "E-mail de confirmação enviado com sucesso."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:46:            Map.entry("Unable to send email", "Não foi possível enviar o e-mail."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:52:            Map.entry("User with this email already exists", "Já existe um usuário com este e-mail."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:87:            Map.entry("Invalid email or password", "E-mail ou senha inválidos."),
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/ErrorMessageTranslator.java:149:            case "email" -> "O e-mail é obrigatório.";
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java:1:package com.jpsoftware.farmapp.shared.email.service;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java:3:import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java:5:public interface EmailSender {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java:7:    void send(EmailMessage emailMessage);
backend/farmapp/src/test/java/com/jpsoftware/farmapp/base/BaseIntegrationTest.java:4:import com.jpsoftware.farmapp.auth.service.EmailConfirmationTokenService;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/base/BaseIntegrationTest.java:68:    protected EmailConfirmationTokenService emailConfirmationTokenService;
backend/farmapp/src/test/java/com/jpsoftware/farmapp/base/BaseIntegrationTest.java:97:        user.setEmail(role.toLowerCase() + "-" + UUID.randomUUID() + "@farm.com");
backend/farmapp/src/test/java/com/jpsoftware/farmapp/base/BaseIntegrationTest.java:101:        user.setEmailConfirmed(true);
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailConfirmationRequiredException.java:3:public class EmailConfirmationRequiredException extends RuntimeException {
backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/EmailConfirmationRequiredException.java:5:    public EmailConfirmationRequiredException(String message) {

codex
The repo already has email-confirmation infrastructure, including `EmailSender`, SMTP config, and confirmation services. I’m reading those pieces now to see whether this is mostly a Brevo-specific config alignment or if the registration trigger path still needs wiring.
exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailProperties.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.infrastructure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "app.email")
public class EmailProperties {

    private boolean enabled;
    private String from = "no-reply@farmapp.local";
    private final Confirmation confirmation = new Confirmation();
    private final Smtp smtp = new Smtp();

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFrom() {
        return from;
    }

    public void setFrom(String from) {
        this.from = from;
    }

    public Confirmation getConfirmation() {
        return confirmation;
    }

    public Smtp getSmtp() {
        return smtp;
    }

    public static class Confirmation {

        private String subject = "Confirme sua conta no Farm App";

        public String getSubject() {
            return subject;
        }

        public void setSubject(String subject) {
            this.subject = subject;
        }
    }

    public static class Smtp {

        private String host;
        private int port = 587;
        private String username;
        private String password;
        private boolean auth = true;
        private boolean starttlsEnabled = true;
        private String protocol = "smtp";
        private int connectionTimeoutMs = 5000;
        private int timeoutMs = 5000;
        private int writeTimeoutMs = 5000;

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String password) {
            this.password = password;
        }

        public boolean isAuth() {
            return auth;
        }

        public void setAuth(boolean auth) {
            this.auth = auth;
        }

        public boolean isStarttlsEnabled() {
            return starttlsEnabled;
        }

        public void setStarttlsEnabled(boolean starttlsEnabled) {
            this.starttlsEnabled = starttlsEnabled;
        }

        public String getProtocol() {
            return protocol;
        }

        public void setProtocol(String protocol) {
            this.protocol = protocol;
        }

        public int getConnectionTimeoutMs() {
            return connectionTimeoutMs;
        }

        public void setConnectionTimeoutMs(int connectionTimeoutMs) {
            this.connectionTimeoutMs = connectionTimeoutMs;
        }

        public int getTimeoutMs() {
            return timeoutMs;
        }

        public void setTimeoutMs(int timeoutMs) {
            this.timeoutMs = timeoutMs;
        }

        public int getWriteTimeoutMs() {
            return writeTimeoutMs;
        }

        public void setWriteTimeoutMs(int writeTimeoutMs) {
            this.writeTimeoutMs = writeTimeoutMs;
        }
    }
}

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/EmailConfiguration.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.infrastructure;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties(EmailProperties.class)
public class EmailConfiguration {
}

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "true")
public class SmtpEmailSender implements EmailSender {

    private final EmailProperties emailProperties;
    private final SmtpTransport smtpTransport;

    @Autowired
    public SmtpEmailSender(EmailProperties emailProperties) {
        this(emailProperties, new SocketSmtpTransport());
    }

    public SmtpEmailSender(EmailProperties emailProperties, SmtpTransport smtpTransport) {
        this.emailProperties = emailProperties;
        this.smtpTransport = smtpTransport;
    }

    @Override
    public void send(EmailMessage emailMessage) {
        validateEmailMessage(emailMessage);
        try {
            smtpTransport.send(
                    emailProperties,
                    resolveFromAddress(),
                    emailMessage.recipientEmail().trim(),
                    buildMessage(emailMessage));
        } catch (IOException exception) {
            throw new EmailDispatchException("Unable to send email", exception);
        }
    }

    private void validateEmailMessage(EmailMessage emailMessage) {
        if (emailMessage == null) {
            throw new IllegalArgumentException("emailMessage must not be null");
        }
        if (!StringUtils.hasText(emailMessage.recipientEmail())) {
            throw new IllegalArgumentException("recipientEmail must not be blank");
        }
        if (!StringUtils.hasText(emailMessage.subject())) {
            throw new IllegalArgumentException("subject must not be blank");
        }
        if (!StringUtils.hasText(emailMessage.body())) {
            throw new IllegalArgumentException("body must not be blank");
        }
    }

    private String resolveFromAddress() {
        if (!StringUtils.hasText(emailProperties.getFrom())) {
            throw new IllegalStateException("app.email.from must be configured when app.email.enabled is true");
        }
        return emailProperties.getFrom().trim();
    }

    private String buildMessage(EmailMessage emailMessage) {
        return "From: <" + resolveFromAddress() + ">\r\n"
                + "To: <" + emailMessage.recipientEmail().trim() + ">\r\n"
                + "Subject: " + encodeHeader(emailMessage.subject().trim()) + "\r\n"
                + "MIME-Version: 1.0\r\n"
                + "Content-Type: text/plain; charset=UTF-8\r\n"
                + "Content-Transfer-Encoding: base64\r\n"
                + "\r\n"
                + escapeMessageBody(encodeBody(emailMessage.body()));
    }

    private String encodeHeader(String headerValue) {
        String encoded = Base64.getEncoder().encodeToString(headerValue.getBytes(StandardCharsets.UTF_8));
        return "=?UTF-8?B?" + encoded + "?=";
    }

    private String encodeBody(String body) {
        byte[] bodyBytes = body.replace("\r\n", "\n").replace('\r', '\n').getBytes(StandardCharsets.UTF_8);
        return Base64.getMimeEncoder(76, "\r\n".getBytes(StandardCharsets.US_ASCII)).encodeToString(bodyBytes);
    }

    private String escapeMessageBody(String body) {
        String[] lines = body.split("\r\n", -1);
        StringBuilder escapedBody = new StringBuilder(body.length() + 16);
        for (int index = 0; index < lines.length; index++) {
            String line = lines[index];
            if (line.startsWith(".")) {
                escapedBody.append('.');
            }
            escapedBody.append(line);
            if (index < lines.length - 1) {
                escapedBody.append("\r\n");
            }
        }
        return escapedBody.toString();
    }

    public interface SmtpTransport {

        void send(EmailProperties emailProperties, String fromAddress, String recipientEmail, String message)
                throws IOException;
    }

    private static final class SocketSmtpTransport implements SmtpTransport {

        @Override
        public void send(EmailProperties emailProperties, String fromAddress, String recipientEmail, String message)
                throws IOException {
            try (SmtpSession smtpSession = SmtpSession.open(emailProperties)) {
                smtpSession.expectCode(220, smtpSession.readResponse());
                smtpSession.sendEhlo();

                if (emailProperties.getSmtp().isStarttlsEnabled()
                        && !"smtps".equalsIgnoreCase(emailProperties.getSmtp().getProtocol())) {
                    smtpSession.startTls();
                    smtpSession.sendEhlo();
                }
                if (emailProperties.getSmtp().isAuth()) {
                    smtpSession.authenticate();
                }

                smtpSession.sendCommand("MAIL FROM:<" + fromAddress + ">", 250);
                smtpSession.sendCommand("RCPT TO:<" + recipientEmail + ">", 250);
                smtpSession.sendData(message);
                smtpSession.quit();
            }
        }
    }

    private static final class SmtpSession implements Closeable {

        private static final String CLIENT_NAME = "farmapp.local";

        private Socket socket;
        private BufferedReader reader;
        private BufferedWriter writer;
        private final EmailProperties emailProperties;

        private SmtpSession(Socket socket, EmailProperties emailProperties) throws IOException {
            this.socket = socket;
            this.emailProperties = emailProperties;
            refreshStreams(socket);
        }

        static SmtpSession open(EmailProperties emailProperties) throws IOException {
            EmailProperties.Smtp smtp = emailProperties.getSmtp();
            if (!StringUtils.hasText(smtp.getHost())) {
                throw new IllegalStateException("app.email.smtp.host must be configured when app.email.enabled is true");
            }
            if (smtp.isAuth() && (!StringUtils.hasText(smtp.getUsername()) || !StringUtils.hasText(smtp.getPassword()))) {
                throw new IllegalStateException(
                        "app.email.smtp.username and app.email.smtp.password must be configured when app.email.smtp.auth is true");
            }

            Socket socket;
            if ("smtps".equalsIgnoreCase(smtp.getProtocol())) {
                socket = SSLSocketFactory.getDefault().createSocket();
            } else {
                socket = new Socket();
            }

            socket.connect(new InetSocketAddress(smtp.getHost().trim(), smtp.getPort()), smtp.getConnectionTimeoutMs());
            socket.setSoTimeout(smtp.getTimeoutMs());

            if (socket instanceof SSLSocket sslSocket) {
                sslSocket.startHandshake();
            }

            return new SmtpSession(socket, emailProperties);
        }

        void sendEhlo() throws IOException {
            sendCommand("EHLO " + CLIENT_NAME, 250);
        }

        void startTls() throws IOException {
            sendCommand("STARTTLS", 220);
            SSLSocketFactory sslSocketFactory = (SSLSocketFactory) SSLSocketFactory.getDefault();
            SSLSocket sslSocket = (SSLSocket) sslSocketFactory.createSocket(
                    socket,
                    emailProperties.getSmtp().getHost().trim(),
                    emailProperties.getSmtp().getPort(),
                    true);
            sslSocket.startHandshake();
            socket = sslSocket;
            refreshStreams(socket);
        }

        void authenticate() throws IOException {
            sendCommand("AUTH LOGIN", 334);
            sendCommand(base64(emailProperties.getSmtp().getUsername().trim()), 334);
            sendCommand(base64(emailProperties.getSmtp().getPassword()), 235);
        }

        void sendData(String message) throws IOException {
            sendCommand("DATA", 354);
            writer.write(message);
            writer.write("\r\n.\r\n");
            writer.flush();
            expectCode(250, readResponse());
        }

        void quit() throws IOException {
            sendCommand("QUIT", 221);
        }

        void sendCommand(String command, int expectedCode) throws IOException {
            writer.write(command);
            writer.write("\r\n");
            writer.flush();
            expectCode(expectedCode, readResponse());
        }

        String readResponse() throws IOException {
            String line = reader.readLine();
            if (line == null) {
                throw new IOException("SMTP server closed the connection unexpectedly");
            }

            StringBuilder response = new StringBuilder(line);
            while (line.length() > 3 && line.charAt(3) == '-') {
                line = reader.readLine();
                if (line == null) {
                    throw new IOException("SMTP server closed the connection unexpectedly");
                }
                response.append("\n").append(line);
            }
            return response.toString();
        }

        void expectCode(int expectedCode, String response) throws IOException {
            if (response.length() < 3) {
                throw new IOException("Invalid SMTP response: " + response);
            }
            int responseCode;
            try {
                responseCode = Integer.parseInt(response.substring(0, 3));
            } catch (NumberFormatException exception) {
                throw new IOException("Invalid SMTP response: " + response, exception);
            }
            if (responseCode != expectedCode) {
                throw new IOException("Unexpected SMTP response " + responseCode + ": " + response);
            }
        }


exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.infrastructure;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(prefix = "app.email", name = "enabled", havingValue = "false", matchIfMissing = true)
public class LoggingEmailSender implements EmailSender {

    private static final Logger logger = LoggerFactory.getLogger(LoggingEmailSender.class);

    @Override
    public void send(EmailMessage emailMessage) {
        logger.warn(
                "SMTP email delivery is disabled. Transactional email to {} with subject '{}': {}",
                emailMessage.recipientEmail(),
                emailMessage.subject(),
                emailMessage.body());
    }
}

exec
/bin/bash -lc "sed -n '1,280p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.service;

import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.ResourceNotFoundException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class EmailConfirmationService {

    private final UserRepository userRepository;
    private final EmailConfirmationTokenService tokenService;
    private final EmailSender emailSender;
    private final String frontendBaseUrl;
    private final String confirmationSubject;
    private final long tokenExpirationHours;

    public EmailConfirmationService(
            UserRepository userRepository,
            EmailConfirmationTokenService tokenService,
            EmailSender emailSender,
            @Value("${app.auth.email-confirmation.frontend-base-url}") String frontendBaseUrl,
            @Value("${app.email.confirmation.subject}") String confirmationSubject,
            @Value("${app.auth.email-confirmation.token-expiration-hours}") long tokenExpirationHours) {
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.emailSender = emailSender;
        this.frontendBaseUrl = frontendBaseUrl;
        this.confirmationSubject = confirmationSubject;
        this.tokenExpirationHours = tokenExpirationHours;
    }

    @Transactional
    public void initializePendingConfirmation(UserEntity userEntity) {
        issueConfirmationToken(userEntity);
    }

    @Transactional
    public void resendConfirmation(String email) {
        UserEntity userEntity = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        if (userEntity.isEmailConfirmed()) {
            throw new ConflictException("Email already confirmed");
        }

        issueConfirmationToken(userEntity);
    }

    @Transactional
    public void confirmEmail(String token) {
        if (!StringUtils.hasText(token)) {
            throw new ValidationException("token must not be blank");
        }

        String tokenHash = tokenService.hashToken(token.trim());
        UserEntity userEntity = userRepository.findByEmailConfirmationTokenHash(tokenHash)
                .orElseThrow(() -> new ValidationException("Invalid or expired email confirmation token"));

        if (userEntity.isEmailConfirmed()) {
            throw new ConflictException("Email already confirmed");
        }
        if (userEntity.getEmailConfirmationTokenExpiresAt() == null
                || userEntity.getEmailConfirmationTokenExpiresAt().isBefore(Instant.now())) {
            throw new ValidationException("Invalid or expired email confirmation token");
        }

        userEntity.setEmailConfirmed(true);
        userEntity.setEmailConfirmationTokenHash(null);
        userEntity.setEmailConfirmationTokenExpiresAt(null);
        userRepository.save(userEntity);
    }

    private void issueConfirmationToken(UserEntity userEntity) {
        String rawToken = tokenService.generateToken();
        userEntity.setEmailConfirmed(false);
        userEntity.setEmailConfirmationTokenHash(tokenService.hashToken(rawToken));
        userEntity.setEmailConfirmationTokenExpiresAt(Instant.now().plus(tokenExpirationHours, ChronoUnit.HOURS));
        userRepository.save(userEntity);

        emailSender.send(new EmailMessage(
                userEntity.getEmail(),
                confirmationSubject,
                buildConfirmationBody(userEntity.getName(), buildConfirmationUrl(rawToken))));
    }

    private String buildConfirmationUrl(String rawToken) {
        return frontendBaseUrl.replaceAll("/+$", "") + "/login?mode=confirm&token=" + rawToken;
    }

    private String buildConfirmationBody(String recipientName, String confirmationUrl) {
        String resolvedName = StringUtils.hasText(recipientName) ? recipientName.trim() : "usuário";
        return """
                Olá %s,

                Recebemos o cadastro da sua conta no Farm App.
                Confirme seu e-mail acessando o link abaixo:
                %s

                Se você não solicitou este cadastro, ignore esta mensagem.
                """.formatted(resolvedName, confirmationUrl);
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.service;

import com.jpsoftware.farmapp.auth.dto.LoginResponse;
import com.jpsoftware.farmapp.auth.dto.RegisterRequest;
import com.jpsoftware.farmapp.shared.dto.MessageResponse;
import com.jpsoftware.farmapp.shared.exception.ConflictException;
import com.jpsoftware.farmapp.shared.exception.EmailConfirmationRequiredException;
import com.jpsoftware.farmapp.shared.exception.InvalidCredentialsException;
import com.jpsoftware.farmapp.shared.exception.ValidationException;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import com.jpsoftware.farmapp.user.mapper.UserMapper;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.util.List;
import java.util.Locale;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final TokenService tokenService;
    private final EmailConfirmationService emailConfirmationService;

    public AuthService(
            UserRepository userRepository,
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            TokenService tokenService,
            EmailConfirmationService emailConfirmationService) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.tokenService = tokenService;
        this.emailConfirmationService = emailConfirmationService;
    }

    @Transactional(readOnly = true)
    public LoginResponse login(String email, String password) {
        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new InvalidCredentialsException("Invalid email or password");
        }

        UserEntity user = userRepository.findByEmail(normalizeEmail(email))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid email or password"));

        if (!user.isActive() || !passwordEncoder.matches(password, user.getPassword())) {
            throw new InvalidCredentialsException("Invalid email or password");
        }
        if (!user.isEmailConfirmed()) {
            throw new EmailConfirmationRequiredException("Email confirmation is required before login");
        }

        String accessToken = tokenService.generateToken(user);
        UserResponse userResponse = userMapper.toResponse(user);
        return new LoginResponse(accessToken, userResponse);
    }

    @Transactional
    public UserResponse register(RegisterRequest request) {
        if (request == null) {
            throw new ValidationException("request must not be null");
        }

        String normalizedEmail = normalizeEmail(request.getEmail());
        userRepository.findByEmail(normalizedEmail)
                .ifPresent(existingUser -> {
                    throw new ConflictException("User with this email already exists");
                });

        UserEntity user = new UserEntity();
        user.setName(request.getName().trim());
        user.setEmail(normalizedEmail);
        user.setRole("MANAGER");
        user.setPassword(passwordEncoder.encode(request.getPassword().trim()));
        user.setActive(true);
        user.setEmailConfirmed(false);
        user.setPlan(UserPlan.defaultPlan());

        UserEntity savedUser = userRepository.save(user);
        emailConfirmationService.initializePendingConfirmation(savedUser);
        return userMapper.toResponse(savedUser, List.of());
    }

    @Transactional
    public MessageResponse confirmEmail(String token) {
        emailConfirmationService.confirmEmail(token);
        return new MessageResponse("E-mail confirmado com sucesso.");
    }

    @Transactional
    public MessageResponse resendConfirmation(String email) {
        emailConfirmationService.resendConfirmation(email);
        return new MessageResponse("E-mail de confirmação enviado com sucesso.");
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
}

codex
There’s already a decoupled confirmation flow in `AuthService`/`EmailConfirmationService`; the main gaps are likely provider config, failure handling, and removing unsafe logging. I’m checking the current tests and config so the plan can call out the exact touchpoints.
exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/pom.xml" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 https://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>3.4.4</version>
        <relativePath/>
    </parent>

    <groupId>com.jpsoftware</groupId>
    <artifactId>farmapp</artifactId>
    <version>0.0.1-SNAPSHOT</version>
    <name>farmapp</name>
    <description>Farm management API</description>

    <properties>
        <java.version>21</java.version>
        <maven.compiler.release>${java.version}</maven.compiler.release>
        <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
        <springdoc-openapi.version>2.8.5</springdoc-openapi.version>
        <jjwt.version>0.12.6</jjwt.version>
        <lombok.version>1.18.46</lombok.version>
        <flyway.version>11.14.1</flyway.version>
        <spring-boot-maven-plugin.version>3.4.4</spring-boot-maven-plugin.version>
        <maven-compiler-plugin.version>3.13.0</maven-compiler-plugin.version>
        <maven-surefire-plugin.version>3.5.2</maven-surefire-plugin.version>
        <jacoco-maven-plugin.version>0.8.11</jacoco-maven-plugin.version>
    </properties>

    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>org.springdoc</groupId>
                <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
                <version>${springdoc-openapi.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-api</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-impl</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>io.jsonwebtoken</groupId>
                <artifactId>jjwt-jackson</artifactId>
                <version>${jjwt.version}</version>
            </dependency>
            <dependency>
                <groupId>org.projectlombok</groupId>
                <artifactId>lombok</artifactId>
                <version>${lombok.version}</version>
            </dependency>
        </dependencies>
    </dependencyManagement>

    <dependencies>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-data-jpa</artifactId>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-core</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        <dependency>
            <groupId>org.flywaydb</groupId>
            <artifactId>flyway-database-postgresql</artifactId>
            <version>${flyway.version}</version>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        <dependency>
            <groupId>org.springdoc</groupId>
            <artifactId>springdoc-openapi-starter-webmvc-ui</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-api</artifactId>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-impl</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>io.jsonwebtoken</groupId>
            <artifactId>jjwt-jackson</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.postgresql</groupId>
            <artifactId>postgresql</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <version>${lombok.version}</version>
            <optional>true</optional>
        </dependency>

        <dependency>
            <groupId>com.h2database</groupId>
            <artifactId>h2</artifactId>
            <scope>runtime</scope>
        </dependency>
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>

    <build>
        <directory>${project.basedir}/.build/${user.name}</directory>
        <plugins>
            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-surefire-plugin</artifactId>
                <version>${maven-surefire-plugin.version}</version>
            </plugin>

            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
                <version>${spring-boot-maven-plugin.version}</version>
                <configuration>
                    <excludes>
                        <exclude>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                        </exclude>
                    </excludes>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.apache.maven.plugins</groupId>
                <artifactId>maven-compiler-plugin</artifactId>
                <version>${maven-compiler-plugin.version}</version>
                <configuration>
                    <release>${maven.compiler.release}</release>
                    <parameters>true</parameters>
                    <annotationProcessorPaths>
                        <path>
                            <groupId>org.projectlombok</groupId>
                            <artifactId>lombok</artifactId>
                            <version>${lombok.version}</version>
                        </path>
                    </annotationProcessorPaths>
                </configuration>
            </plugin>

            <plugin>
                <groupId>org.jacoco</groupId>
                <artifactId>jacoco-maven-plugin</artifactId>
                <version>${jacoco-maven-plugin.version}</version>
                <executions>
                    <execution>
                        <goals>
                            <goal>prepare-agent</goal>
                        </goals>
                    </execution>
                    <execution>
                        <id>report</id>
                        <phase>test</phase>
                        <goals>
                            <goal>report</goal>
                        </goals>
                    </execution>
                </executions>
            </plugin>
        </plugins>
    </build>
</project>

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.jpsoftware.farmapp.auth.service.EmailConfirmationService;
import com.jpsoftware.farmapp.auth.service.EmailConfirmationTokenService;
import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.repository.UserRepository;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;

class EmailConfirmationServiceTest {

    private final UserRepository userRepository = org.mockito.Mockito.mock(UserRepository.class);
    private final EmailConfirmationTokenService tokenService =
            org.mockito.Mockito.mock(EmailConfirmationTokenService.class);
    private final EmailSender emailSender = org.mockito.Mockito.mock(EmailSender.class);

    @Test
    void shouldComposeConfirmationEmailWithConfiguredSubjectAndTrimmedFrontendBaseUrl() {
        EmailConfirmationService service = new EmailConfirmationService(
                userRepository,
                tokenService,
                emailSender,
                "http://localhost:5173///",
                "Confirme sua conta no Farm App",
                24);
        UserEntity user = new UserEntity();
        user.setName("   ");
        user.setEmail("maria@farm.com");
        Instant beforeIssuance = Instant.now();

        when(tokenService.generateToken()).thenReturn("raw-token");
        when(tokenService.hashToken("raw-token")).thenReturn("hashed-token");
        when(userRepository.save(user)).thenReturn(user);

        service.initializePendingConfirmation(user);

        assertFalse(user.isEmailConfirmed());
        assertEquals("hashed-token", user.getEmailConfirmationTokenHash());
        assertNotNull(user.getEmailConfirmationTokenExpiresAt());
        assertTrue(user.getEmailConfirmationTokenExpiresAt().isAfter(beforeIssuance));

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(emailCaptor.capture());
        EmailMessage sentEmail = emailCaptor.getValue();
        assertEquals("maria@farm.com", sentEmail.recipientEmail());
        assertEquals("Confirme sua conta no Farm App", sentEmail.subject());
        assertTrue(sentEmail.body().contains("Olá usuário,"));
        assertTrue(sentEmail.body().contains("http://localhost:5173/login?mode=confirm&token=raw-token"));
    }
}

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/SmtpEmailSenderTest.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.unit.auth;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.jpsoftware.farmapp.auth.infrastructure.EmailProperties;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.exception.EmailDispatchException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import org.junit.jupiter.api.Test;

class SmtpEmailSenderTest {

    @Test
    void shouldSendTransactionalEmailWithConfiguredMetadata() {
        EmailProperties emailProperties = buildEmailProperties();
        SentMessage sentMessage = new SentMessage();
        SmtpEmailSender sender = new SmtpEmailSender(emailProperties, (properties, fromAddress, recipientEmail, message) -> {
            sentMessage.fromAddress = fromAddress;
            sentMessage.recipientEmail = recipientEmail;
            sentMessage.message = message;
        });

        sender.send(new EmailMessage(
                "  maria@farm.com  ",
                "Confirme sua conta no Farm App",
                "Olá Maria Silva,\n\nUse o link para confirmar."));

        assertEquals("no-reply@farmapp.local", sentMessage.fromAddress);
        assertEquals("maria@farm.com", sentMessage.recipientEmail);
        assertTrue(sentMessage.message.contains("Content-Type: text/plain; charset=UTF-8"));
        assertTrue(sentMessage.message.contains("Content-Transfer-Encoding: base64"));
        assertTrue(sentMessage.message.contains("=?UTF-8?B?"));
        assertTrue(decodeBody(sentMessage.message).contains("Olá Maria Silva,"));
    }

    @Test
    void shouldWrapMailFailuresAsEmailDispatchException() {
        EmailProperties emailProperties = buildEmailProperties();
        SmtpEmailSender sender = new SmtpEmailSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new IOException("smtp error");
                });

        EmailDispatchException exception = assertThrows(
                EmailDispatchException.class,
                () -> sender.send(new EmailMessage(
                        "maria@farm.com",
                        "Confirme sua conta no Farm App",
                        "Olá Maria Silva")));

        assertEquals("Unable to send email", exception.getMessage());
    }

    @Test
    void shouldRejectInvalidEmailMessageBeforeSending() {
        EmailProperties emailProperties = buildEmailProperties();
        SmtpEmailSender sender = new SmtpEmailSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new AssertionError("transport should not be called");
                });

        IllegalArgumentException nullMessageException = assertThrows(IllegalArgumentException.class, () -> sender.send(null));
        assertEquals("emailMessage must not be null", nullMessageException.getMessage());

        IllegalArgumentException blankRecipientException = assertThrows(
                IllegalArgumentException.class,
                () -> sender.send(new EmailMessage(" ", "Confirme sua conta", "Olá Maria")));
        assertEquals("recipientEmail must not be blank", blankRecipientException.getMessage());

        IllegalArgumentException blankSubjectException = assertThrows(
                IllegalArgumentException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", " ", "Olá Maria")));
        assertEquals("subject must not be blank", blankSubjectException.getMessage());

        IllegalArgumentException blankBodyException = assertThrows(
                IllegalArgumentException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", " ")));
        assertEquals("body must not be blank", blankBodyException.getMessage());
    }

    @Test
    void shouldRequireConfiguredFromAddressWhenSending() {
        EmailProperties emailProperties = buildEmailProperties();
        emailProperties.setFrom("   ");
        SmtpEmailSender sender = new SmtpEmailSender(
                emailProperties,
                (properties, fromAddress, recipientEmail, message) -> {
                    throw new AssertionError("transport should not be called");
                });

        IllegalStateException exception = assertThrows(
                IllegalStateException.class,
                () -> sender.send(new EmailMessage("maria@farm.com", "Confirme sua conta", "Olá Maria")));

        assertEquals("app.email.from must be configured when app.email.enabled is true", exception.getMessage());
    }

    private String decodeBody(String rawMessage) {
        String[] parts = rawMessage.split("\r\n\r\n", 2);
        return new String(Base64.getMimeDecoder().decode(parts[1]), StandardCharsets.UTF_8);
    }

    private EmailProperties buildEmailProperties() {
        EmailProperties emailProperties = new EmailProperties();
        emailProperties.setFrom("no-reply@farmapp.local");
        emailProperties.getConfirmation().setSubject("Confirme sua conta no Farm App");
        emailProperties.setEnabled(true);
        emailProperties.getSmtp().setHost("smtp.example.com");
        emailProperties.getSmtp().setPort(587);
        emailProperties.getSmtp().setAuth(false);
        emailProperties.getSmtp().setStarttlsEnabled(false);
        return emailProperties;
    }

    private static final class SentMessage {
        private String fromAddress;
        private String recipientEmail;
        private String message;
    }
}

exec
/bin/bash -lc "sed -n '1,240p' backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.unit.auth;

import static org.assertj.core.api.Assertions.assertThat;

import com.jpsoftware.farmapp.auth.infrastructure.EmailConfiguration;
import com.jpsoftware.farmapp.auth.infrastructure.LoggingEmailSender;
import com.jpsoftware.farmapp.auth.infrastructure.SmtpEmailSender;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Import;

class EmailSenderConfigurationTest {

    @Test
    void shouldUseLoggingEmailSenderWhenEmailIsDisabled() {
        try (ConfigurableApplicationContext context = runContext("--app.email.enabled=false")) {
            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
            assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
            assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
        }
    }

    @Test
    void shouldUseLoggingEmailSenderWhenEmailPropertyIsMissing() {
        try (ConfigurableApplicationContext context = runContext()) {
            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
            assertThat(context.getBean(EmailSender.class)).isInstanceOf(LoggingEmailSender.class);
            assertThat(context.getBeansOfType(SmtpEmailSender.class)).isEmpty();
        }
    }

    @Test
    void shouldUseSmtpEmailSenderWhenEmailIsEnabled() {
        try (ConfigurableApplicationContext context = runContext("--app.email.enabled=true")) {
            assertThat(context.getBeansOfType(EmailSender.class)).hasSize(1);
            assertThat(context.getBean(EmailSender.class)).isInstanceOf(SmtpEmailSender.class);
            assertThat(context.getBeansOfType(LoggingEmailSender.class)).isEmpty();
        }
    }

    private ConfigurableApplicationContext runContext(String... args) {
        return new SpringApplicationBuilder(EmailSenderTestApplication.class)
                .web(WebApplicationType.NONE)
                .run(args);
    }

    @SpringBootConfiguration
    @Import({EmailConfiguration.class, LoggingEmailSender.class, SmtpEmailSender.class})
    static class EmailSenderTestApplication {
    }
}

exec
/bin/bash -lc "sed -n '1,260p' backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.integration.auth;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.mockito.Mockito.verify;

import com.jpsoftware.farmapp.base.BaseIntegrationTest;
import com.jpsoftware.farmapp.shared.email.model.EmailMessage;
import com.jpsoftware.farmapp.shared.email.service.EmailSender;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Date;
import com.jpsoftware.farmapp.user.entity.UserEntity;
import com.jpsoftware.farmapp.user.entity.UserPlan;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.mockito.ArgumentCaptor;

class AuthIntegrationTest extends BaseIntegrationTest {

    @MockBean
    private EmailSender emailSender;

    @Test
    void shouldRegisterAccountAsPendingConfirmation() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@farm.com"))
                .andExpect(jsonPath("$.role").value("MANAGER"))
                .andExpect(jsonPath("$.active").value(true))
                .andExpect(jsonPath("$.plan").value("FREE"))
                .andExpect(jsonPath("$.farmIds").isArray())
                .andExpect(jsonPath("$.farmIds").isEmpty());

        UserEntity registeredUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
        Assertions.assertTrue(registeredUser.isActive());
        Assertions.assertFalse(registeredUser.isEmailConfirmed());
        Assertions.assertEquals("MANAGER", registeredUser.getRole());
        Assertions.assertEquals(UserPlan.FREE, registeredUser.getPlan());
        Assertions.assertTrue(passwordEncoder.matches("farmapp@123", registeredUser.getPassword()));
        Assertions.assertNotNull(registeredUser.getEmailConfirmationTokenHash());
        Assertions.assertNotNull(registeredUser.getEmailConfirmationTokenExpiresAt());

        ArgumentCaptor<EmailMessage> emailCaptor = ArgumentCaptor.forClass(EmailMessage.class);
        verify(emailSender).send(emailCaptor.capture());
        EmailMessage sentEmail = emailCaptor.getValue();
        Assertions.assertEquals("maria@farm.com", sentEmail.recipientEmail());
        Assertions.assertEquals("Confirme sua conta no Farm App", sentEmail.subject());
        Assertions.assertTrue(sentEmail.body().contains("Olá Maria Silva,"));
        Assertions.assertTrue(sentEmail.body().contains("http://localhost:5173/login?mode=confirm&token="));
        String rawToken = sentEmail.body()
                .substring(sentEmail.body().indexOf("token=") + "token=".length())
                .split("\\s", 2)[0];
        Assertions.assertEquals(
                emailConfirmationTokenService.hashToken(rawToken),
                registeredUser.getEmailConfirmationTokenHash());
    }

    @Test
    void shouldRejectDuplicateEmailDuringRegistration() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Existing User",
                "maria@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
    }

    @Test
    void shouldRejectDuplicateEmailDuringRegistrationIgnoringCaseAndWhitespace() throws Exception {
        userRepository.save(new UserEntity(
                null,
                "Existing User",
                "maria@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));

        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "  MARIA@FARM.COM  ",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.error").value("Já existe um usuário com este e-mail."));
    }

    @Test
    void shouldNormalizeRegistrationFieldsBeforePersistingUser() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "  Maria Silva  ",
                                  "email": "  MARIA@FARM.COM  ",
                                  "password": "  farmapp@123  "
                                }
                                """))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Maria Silva"))
                .andExpect(jsonPath("$.email").value("maria@farm.com"));

        UserEntity registeredUser = userRepository.findByEmail("maria@farm.com").orElseThrow();
        Assertions.assertEquals("Maria Silva", registeredUser.getName());
        Assertions.assertTrue(passwordEncoder.matches("farmapp@123", registeredUser.getPassword()));
        Assertions.assertFalse(registeredUser.isEmailConfirmed());
    }

    @Test
    void shouldValidateRequiredFieldsDuringRegistration() throws Exception {
        mockMvc.perform(post("/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "name": "Maria Silva",
                                  "email": "maria@farm.com",
                                  "password": "   "
                                }
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("A senha é obrigatória."))
                .andExpect(jsonPath("$.path").value("/auth/register"));
    }

    @Test
    void shouldLoginAndReturnJwtToken() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));
        user.setEmailConfirmed(true);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.accessToken").isString())
                .andExpect(jsonPath("$.user.email").value("jane@farm.com"))
                .andExpect(jsonPath("$.user.role").value("MANAGER"))
                .andExpect(jsonPath("$.user.plan").value("FREE"));
    }

    @Test
    void shouldReturn403ForUnconfirmedUser() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));
        user.setEmailConfirmed(false);
        user.setEmailConfirmationTokenHash(emailConfirmationTokenService.hashToken("pending-token"));
        user.setEmailConfirmationTokenExpiresAt(Instant.now().plusSeconds(3600));
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.error").value("Confirme seu e-mail antes de entrar."));
    }

    @Test
    void shouldReturn401ForInvalidCredentials() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "MANAGER",
                passwordEncoder.encode("farmapp@123"),
                true));
        user.setEmailConfirmed(true);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "wrong-password"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("E-mail ou senha inválidos."));
    }

    @Test
    void shouldReturn401ForInactiveUser() throws Exception {
        UserEntity user = userRepository.save(new UserEntity(
                null,
                "Jane Doe",
                "jane@farm.com",
                "WORKER",
                passwordEncoder.encode("farmapp@123"),
                false));
        user.setEmailConfirmed(true);
        userRepository.save(user);

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "email": "jane@farm.com",
                                  "password": "farmapp@123"
                                }
                                """))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.error").value("E-mail ou senha inválidos."));
    }

    @Test

codex
The current implementation already sends a confirmation email on `/auth/register`, but it uses custom `app.email.*` properties, a hand-rolled SMTP client, and a fallback logger that prints the full email body. Those are the main mismatch and security points for this feature.
exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/resources/application.properties" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
spring.datasource.url=${SPRING_DATASOURCE_URL:jdbc:postgresql://localhost:5432/farmdb}
spring.datasource.driver-class-name=org.postgresql.Driver
spring.datasource.username=${SPRING_DATASOURCE_USERNAME:postgres}
spring.datasource.password=${SPRING_DATASOURCE_PASSWORD:postgres}

spring.jpa.database-platform=org.hibernate.dialect.PostgreSQLDialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.flyway.enabled=true
spring.flyway.locations=classpath:db/migration
spring.flyway.baseline-on-migrate=true
spring.flyway.baseline-version=1

app.security.jwt.secret=${APP_SECURITY_JWT_SECRET:0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef}
app.security.jwt.allow-default-secret=${APP_SECURITY_JWT_ALLOW_DEFAULT_SECRET:false}
app.security.jwt.expiration=86400000
app.auth.email-confirmation.frontend-base-url=${APP_AUTH_EMAIL_CONFIRMATION_FRONTEND_BASE_URL:http://localhost:5173}
app.auth.email-confirmation.token-expiration-hours=${APP_AUTH_EMAIL_CONFIRMATION_TOKEN_EXPIRATION_HOURS:24}
app.email.enabled=${APP_EMAIL_ENABLED:false}
app.email.from=${APP_EMAIL_FROM:no-reply@farmapp.local}
app.email.confirmation.subject=${APP_EMAIL_CONFIRMATION_SUBJECT:Confirme sua conta no Farm App}
app.email.smtp.host=${APP_EMAIL_SMTP_HOST:}
app.email.smtp.port=${APP_EMAIL_SMTP_PORT:587}
app.email.smtp.username=${APP_EMAIL_SMTP_USERNAME:}
app.email.smtp.password=${APP_EMAIL_SMTP_PASSWORD:}
app.email.smtp.auth=${APP_EMAIL_SMTP_AUTH:true}
app.email.smtp.starttls-enabled=${APP_EMAIL_SMTP_STARTTLS_ENABLED:true}
app.email.smtp.protocol=${APP_EMAIL_SMTP_PROTOCOL:smtp}
app.email.smtp.connection-timeout-ms=${APP_EMAIL_SMTP_CONNECTION_TIMEOUT_MS:5000}
app.email.smtp.timeout-ms=${APP_EMAIL_SMTP_TIMEOUT_MS:5000}
app.email.smtp.write-timeout-ms=${APP_EMAIL_SMTP_WRITE_TIMEOUT_MS:5000}

exec
/bin/bash -lc "sed -n '1,200p' backend/farmapp/src/main/resources/application-local.properties" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
spring.datasource.url=jdbc:h2:mem:farmdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driver-class-name=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

app.security.jwt.allow-default-secret=true
app.email.smtp.host=localhost
app.email.smtp.port=1025
app.email.smtp.auth=false
app.email.smtp.starttls-enabled=false

exec
/bin/bash -lc "sed -n '1,200p' backend/farmapp/src/test/resources/application.properties" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
spring.datasource.url=jdbc:h2:mem:farmdb;MODE=PostgreSQL;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true

app.security.jwt.secret=0123456789abcdef0123456789abcdef0123456789abcdef0123456789abcdef
app.security.jwt.allow-default-secret=true
app.security.jwt.expiration=86400000
app.auth.email-confirmation.frontend-base-url=http://localhost:5173
app.auth.email-confirmation.token-expiration-hours=24
app.email.enabled=false
app.email.confirmation.subject=Confirme sua conta no Farm App

exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/controller/AuthController.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.auth.controller;

import com.jpsoftware.farmapp.auth.dto.LoginRequest;
import com.jpsoftware.farmapp.auth.dto.LoginResponse;
import com.jpsoftware.farmapp.auth.dto.RegisterRequest;
import com.jpsoftware.farmapp.auth.dto.ResendConfirmationRequest;
import com.jpsoftware.farmapp.auth.service.AuthService;
import com.jpsoftware.farmapp.shared.dto.MessageResponse;
import com.jpsoftware.farmapp.shared.exception.ErrorResponse;
import com.jpsoftware.farmapp.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Validated
@RequestMapping("/auth")
@Tag(name = "Authentication", description = "Authentication operations.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT access token.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "403", description = "Email not confirmed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<LoginResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request.getEmail(), request.getPassword()));
    }

    @PostMapping("/register")
    @Operation(summary = "Register account", description = "Creates a new manager account pending email confirmation.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Account created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already in use",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @GetMapping("/confirm-email")
    @Operation(summary = "Confirm email", description = "Validates the email confirmation token and activates email access.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Email confirmed successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid or expired token",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already confirmed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> confirmEmail(
            @RequestParam @NotBlank(message = "token must not be blank") String token) {
        return ResponseEntity.ok(authService.confirmEmail(token));
    }

    @PostMapping("/confirm-email/resend")
    @Operation(summary = "Resend confirmation email", description = "Generates a new confirmation token and resends the confirmation email.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Confirmation email resent successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "404", description = "User not found",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))),
            @ApiResponse(responseCode = "409", description = "Email already confirmed",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class)))
    })
    public ResponseEntity<MessageResponse> resendConfirmation(@Valid @RequestBody ResendConfirmationRequest request) {
        return ResponseEntity.ok(authService.resendConfirmation(request.getEmail()));
    }
}

codex
One functional mismatch is explicit: the confirmation email currently builds `/login?mode=confirm&token=...`, while the new requirement says `/confirm-email?token=...`. I’m checking whether the frontend already supports that route or whether the plan should call it out as a dependency/risk instead of assuming it exists.
exec
/bin/bash -lc 'rg -n "confirm-email|mode=confirm|resend confirmation|email confirmation" frontend/web/farm_web/src || true' in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
frontend/web/farm_web/src/services/authService.ts:39:  const response = await api.get<MessageResponse>('/auth/confirm-email', {
frontend/web/farm_web/src/services/authService.ts:49:  const response = await api.post<MessageResponse>('/auth/confirm-email/resend', {

exec
/bin/bash -lc 'rg -n "JavaMailSender|spring.mail|MailSender|spring-boot-starter-mail" backend/farmapp/src backend/farmapp/pom.xml || true' in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
exec
/bin/bash -lc "sed -n '1,220p' backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/exception/GlobalExceptionHandler.java" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
package com.jpsoftware.farmapp.shared.exception;

import jakarta.validation.ConstraintViolationException;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Instant;
import com.jpsoftware.farmapp.shared.onboarding.FarmOnboardingRequiredException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.servlet.resource.NoResourceFoundException;
import com.jpsoftware.farmapp.shared.plan.PlanAccessDeniedException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(
            ResourceNotFoundException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            ValidationException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {
        String errorMessage = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .findFirst()
                .map(FieldError::getDefaultMessage)
                .orElse("Validation failed");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {
        String errorMessage = exception.getConstraintViolations()
                .stream()
                .findFirst()
                .map(constraintViolation -> constraintViolation.getMessage())
                .orElse("Validation failed");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleHandlerMethodValidationException(
            HandlerMethodValidationException exception,
            HttpServletRequest request) {
        String errorMessage = exception.getAllErrors()
                .stream()
                .findFirst()
                .map(error -> error.getDefaultMessage())
                .orElse("Validation failed");

        return buildErrorResponse(HttpStatus.BAD_REQUEST, errorMessage, request.getRequestURI());
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingServletRequestParameterException(
            MissingServletRequestParameterException exception,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "O parâmetro '" + exception.getParameterName() + "' é obrigatório.",
                request.getRequestURI());
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleHttpMessageNotReadableException(
            HttpMessageNotReadableException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, "Malformed request body", request.getRequestURI());
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusinessException(
            BusinessException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(ConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflictException(
            ConflictException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.CONFLICT, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleInvalidCredentialsException(
            InvalidCredentialsException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.UNAUTHORIZED, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(EmailConfirmationRequiredException.class)
    public ResponseEntity<ErrorResponse> handleEmailConfirmationRequiredException(
            EmailConfirmationRequiredException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(PlanAccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handlePlanAccessDeniedException(
            PlanAccessDeniedException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(FarmOnboardingRequiredException.class)
    public ResponseEntity<ErrorResponse> handleFarmOnboardingRequiredException(
            FarmOnboardingRequiredException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.FORBIDDEN, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception,
            HttpServletRequest request) {
        return buildErrorResponse(
                HttpStatus.CONFLICT,
                resolveDataIntegrityMessage(exception),
                request.getRequestURI());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFoundException(
            NoResourceFoundException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.NOT_FOUND, "Not Found", request.getRequestURI());
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.BAD_REQUEST, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(EmailDispatchException.class)
    public ResponseEntity<ErrorResponse> handleEmailDispatchException(
            EmailDispatchException exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, exception.getMessage(), request.getRequestURI());
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(
            Exception exception,
            HttpServletRequest request) {
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Internal server error", request.getRequestURI());
    }

    private ResponseEntity<ErrorResponse> buildErrorResponse(
            HttpStatus status,
            String error,
            String path) {
        ErrorResponse response = ErrorResponse.builder()
                .timestamp(Instant.now())
                .status(status.value())
                .error(ErrorMessageTranslator.translate(error))
                .path(path)
                .build();

        return ResponseEntity.status(status).body(response);
    }

    private String resolveDataIntegrityMessage(DataIntegrityViolationException exception) {
        String message = exception.getMostSpecificCause() != null
                ? exception.getMostSpecificCause().getMessage()
                : exception.getMessage();

        if (message != null && message.toLowerCase().contains("tag")) {
            return "Animal with this tag already exists in this farm";
        }
        if (message != null && message.toLowerCase().contains("email")) {
            return "User with this email already exists";
        }

        return "Data integrity violation";
    }
}

exec
/bin/bash -lc "sed -n '1,220p' frontend/web/farm_web/src/services/authService.ts" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import api from './api'
import type { User } from '../types/user'

export interface LoginResponse {
  accessToken: string
  user: User
}

export interface LoginCredentials {
  email: string
  password: string
}

export interface RegisterAccountRequest {
  name: string
  email: string
  password: string
}

export interface MessageResponse {
  message: string
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/auth/login', {
    email,
    password,
  })

  return response.data
}

export async function registerAccount(data: RegisterAccountRequest): Promise<User> {
  const response = await api.post<User>('/auth/register', data)
  return response.data
}

export async function confirmAccountEmail(token: string): Promise<MessageResponse> {
  const response = await api.get<MessageResponse>('/auth/confirm-email', {
    params: {
      token,
    },
  })

  return response.data
}

export async function resendConfirmationEmail(email: string): Promise<MessageResponse> {
  const response = await api.post<MessageResponse>('/auth/confirm-email/resend', {
    email,
  })

  return response.data
}

exec
/bin/bash -lc 'rg -n "createBrowserRouter|Routes|Route|login" frontend/web/farm_web/src | head -n 80' in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
frontend/web/farm_web/src/components/auth/ManagerRoute.tsx:6:interface ManagerRouteProps {
frontend/web/farm_web/src/components/auth/ManagerRoute.tsx:10:function ManagerRoute({ children }: ManagerRouteProps) {
frontend/web/farm_web/src/components/auth/ManagerRoute.tsx:20:export default ManagerRoute
frontend/web/farm_web/src/App.css:297:.login-page {
frontend/web/farm_web/src/App.css:307:.login-page__panel {
frontend/web/farm_web/src/App.css:316:.login-page__header {
frontend/web/farm_web/src/App.css:322:.login-page__eyebrow {
frontend/web/farm_web/src/App.css:330:.login-page__description {
frontend/web/farm_web/src/App.css:334:.login-page__supporting-action {
frontend/web/farm_web/src/App.css:340:.login-page__supporting-link {
frontend/web/farm_web/src/App.css:346:.login-page__supporting-link:hover {
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:6:function ProtectedRoute() {
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:11:  const isOnboardingRoute = location.pathname === '/onboarding/farm'
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:14:    return <Navigate to="/login" replace state={{ from: location }} />
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:28:  if (needsOnboarding && !isOnboardingRoute) {
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:32:  if (!needsOnboarding && isOnboardingRoute) {
frontend/web/farm_web/src/components/auth/ProtectedRoute.tsx:39:export default ProtectedRoute
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:7:interface PlanRouteProps {
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:12:function PlanRoute({ children, feature }: PlanRouteProps) {
frontend/web/farm_web/src/components/auth/PlanRoute.tsx:30:export default PlanRoute
frontend/web/farm_web/src/context/authContext.ts:8:  login: (email: string, password: string) => Promise<void>
frontend/web/farm_web/src/context/AuthContext.tsx:9:  login as loginRequest,
frontend/web/farm_web/src/context/AuthContext.tsx:46:  const login = useCallback(async (email: string, password: string) => {
frontend/web/farm_web/src/context/AuthContext.tsx:47:    const response = await loginRequest(email, password)
frontend/web/farm_web/src/context/AuthContext.tsx:70:      login,
frontend/web/farm_web/src/context/AuthContext.tsx:73:    [authState, login, logout],
frontend/web/farm_web/src/services/api.ts:25:  const isLoginRequest = requestUrl.includes('/auth/login')
frontend/web/farm_web/src/main.tsx:3:import { BrowserRouter } from 'react-router-dom'
frontend/web/farm_web/src/main.tsx:22:                <BrowserRouter>
frontend/web/farm_web/src/main.tsx:26:                </BrowserRouter>
frontend/web/farm_web/src/services/authService.ts:24:export async function login(email: string, password: string): Promise<LoginResponse> {
frontend/web/farm_web/src/services/authService.ts:25:  const response = await api.post<LoginResponse>('/auth/login', {
frontend/web/farm_web/src/i18n/en.ts:164:      loginPrompt: 'Already have an account?',
frontend/web/farm_web/src/i18n/en.ts:165:      loginAction: 'Sign in',
frontend/web/farm_web/src/i18n/en.ts:186:      loginAction: 'Go to login',
frontend/web/farm_web/src/i18n/en.ts:187:      backToLogin: 'Back to login',
frontend/web/farm_web/src/i18n/en.ts:679:    description: 'Register users with controlled farm access and login status.',
frontend/web/farm_web/src/i18n/en.ts:682:    createDescription: 'Define the role, login status, password, and farms assigned to the new user.',
frontend/web/farm_web/src/i18n/en.ts:729:      active: 'Allow login',
frontend/web/farm_web/src/i18n/pt-BR.ts:164:      loginPrompt: 'Já tem uma conta?',
frontend/web/farm_web/src/i18n/pt-BR.ts:165:      loginAction: 'Entrar',
frontend/web/farm_web/src/i18n/pt-BR.ts:186:      loginAction: 'Ir para o login',
frontend/web/farm_web/src/i18n/pt-BR.ts:187:      backToLogin: 'Voltar para o login',
frontend/web/farm_web/src/i18n/pt-BR.ts:679:    description: 'Cadastre usuários com acesso às fazendas e status de login controlados.',
frontend/web/farm_web/src/i18n/pt-BR.ts:682:    createDescription: 'Defina o papel, o status de login, a senha e as fazendas vinculadas ao novo usuário.',
frontend/web/farm_web/src/i18n/pt-BR.ts:729:      active: 'Permitir login',
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:69:    <main className="login-page">
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:70:      <section className="login-page__panel">
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:71:        <div className="login-page__header">
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:72:          <p className="login-page__eyebrow">{t('farm.eyebrow')}</p>
frontend/web/farm_web/src/pages/farm/FarmOnboardingPage.tsx:74:          <p className="login-page__description">{t('farm.onboardingDescription')}</p>
frontend/web/farm_web/src/App.tsx:1:import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
frontend/web/farm_web/src/App.tsx:14:import LoginPage from './pages/login/LoginPage'
frontend/web/farm_web/src/App.tsx:15:import ProtectedRoute from './components/auth/ProtectedRoute'
frontend/web/farm_web/src/App.tsx:16:import ManagerRoute from './components/auth/ManagerRoute'
frontend/web/farm_web/src/App.tsx:17:import PlanRoute from './components/auth/PlanRoute'
frontend/web/farm_web/src/App.tsx:26:function AnimalsRoute() {
frontend/web/farm_web/src/App.tsx:32:function AnimalDetailsRoute() {
frontend/web/farm_web/src/App.tsx:39:function DefaultRoute() {
frontend/web/farm_web/src/App.tsx:47:    <Routes>
frontend/web/farm_web/src/App.tsx:48:      <Route path="/login" element={<LoginPage />} />
frontend/web/farm_web/src/App.tsx:49:      <Route element={<ProtectedRoute />}>
frontend/web/farm_web/src/App.tsx:50:        <Route path="/onboarding/farm" element={<FarmOnboardingPage />} />
frontend/web/farm_web/src/App.tsx:51:        <Route element={<AppLayout />}>
frontend/web/farm_web/src/App.tsx:52:          <Route index element={<DefaultRoute />} />
frontend/web/farm_web/src/App.tsx:53:          <Route
frontend/web/farm_web/src/App.tsx:56:              <ManagerRoute>
frontend/web/farm_web/src/App.tsx:57:                <PlanRoute feature="DASHBOARD">
frontend/web/farm_web/src/App.tsx:59:                </PlanRoute>
frontend/web/farm_web/src/App.tsx:60:              </ManagerRoute>
frontend/web/farm_web/src/App.tsx:63:          <Route path="/animals" element={<AnimalsRoute />} />
frontend/web/farm_web/src/App.tsx:64:          <Route path="/animals/:id" element={<AnimalDetailsRoute />} />
frontend/web/farm_web/src/App.tsx:65:          <Route path="/batches" element={<AnimalBatchPage />} />
frontend/web/farm_web/src/App.tsx:66:          <Route path="/production" element={<ProductionPage />} />
frontend/web/farm_web/src/App.tsx:67:          <Route path="/milk-prices" element={<MilkPricePage />} />
frontend/web/farm_web/src/App.tsx:68:          <Route path="/feeding" element={<FeedingPage />} />
frontend/web/farm_web/src/App.tsx:69:          <Route path="/feed-types" element={<FeedTypePage />} />
frontend/web/farm_web/src/App.tsx:70:          <Route path="/plans" element={<PlansPage />} />
frontend/web/farm_web/src/App.tsx:71:          <Route path="/settings" element={<SettingsPage />} />
frontend/web/farm_web/src/App.tsx:72:          <Route

exec
/bin/bash -lc "sed -n '1,320p' frontend/web/farm_web/src/pages/login/LoginPage.tsx" in /home/juaneugenio/programmig/farm_app
 succeeded in 0ms:
import { useEffect, useState, type FormEvent } from 'react'
import axios from 'axios'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import {
  confirmAccountEmail,
  registerAccount,
  resendConfirmationEmail,
} from '../../services/authService'
import { isManager } from '../../utils/authorization'
import '../../App.css'

interface ApiErrorResponse {
  error?: string
}

interface LoginLocationState {
  registrationSuccess?: boolean
  registrationEmail?: string
  confirmationSuccess?: boolean
  from?: {
    pathname?: string
  }
}

function LoginPage() {
  const { isAuthenticated, login, user } = useAuth()
  const { hasResolvedFarms, needsOnboarding } = useFarm()
  const { t, language } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const locationState = (location.state as LoginLocationState | null) ?? null
  const searchParams = new URLSearchParams(location.search)
  const mode = searchParams.get('mode')
  const isSignupMode = mode === 'signup'
  const isConfirmationMode = mode === 'confirm'
  const confirmationToken = searchParams.get('token')?.trim() ?? ''
  const [name, setName] = useState('')
  const [email, setEmail] = useState(locationState?.registrationEmail ?? '')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [infoMessage, setInfoMessage] = useState(
    locationState?.confirmationSuccess
      ? t('auth.success.confirmation')
      : locationState?.registrationSuccess
        ? t('auth.success.registration')
        : '',
  )
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isResendingConfirmation, setIsResendingConfirmation] = useState(false)
  const [showResendConfirmation, setShowResendConfirmation] = useState(
    Boolean(locationState?.registrationSuccess),
  )
  const [confirmationStatus, setConfirmationStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [confirmationMessage, setConfirmationMessage] = useState(t('auth.confirmation.loading'))

  useEffect(() => {
    if (!isConfirmationMode) {
      return
    }

    if (!confirmationToken) {
      setConfirmationStatus('error')
      setConfirmationMessage(t('auth.confirmation.errors.invalidLink'))
      return
    }

    let isMounted = true

    confirmAccountEmail(confirmationToken)
      .then(() => {
        if (!isMounted) {
          return
        }

        setConfirmationStatus('success')
        setConfirmationMessage(t('auth.confirmation.success'))
      })
      .catch((error) => {
        if (!isMounted) {
          return
        }

        if (axios.isAxiosError<ApiErrorResponse>(error)) {
          setConfirmationMessage(error.response?.data?.error ?? t('auth.confirmation.errors.generic'))
        } else {
          setConfirmationMessage(t('auth.confirmation.errors.generic'))
        }
        setConfirmationStatus('error')
      })

    return () => {
      isMounted = false
    }
  }, [confirmationToken, isConfirmationMode, language])

  if (isAuthenticated) {
    if (!hasResolvedFarms) {
      return null
    }

    if (needsOnboarding) {
      return <Navigate to="/onboarding/farm" replace />
    }

    return <Navigate to={isManager(user) ? '/dashboard' : '/animals'} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage('')
    setInfoMessage('')
    setShowResendConfirmation(false)
    setIsSubmitting(true)

    try {
      if (isSignupMode) {
        await registerAccount({ name, email, password })
        navigate('/login', {
          replace: true,
          state: {
            registrationSuccess: true,
            registrationEmail: email.trim(),
          },
        })
        return
      }

      await login(email, password)

      const requestedPath = (location.state as LoginLocationState | null)?.from?.pathname
      const nextPath = requestedPath ?? '/dashboard'

      navigate(nextPath, { replace: true })
    } catch (error) {
      if (isSignupMode && axios.isAxiosError<ApiErrorResponse>(error)) {
        setErrorMessage(error.response?.data?.error ?? t('auth.signup.errors.generic'))
      } else if (axios.isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage(t('auth.errors.invalidCredentials'))
      } else if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.status === 403) {
        setErrorMessage(error.response?.data?.error ?? t('auth.errors.confirmationRequired'))
        setShowResendConfirmation(true)
      } else if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.error) {
        setErrorMessage(error.response.data.error)
      } else {
        setErrorMessage(isSignupMode ? t('auth.signup.errors.generic') : t('auth.errors.generic'))
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleResendConfirmation() {
    setErrorMessage('')
    setInfoMessage('')
    setIsResendingConfirmation(true)

    try {
      await resendConfirmationEmail(email)
      setInfoMessage(t('auth.success.confirmationResent'))
      setShowResendConfirmation(true)
    } catch (error) {
      if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.error) {
        setErrorMessage(error.response.data.error)
      } else {
        setErrorMessage(t('auth.errors.resendConfirmation'))
      }
    } finally {
      setIsResendingConfirmation(false)
    }
  }

  if (isConfirmationMode) {
    return (
      <main className="login-page">
        <section className="login-page__panel">
          <div className="login-page__header">
            <p className="login-page__eyebrow">{t('auth.eyebrow')}</p>
            <h1>{t('auth.confirmation.title')}</h1>
            <p className="login-page__description">{t('auth.confirmation.description')}</p>
          </div>

          <p className={`animal-form__feedback ${confirmationStatus === 'success' ? 'animal-form__feedback--success' : 'animal-form__feedback--error'}`}>
            {confirmationMessage}
          </p>

          <p className="login-page__supporting-action">
            <Link
              className="login-page__supporting-link"
              to="/login"
              state={confirmationStatus === 'success' ? { confirmationSuccess: true } : undefined}
            >
              {confirmationStatus === 'success'
                ? t('auth.confirmation.loginAction')
                : t('auth.confirmation.backToLogin')}
            </Link>
          </p>
        </section>
      </main>
    )
  }

  return (
    <main className="login-page">
      <section className="login-page__panel">
        <div className="login-page__header">
          <p className="login-page__eyebrow">{t('auth.eyebrow')}</p>
          <h1>{isSignupMode ? t('auth.signup.title') : t('auth.title')}</h1>
          <p className="login-page__description">
            {isSignupMode ? t('auth.signup.description') : t('auth.description')}
          </p>
        </div>

        <form className="animal-form" onSubmit={handleSubmit}>
          {isSignupMode && (
            <label className="animal-form__field" htmlFor="name">
              {t('auth.signup.form.name')}
              <input
                id="name"
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                autoComplete="name"
                required
              />
            </label>
          )}

          <label className="animal-form__field" htmlFor="email">
            {isSignupMode ? t('auth.signup.form.email') : t('auth.form.email')}
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />
          </label>

          <label className="animal-form__field" htmlFor="password">
            {isSignupMode ? t('auth.signup.form.password') : t('auth.form.password')}
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete={isSignupMode ? 'new-password' : 'current-password'}
              required
            />
          </label>

          {errorMessage && <p className="animal-form__feedback animal-form__feedback--error">{errorMessage}</p>}
          {!isSignupMode && infoMessage && (
            <p className="animal-form__feedback animal-form__feedback--success">
              {infoMessage}
            </p>
          )}
          {!isSignupMode && showResendConfirmation && (
            <div className="animal-form__actions">
              <button
                type="button"
                disabled={isResendingConfirmation || !email.trim()}
                onClick={handleResendConfirmation}
              >
                {isResendingConfirmation
                  ? t('auth.actions.resendingConfirmation')
                  : t('auth.actions.resendConfirmation')}
              </button>
            </div>
          )}

          <div className="animal-form__actions">
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting
                ? (isSignupMode ? t('auth.signup.submitting') : t('auth.submitting'))
                : (isSignupMode ? t('auth.signup.submit') : t('auth.submit'))}
            </button>
          </div>
        </form>

        <p className="login-page__supporting-action">
          {isSignupMode ? t('auth.signup.loginPrompt') : t('auth.signup.entryLabel')}{' '}
          <Link className="login-page__supporting-link" to={isSignupMode ? '/login' : '/login?mode=signup'}>
            {isSignupMode ? t('auth.signup.loginAction') : t('auth.signup.entryAction')}
          </Link>
        </p>
      </section>
    </main>
  )
}

export default LoginPage

exec
/bin/bash -lc "sed -n '1,260p' frontend/web/farm_web/src/App.tsx" in /home/juaneugenio/programmig/farm_app
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
import PlansPage from './pages/plans/PlansPage'
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
          <Route path="/plans" element={<PlansPage />} />
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

codex
**Target Areas**
- Backend config: [pom.xml](/home/juaneugenio/programmig/farm_app/backend/farmapp/pom.xml), [application.properties](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/resources/application.properties), [application-local.properties](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/resources/application-local.properties)
- Email infrastructure: [EmailSender.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java), [SmtpEmailSender.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java), [LoggingEmailSender.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java)
- Confirmation flow: [EmailConfirmationService.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java), [AuthService.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java)
- Tests: [EmailConfirmationServiceTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java), [EmailSenderConfigurationTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java), [AuthIntegrationTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java)

**Plan**
1. Align configuration with Brevo and Spring Mail. Add `spring-boot-starter-mail`, point SMTP to `smtp-relay.brevo.com:587`, enable STARTTLS, and externalize `BREVO_SMTP_USERNAME`, `BREVO_SMTP_PASSWORD`, `APP_MAIL_FROM`, and `APP_FRONTEND_URL` in properties.
2. Reuse the existing `EmailSender` abstraction and swap the provider-specific implementation behind it. Keep Brevo details isolated in the SMTP sender; do not couple auth/user services to Brevo classes or raw SMTP details.
3. Update confirmation email composition in `EmailConfirmationService` to build `${APP_FRONTEND_URL}/confirm-email?token={token}`. Keep subject/body generation centralized and avoid logging token or credentials anywhere.
4. Make dispatch failures non-blocking. Persist the user and confirmation token first, attempt email send after that, catch mail exceptions, log a sanitized warning, and preserve the current successful user creation response.
5. Update automated coverage for config wiring, confirmation-link generation, and “send failure does not break registration” behavior. No schema or API contract changes should be required.

**Risks**
- The frontend currently appears to confirm through `/login?mode=confirm`; sending `/confirm-email?...` may require a frontend route or redirect to avoid a broken link.
- Current fallback logging prints the full email body, which would expose the token; that must be tightened.
- Moving from custom SMTP code to Spring Mail may require test rewrites if existing tests assert raw wire-format details.
- Silent delivery failure preserves registration flow but can leave users pending confirmation; logs must remain actionable without leaking secrets.

**Validation**
- `/auth/register` still returns `201`, creates an unconfirmed user, and stores a confirmation token.
- Sent email contains the correct `APP_FRONTEND_URL`-based confirmation link.
- SMTP credentials and tokens never appear in logs or source.
- Simulated SMTP failure does not fail registration and resend still works.
- Manual smoke test with Brevo env vars confirms delivery over STARTTLS on port `587`.
tokens used
55,011
**Target Areas**
- Backend config: [pom.xml](/home/juaneugenio/programmig/farm_app/backend/farmapp/pom.xml), [application.properties](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/resources/application.properties), [application-local.properties](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/resources/application-local.properties)
- Email infrastructure: [EmailSender.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/shared/email/service/EmailSender.java), [SmtpEmailSender.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/SmtpEmailSender.java), [LoggingEmailSender.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/infrastructure/LoggingEmailSender.java)
- Confirmation flow: [EmailConfirmationService.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/EmailConfirmationService.java), [AuthService.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/main/java/com/jpsoftware/farmapp/auth/service/AuthService.java)
- Tests: [EmailConfirmationServiceTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailConfirmationServiceTest.java), [EmailSenderConfigurationTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/unit/auth/EmailSenderConfigurationTest.java), [AuthIntegrationTest.java](/home/juaneugenio/programmig/farm_app/backend/farmapp/src/test/java/com/jpsoftware/farmapp/integration/auth/AuthIntegrationTest.java)

**Plan**
1. Align configuration with Brevo and Spring Mail. Add `spring-boot-starter-mail`, point SMTP to `smtp-relay.brevo.com:587`, enable STARTTLS, and externalize `BREVO_SMTP_USERNAME`, `BREVO_SMTP_PASSWORD`, `APP_MAIL_FROM`, and `APP_FRONTEND_URL` in properties.
2. Reuse the existing `EmailSender` abstraction and swap the provider-specific implementation behind it. Keep Brevo details isolated in the SMTP sender; do not couple auth/user services to Brevo classes or raw SMTP details.
3. Update confirmation email composition in `EmailConfirmationService` to build `${APP_FRONTEND_URL}/confirm-email?token={token}`. Keep subject/body generation centralized and avoid logging token or credentials anywhere.
4. Make dispatch failures non-blocking. Persist the user and confirmation token first, attempt email send after that, catch mail exceptions, log a sanitized warning, and preserve the current successful user creation response.
5. Update automated coverage for config wiring, confirmation-link generation, and “send failure does not break registration” behavior. No schema or API contract changes should be required.

**Risks**
- The frontend currently appears to confirm through `/login?mode=confirm`; sending `/confirm-email?...` may require a frontend route or redirect to avoid a broken link.
- Current fallback logging prints the full email body, which would expose the token; that must be tightened.
- Moving from custom SMTP code to Spring Mail may require test rewrites if existing tests assert raw wire-format details.
- Silent delivery failure preserves registration flow but can leave users pending confirmation; logs must remain actionable without leaking secrets.

**Validation**
- `/auth/register` still returns `201`, creates an unconfirmed user, and stores a confirmation token.
- Sent email contains the correct `APP_FRONTEND_URL`-based confirmation link.
- SMTP credentials and tokens never appear in logs or source.
- Simulated SMTP failure does not fail registration and resend still works.
- Manual smoke test with Brevo env vars confirms delivery over STARTTLS on port `587`.
