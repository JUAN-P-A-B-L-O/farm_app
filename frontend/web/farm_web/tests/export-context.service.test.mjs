import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-export-context')

function compileService(sourcePath, outputName, replacements = []) {
  const source = readFileSync(sourcePath, 'utf8')
  let compiled = source

  for (const [pattern, replacement] of replacements) {
    compiled = compiled.replace(pattern, replacement)
  }

  writeFileSync(path.join(compiledRoot, outputName), compiled)
}

function compileServices() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  compileService(
    path.join(projectRoot, 'src', 'services', 'userService.ts'),
    'userService.js',
    [
      ["import api from './api'\n", "import api from './api.js'\n"],
      ["import { downloadCsv } from './csvExportService'\n", "import { downloadCsv } from './csvExportService.js'\n"],
      ["import type { PaginatedResponse, PaginationParams } from '../types/pagination'\n", ''],
      ["import type { User, UserFormData, UserListFilters } from '../types/user'\n", ''],
      [/function buildUserListParams\(filters\?: UserListFilters\) \{/g, 'function buildUserListParams(filters) {'],
      [/export async function getAllUsers\(filters\?: UserListFilters\): Promise<User\[]> \{/g, 'export async function getAllUsers(filters) {'],
      [/export async function getUsersPage\([\s\S]*?\): Promise<PaginatedResponse<User>> \{/g, 'export async function getUsersPage(filters, pagination) {'],
      [/export async function createUser\(data: UserFormData\): Promise<User> \{/g, 'export async function createUser(data) {'],
      [/export async function updateUser\(id: string, data: UserFormData\): Promise<User> \{/g, 'export async function updateUser(id, data) {'],
      [/export async function inactivateUser\(id: string\): Promise<User> \{/g, 'export async function inactivateUser(id) {'],
      [/export async function activateUser\(id: string, password\?: string\): Promise<User> \{/g, 'export async function activateUser(id, password) {'],
      [/export async function deleteUser\(id: string\): Promise<void> \{/g, 'export async function deleteUser(id) {'],
      [/export async function updateOwnPassword\(currentPassword: string, newPassword: string\): Promise<void> \{/g, 'export async function updateOwnPassword(currentPassword, newPassword) {'],
      [/export async function exportUsersCsv\(filters\?: UserListFilters\): Promise<void> \{/g, 'export async function exportUsersCsv(filters) {'],
      [/const response = await api.get<User\[]>/g, 'const response = await api.get'],
      [/const response = await api.get<PaginatedResponse<User>>/g, 'const response = await api.get'],
      [/const response = await api.post<User>/g, 'const response = await api.post'],
      [/const response = await api.put<User>/g, 'const response = await api.put'],
      [/const response = await api.patch<User>/g, 'const response = await api.patch'],
    ],
  )

  compileService(
    path.join(projectRoot, 'src', 'services', 'dashboardService.ts'),
    'dashboardService.js',
    [
      ["import api from './api'\n", "import api from './api.js'\n"],
      ["import { downloadCsv } from './csvExportService'\n", "import { downloadCsv } from './csvExportService.js'\n"],
      ["import type { CurrencyCode } from '../context/CurrencyContext'\n", ''],
      ["import type { DashboardFilters, DashboardSummary } from '../types/dashboard'\n", ''],
      ["const inFlightDashboardRequests = new Map<string, Promise<DashboardSummary>>()\n", 'const inFlightDashboardRequests = new Map()\n'],
      [/function buildDashboardParams\([\s\S]*?\) \{/g, 'function buildDashboardParams(farmId, includeAcquisitionCost = true, currency, filters) {'],
      [/export async function fetchDashboard\([\s\S]*?\): Promise<DashboardSummary> \{/g, 'export async function fetchDashboard(farmId, includeAcquisitionCost = true, currency, filters) {'],
      [/export async function exportDashboardCsv\([\s\S]*?\): Promise<void> \{/g, 'export async function exportDashboardCsv(farmId, includeAcquisitionCost = true, currency, filters) {'],
      [/api.get<DashboardSummary>/g, 'api.get'],
    ],
  )

  writeFileSync(
    path.join(compiledRoot, 'api.js'),
    'const api = globalThis.__exportContextApiStub;\nexport default api;\n',
  )
  writeFileSync(
    path.join(compiledRoot, 'csvExportService.js'),
    'export const downloadCsv = (...args) => globalThis.__exportContextDownloadCsv(...args);\n',
  )
}

compileServices()

const apiStub = {
  requests: [],
  async get(url, config) {
    this.requests.push({ url, config })
    return { data: { ok: true } }
  },
}

const downloadCsvCalls = []

globalThis.__exportContextApiStub = apiStub
globalThis.__exportContextDownloadCsv = (...args) => {
  downloadCsvCalls.push(args)
}

const userServiceModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'userService.js')).href}?t=${Date.now()}`
const dashboardServiceModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'dashboardService.js')).href}?t=${Date.now()}`

const userService = await import(userServiceModuleUrl)
const dashboardService = await import(dashboardServiceModuleUrl)

test.beforeEach(() => {
  apiStub.requests = []
  downloadCsvCalls.length = 0
})

test('user listing and export reuse the same active filters', async () => {
  const filters = {
    search: 'Jane',
    active: 'true',
    role: 'MANAGER',
  }

  await userService.getAllUsers(filters)
  await userService.exportUsersCsv(filters)

  assert.deepEqual(apiStub.requests[0], {
    url: '/users',
    config: {
      params: filters,
    },
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/users/export',
    filters,
    'users.csv',
  ])
})

test('user export drops empty filter values the same way listing requests do', async () => {
  const filters = {
    search: '',
    active: '',
    role: 'WORKER',
  }

  await userService.getAllUsers(filters)
  await userService.exportUsersCsv(filters)

  assert.deepEqual(apiStub.requests[0].config.params, {
    role: 'WORKER',
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/users/export',
    { role: 'WORKER' },
    'users.csv',
  ])
})

test('user listing and export preserve an inactive status filter', async () => {
  const filters = {
    search: '',
    active: 'false',
    role: '',
  }

  await userService.getAllUsers(filters)
  await userService.exportUsersCsv(filters)

  assert.deepEqual(apiStub.requests[0].config.params, {
    active: 'false',
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/users/export',
    { active: 'false' },
    'users.csv',
  ])
})

test('dashboard fetch and export share the same farm, acquisition-cost, and currency context', async () => {
  await dashboardService.fetchDashboard('farm-1', false, 'USD')
  await dashboardService.exportDashboardCsv('farm-1', false, 'USD')

  assert.deepEqual(apiStub.requests[0], {
    url: '/dashboard',
    config: {
      params: {
        farmId: 'farm-1',
        includeAcquisitionCost: false,
        currency: 'USD',
      },
    },
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/dashboard/export',
    {
      farmId: 'farm-1',
      includeAcquisitionCost: false,
      currency: 'USD',
    },
    'dashboard-summary.csv',
  ])
})

test('dashboard export keeps the default acquisition-cost context without a farm filter', async () => {
  await dashboardService.fetchDashboard()
  await dashboardService.exportDashboardCsv()

  assert.deepEqual(apiStub.requests[0], {
    url: '/dashboard',
    config: {
      params: {
        includeAcquisitionCost: true,
      },
    },
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/dashboard/export',
    {
      includeAcquisitionCost: true,
    },
    'dashboard-summary.csv',
  ])
})

test('dashboard fetch and export preserve multi-animal, date, and status filters', async () => {
  const filters = {
    startDate: '2026-01-01',
    endDate: '2026-01-31',
    animalIds: ['animal-1', 'animal-2'],
    status: 'ACTIVE',
  }

  await dashboardService.fetchDashboard('farm-1', true, 'USD', filters)
  await dashboardService.exportDashboardCsv('farm-1', true, 'USD', filters)

  assert.deepEqual(apiStub.requests[0], {
    url: '/dashboard',
    config: {
      params: {
        farmId: 'farm-1',
        startDate: '2026-01-01',
        endDate: '2026-01-31',
        animalIds: 'animal-1,animal-2',
        status: 'ACTIVE',
        includeAcquisitionCost: true,
        currency: 'USD',
      },
    },
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/dashboard/export',
    {
      farmId: 'farm-1',
      startDate: '2026-01-01',
      endDate: '2026-01-31',
      animalIds: 'animal-1,animal-2',
      status: 'ACTIVE',
      includeAcquisitionCost: true,
      currency: 'USD',
    },
    'dashboard-summary.csv',
  ])
})

test('dashboard fetch and export use the singular animalId parameter for a single selected animal', async () => {
  const filters = {
    startDate: '2026-01-01',
    endDate: '2026-01-31',
    animalIds: ['animal-1'],
    status: 'ACTIVE',
  }

  await dashboardService.fetchDashboard('farm-1', true, 'USD', filters)
  await dashboardService.exportDashboardCsv('farm-1', true, 'USD', filters)

  assert.deepEqual(apiStub.requests[0], {
    url: '/dashboard',
    config: {
      params: {
        farmId: 'farm-1',
        startDate: '2026-01-01',
        endDate: '2026-01-31',
        animalId: 'animal-1',
        status: 'ACTIVE',
        includeAcquisitionCost: true,
        currency: 'USD',
      },
    },
  })
  assert.deepEqual(downloadCsvCalls[0], [
    '/dashboard/export',
    {
      farmId: 'farm-1',
      startDate: '2026-01-01',
      endDate: '2026-01-31',
      animalId: 'animal-1',
      status: 'ACTIVE',
      includeAcquisitionCost: true,
      currency: 'USD',
    },
    'dashboard-summary.csv',
  ])
})
