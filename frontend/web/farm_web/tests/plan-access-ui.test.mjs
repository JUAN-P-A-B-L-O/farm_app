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
  assert.match(source, /path="\/plans" element=\{<PlansPage \/>\}/)
})

test('shared export button source centralizes csv plan checks', () => {
  const source = readSource('src/components/common/ExportCsvButton.tsx')

  assert.match(source, /getFeatureAccessState\(user, 'CSV_EXPORT'\)/)
  assert.match(source, /openUpgradePrompt\('CSV_EXPORT'\)/)
  assert.match(source, /disabled=\{disabled \|\| isLoading\}/)
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
  assert.match(source, /const \{ openUpgradePrompt \} = usePlanUpgrade\(\)/)
  assert.match(source, /const accessState = getFeatureAccessState\(user, feature\)/)
  assert.match(source, /openUpgradePrompt\(feature\)/)
  assert.match(source, /if \(!accessState\.allowed\) \{/)
})

test('app layout source exposes premium navigation items without duplicating rules', () => {
  const source = readSource('src/layout/AppLayout.tsx')

  assert.match(source, /feature: 'DASHBOARD'/)
  assert.match(source, /feature: 'ANALYTICS'/)
  assert.match(source, /labelKey: 'layout\.navigation\.plans'/)
  assert.match(source, /getFeatureAccessState\(user, restrictedFeature\)/)
  assert.match(source, /featureAccessState !== null && !featureAccessState\.allowed/)
  assert.match(source, /className="app-layout__nav-link app-layout__nav-link--disabled"/)
  assert.match(source, /openUpgradePrompt\(restrictedFeature\)/)
  assert.match(source, /t\('plan\.badge'\)/)
  assert.match(source, /t\('plan\.currentLabel'\)/)
})

test('shared upgrade prompt source includes a modal provider and dedicated plans page', () => {
  const providerSource = readSource('src/context/PlanUpgradeContext.tsx')
  const modalSource = readSource('src/components/common/PlanUpgradeModal.tsx')
  const pageSource = readSource('src/pages/plans/PlansPage.tsx')

  assert.match(providerSource, /<PlanUpgradeModal/)
  assert.match(providerSource, /navigate\('\/plans'/)
  assert.match(modalSource, /role="dialog"/)
  assert.match(modalSource, /t\('plan\.modal\.cta'\)/)
  assert.match(pageSource, /availablePlans\.map\(\(plan\) =>/)
  assert.match(pageSource, /getPlanFeatureSummaries\(plan\)/)
})
