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

test('shared filter fields preserve the new multiselect, checkbox, and help-text behavior', () => {
  const source = readSource('src/components/common/FilterFields.tsx')

  assert.match(source, /export function MultiSelectFilterField\(/)
  assert.match(source, /multiple/)
  assert.match(source, /size=\{size\}/)
  assert.match(source, /Array\.from\(event\.target\.selectedOptions, \(option\) => option\.value\)/)
  assert.match(source, /export function CheckboxFilterField\(/)
  assert.match(source, /type="checkbox"/)
  assert.match(source, /onChange=\{\(event\) => onChange\(event\.target\.checked\)\}/)
  assert.match(source, /listing-filters__help/)
})

test('listing filter consumers wire the new dashboard and analytics filter branches', () => {
  const listingFiltersBarSource = readSource('src/components/common/ListingFiltersBar.tsx')
  const dashboardPageSource = readSource('src/pages/dashboard/DashboardPage.tsx')
  const analyticsPageSource = readSource('src/pages/analytics/AnalyticsPage.tsx')
  const appCssSource = readSource('src/App.css')

  assert.match(listingFiltersBarSource, /className\?: string/)
  assert.match(listingFiltersBarSource, /className = ''/)
  assert.match(listingFiltersBarSource, /const rootClassName = \['listing-filters', className\]\.filter\(Boolean\)\.join\(' '\)/)
  assert.match(listingFiltersBarSource, /<div className=\{rootClassName\}>/)
  assert.match(listingFiltersBarSource, /MultiSelectFilterField/)
  assert.match(listingFiltersBarSource, /CheckboxFilterField/)
  assert.match(listingFiltersBarSource, /filter\.type === 'multiselect'/)
  assert.match(listingFiltersBarSource, /<CheckboxFilterField/)

  assert.match(dashboardPageSource, /type: 'multiselect'/)
  assert.match(dashboardPageSource, /id: 'dashboard-animal-select'/)
  assert.match(dashboardPageSource, /helpText: t\('dashboard\.filters\.allAnimalsHint'\)/)
  assert.match(dashboardPageSource, /type: 'checkbox'/)
  assert.match(dashboardPageSource, /id: 'dashboard-include-acquisition-cost'/)
  assert.match(dashboardPageSource, /<main className="animals-page dashboard-page">/)
  assert.match(dashboardPageSource, /<section className="dashboard-layout">/)
  assert.match(dashboardPageSource, /<article className="animals-panel dashboard-panel">/)
  assert.match(dashboardPageSource, /className="listing-filters--dashboard"/)
  assert.match(dashboardPageSource, /animals-panel__header animals-panel__header--actions/)
  assert.match(dashboardPageSource, /animals-page__status/)
  assert.doesNotMatch(dashboardPageSource, /dashboard-page__status/)
  assert.doesNotMatch(dashboardPageSource, /dashboard-page__header-actions/)
  assert.match(appCssSource, /\.dashboard-layout \{/)
  assert.match(appCssSource, /\.dashboard-grid \{/)
  assert.match(appCssSource, /\.dashboard-panel \{/)
  assert.match(appCssSource, /\.listing-filters--dashboard \{/)
  assert.match(appCssSource, /\.listing-filters--dashboard \.listing-filters__controls \{/)

  assert.match(analyticsPageSource, /type: 'checkbox'/)
  assert.match(analyticsPageSource, /id: 'analytics-include-acquisition-cost'/)
})

test('listing filters now auto-apply across the updated pages', () => {
  const listingFiltersBarSource = readSource('src/components/common/ListingFiltersBar.tsx')

  assert.doesNotMatch(listingFiltersBarSource, /onApply:/)
  assert.doesNotMatch(listingFiltersBarSource, /applyLabel:/)
  assert.doesNotMatch(listingFiltersBarSource, /onClick=\{onApply\}/)

  const autoAppliedPages = [
    'src/pages/analytics/AnalyticsPage.tsx',
    'src/pages/animals/AnimalsPage.tsx',
    'src/pages/batch/AnimalBatchPage.tsx',
    'src/pages/dashboard/DashboardPage.tsx',
    'src/pages/feed-type/FeedTypePage.tsx',
    'src/pages/feeding/FeedingPage.tsx',
    'src/pages/milk-price/MilkPricePage.tsx',
    'src/pages/production/ProductionPage.tsx',
    'src/pages/users/UsersPage.tsx',
  ]

  for (const relativePath of autoAppliedPages) {
    const source = readSource(relativePath)

    assert.match(source, /useAutoAppliedFilters\(/)
    assert.doesNotMatch(source, /onApply=/)
    assert.doesNotMatch(source, /applyLabel=/)
    assert.match(source, /resetFilters\(\)/)
  }

  assert.match(readSource('src/pages/animals/AnimalsPage.tsx'), /debounceKeys: debouncedAnimalFilterKeys/)
  assert.match(readSource('src/pages/batch/AnimalBatchPage.tsx'), /debounceKeys: debouncedBatchFilterKeys/)
  assert.match(readSource('src/pages/feed-type/FeedTypePage.tsx'), /debounceKeys: debouncedFeedTypeFilterKeys/)
  assert.match(readSource('src/pages/feeding/FeedingPage.tsx'), /debounceKeys: debouncedFeedingFilterKeys/)
  assert.match(readSource('src/pages/milk-price/MilkPricePage.tsx'), /debounceKeys: debouncedMilkPriceFilterKeys/)
  assert.match(readSource('src/pages/production/ProductionPage.tsx'), /debounceKeys: debouncedProductionFilterKeys/)
  assert.match(readSource('src/pages/users/UsersPage.tsx'), /debounceKeys: debouncedUserFilterKeys/)
})

test('App.css keeps action controls stacked on mobile and relaxes them at tablet widths', () => {
  const appCssSource = readSource('src/App.css')

  assert.match(appCssSource, /\.animals-panel__header--actions \{[\s\S]*flex-direction: column;[\s\S]*align-items: stretch;[\s\S]*\}/)
  assert.match(appCssSource, /\.animals-panel__header--actions > \* \{\s+min-width: 0;\s+\}/)
  assert.match(appCssSource, /\.animal-form__actions \{\s+display: grid;\s+grid-template-columns: minmax\(0, 1fr\);\s+gap: 12px;\s+\}/)
  assert.match(appCssSource, /\.animal-form__actions button,\s+\.animals-table__action-button \{[\s\S]*display: inline-flex;[\s\S]*width: 100%;[\s\S]*max-width: 100%;[\s\S]*text-align: center;[\s\S]*white-space: normal;[\s\S]*\}/)
  assert.match(appCssSource, /\.listing-filters__actions \{\s+display: grid;\s+grid-template-columns: minmax\(0, 1fr\);\s+gap: 12px;\s+\}/)
  assert.match(appCssSource, /\.analytics-chart__header > \.animals-table__action-button,\s+\.animals-panel__header--actions > \.animals-table__action-button,\s+\.listing-filters__actions > \.animals-table__action-button,\s+\.listing-filters__actions > button \{\s+width: 100%;\s+\}/)
  assert.match(appCssSource, /@media \(min-width: 768px\) \{[\s\S]*\.animal-form__actions,\s+\.listing-filters__actions \{\s+display: flex;\s+flex-wrap: wrap;\s+\}[\s\S]*\.animal-form__actions button,\s+\.animals-table__action-button \{\s+width: auto;\s+white-space: nowrap;\s+\}[\s\S]*\.animals-panel__header--actions,[\s\S]*\.analytics-chart__header \{[\s\S]*flex-direction: row;[\s\S]*align-items: flex-start;[\s\S]*\}/)
})
