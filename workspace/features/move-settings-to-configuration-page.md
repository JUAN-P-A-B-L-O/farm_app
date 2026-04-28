# Feature: Move Settings to Configuration Page

## Goal
Relocate currency and unit settings to a dedicated configuration page to reduce clutter in the main navigation and improve UX.

## Scope
- Frontend: navigation/menu, configuration/settings page
- Settings affected:
  - Display currency
  - Production unit
  - Feeding unit

## Requirements
- Remove these settings from the main menu
- Add them to a centralized "Settings" or "Configuration" page
- Ensure settings remain accessible and editable
- Preserve current behavior and persistence of selected values
- Maintain consistency with other user preferences

## Constraints
- Do NOT break existing functionality tied to these settings
- Do NOT change how values are used in calculations
- Do NOT change API contracts unless strictly necessary
- Keep changes incremental and localized
- Follow existing architecture and UI patterns

## Implementation Notes
- Identify current location and state handling of these settings
- Move UI controls to configuration page
- Ensure state is still globally accessible where needed
- Reuse existing components for inputs/selectors
- Avoid duplicating logic for state management

## Validation
- Settings can be updated from the configuration page
- Values are correctly reflected across the application
- Main menu is cleaner and less cluttered
- No regression in calculations or UI behavior

## Done Criteria
- Settings are removed from main menu
- Settings are available and functional in configuration page
- Application reflects selected values consistently
- Navigation is cleaner and more intuitive