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

  assert.match(listingFiltersBarSource, /MultiSelectFilterField/)
  assert.match(listingFiltersBarSource, /CheckboxFilterField/)
  assert.match(listingFiltersBarSource, /filter\.type === 'multiselect'/)
  assert.match(listingFiltersBarSource, /<CheckboxFilterField/)

  assert.match(dashboardPageSource, /type: 'multiselect'/)
  assert.match(dashboardPageSource, /id: 'dashboard-animal-select'/)
  assert.match(dashboardPageSource, /helpText: t\('dashboard\.filters\.allAnimalsHint'\)/)
  assert.match(dashboardPageSource, /size: Math\.min\(Math\.max\(animals\.length, 2\), 6\)/)
  assert.match(dashboardPageSource, /type: 'checkbox'/)
  assert.match(dashboardPageSource, /id: 'dashboard-include-acquisition-cost'/)

  assert.match(analyticsPageSource, /type: 'checkbox'/)
  assert.match(analyticsPageSource, /id: 'analytics-include-acquisition-cost'/)
})
