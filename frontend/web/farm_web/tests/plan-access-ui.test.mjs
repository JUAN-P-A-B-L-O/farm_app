import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-plan-access')

function readSource(relativePath) {
  return readFileSync(path.join(projectRoot, relativePath), 'utf8')
}

function compilePlanAccessModule() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  const compiledSource = readSource('src/utils/planAccess.ts')
    .replace("import type { User, UserPlan } from '../types/user'\n", '')
    .replace("export type AppFeature = 'DASHBOARD' | 'ANALYTICS' | 'CSV_EXPORT'\n\n", '')
    .replace(/interface FeatureMetadata \{[\s\S]*?\}\n\n/, '')
    .replace(/interface PlanMetadata \{[\s\S]*?\}\n\n/, '')
    .replace(/export interface FeatureAccessState \{[\s\S]*?\}\n\n/, '')
    .replace(/export interface PlanFeatureSummary \{[\s\S]*?\}\n\n/, '')
    .replace('const planMetadata: Record<UserPlan, PlanMetadata> = {', 'const planMetadata = {')
    .replace('const featureMetadata: Record<AppFeature, FeatureMetadata> = {', 'const featureMetadata = {')
    .replace("export const availablePlans: UserPlan[] = ['FREE', 'PRO']\n", "export const availablePlans = ['FREE', 'PRO']\n")
    .replace(
      "export const appFeatures: AppFeature[] = ['DASHBOARD', 'ANALYTICS', 'CSV_EXPORT']\n",
      "export const appFeatures = ['DASHBOARD', 'ANALYTICS', 'CSV_EXPORT']\n",
    )
    .replace(
      "export function resolvePlan(user: Pick<User, 'plan'> | null | undefined): UserPlan {",
      'export function resolvePlan(user) {',
    )
    .replace(
      "export function getCurrentPlanMetadata(user: Pick<User, 'plan'> | null | undefined) {",
      'export function getCurrentPlanMetadata(user) {',
    )
    .replace(
      'export function getPlanMetadata(plan: UserPlan) {',
      'export function getPlanMetadata(plan) {',
    )
    .replace(
      /export function getFeatureAccessState\(\s*user: Pick<User, 'plan'> \| null \| undefined,\s*feature: AppFeature,\s*\): FeatureAccessState \{/,
      'export function getFeatureAccessState(user, feature) {',
    )
    .replace(
      "export function hasFeatureAccess(user: Pick<User, 'plan'> | null | undefined, feature: AppFeature) {",
      'export function hasFeatureAccess(user, feature) {',
    )
    .replace(
      'export function getFeatureMetadata(feature: AppFeature) {',
      'export function getFeatureMetadata(feature) {',
    )
    .replace(
      'export function getPlanFeatureSummaries(plan: UserPlan): PlanFeatureSummary[] {',
      'export function getPlanFeatureSummaries(plan) {',
    )

  writeFileSync(path.join(compiledRoot, 'planAccess.js'), compiledSource)
}

compilePlanAccessModule()

const planAccessModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'planAccess.js')).href}?t=${Date.now()}`
const {
  appFeatures,
  availablePlans,
  getFeatureAccessState,
  getPlanFeatureSummaries,
} = await import(planAccessModuleUrl)

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

test('shared access utility returns correct feature availability for each plan tier', () => {
  assert.deepEqual(availablePlans, ['FREE', 'PRO'])
  assert.deepEqual(appFeatures, ['DASHBOARD', 'ANALYTICS', 'CSV_EXPORT'])

  assert.deepEqual(
    getPlanFeatureSummaries('FREE').map(({ feature, included }) => ({ feature, included })),
    [
      { feature: 'DASHBOARD', included: false },
      { feature: 'ANALYTICS', included: false },
      { feature: 'CSV_EXPORT', included: false },
    ],
  )
  assert.deepEqual(
    getPlanFeatureSummaries('PRO').map(({ feature, included }) => ({ feature, included })),
    [
      { feature: 'DASHBOARD', included: true },
      { feature: 'ANALYTICS', included: true },
      { feature: 'CSV_EXPORT', included: true },
    ],
  )
  assert.equal(getFeatureAccessState({ plan: 'FREE' }, 'CSV_EXPORT').minimumPlan, 'PRO')
  assert.equal(getFeatureAccessState({ plan: 'PRO' }, 'CSV_EXPORT').allowed, true)
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
  const mainSource = readSource('src/main.tsx')
  const noticeSource = readSource('src/components/common/PlanUpgradeNotice.tsx')

  assert.match(providerSource, /<PlanUpgradeModal/)
  assert.match(providerSource, /navigate\('\/plans'/)
  assert.match(modalSource, /role="dialog"/)
  assert.match(modalSource, /t\('plan\.modal\.cta'\)/)
  assert.match(pageSource, /availablePlans\.map\(\(plan\) =>/)
  assert.match(pageSource, /getPlanFeatureSummaries\(plan\)/)
  assert.match(mainSource, /<BrowserRouter>\s*<PlanUpgradeProvider>\s*<App \/>/)
  assert.match(noticeSource, /const \{ navigateToPlans \} = usePlanUpgrade\(\)/)
  assert.match(noticeSource, /t\('plan\.notice\.currentPlan'/)
  assert.match(noticeSource, /onClick=\{navigateToPlans\}/)
})
