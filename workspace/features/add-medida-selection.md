# Feature: Measurement Unit Selection

## Goal
Allow users to select measurement units (e.g., liters, kilograms) and ensure consistency across inputs, listings, dashboard, and analytics.

## Scope
- Backend handling of quantity fields
- Frontend forms, listings, dashboard, and analytics where quantities are displayed or entered

## Requirements
- Support selection of measurement units where applicable
- Ensure quantities are consistent across all parts of the system
- Calculations must remain correct regardless of selected unit
- UI must reflect the selected unit clearly in labels and values

## Constraints
- Do NOT break existing calculations or behavior
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized
- Follow existing architecture and patterns from AI_CONTEXT

## Implementation Notes
- Identify all fields that represent measurable quantities
- Ensure a consistent internal representation of values
- Handle unit selection and conversion in a centralized way
- Reuse existing services/components when possible
- Avoid duplicating conversion logic across layers

## Validation
- Unit selection correctly updates displayed values
- Inputs, listings, and analytics reflect the selected unit
- Calculations remain accurate
- No regression in existing features

## Done Criteria
- Users can select measurement units where applicable
- Values are consistent across the system
- UI clearly reflects selected units
- Implementation is reusable and scalable