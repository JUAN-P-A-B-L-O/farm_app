# Feature: Fix Desktop Layout for Dashboard

## Goal
Improve the desktop layout of the dashboard to ensure proper spacing, alignment, and visual hierarchy while keeping the mobile experience unchanged.

## Scope
- Frontend dashboard page (desktop view only)
- Layout, grid, spacing, and component alignment

## Requirements
- Fix broken layout in desktop view (misalignment, oversized inputs, poor spacing)
- Ensure filters are properly aligned in a structured grid
- Improve visual hierarchy between filters and KPI cards
- Normalize input sizes (avoid large/stretching fields)
- Keep layout compact and readable on large screens

## Constraints
- Do NOT break mobile experience
- Do NOT change filtering behavior or logic
- Do NOT change API contracts
- Keep changes incremental and localized
- Follow existing architecture and patterns

## Implementation Notes
- Replace or adjust layout strategy (prefer CSS grid over inconsistent flex usage)
- Use responsive approach:
  - Mobile → column
  - Desktop → grid with defined columns (ex: 3–5 columns depending on width)
- Group filters logically (dates, selects, toggles)
- Standardize spacing (gap, padding, margins)
- Ensure KPI cards align in a consistent row/grid
- Avoid duplicated styling logic

## Validation
- Desktop layout is visually aligned and organized
- Filters are evenly distributed and readable
- KPI cards are aligned and proportional
- Mobile layout remains unchanged
- No regression in functionality

## Done Criteria
- Dashboard is clean and usable on desktop
- Filters and cards follow a consistent layout pattern
- Visual inconsistencies are resolved