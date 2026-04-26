import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
import type { DashboardFilters, DashboardSummary } from '../types/dashboard'

const inFlightDashboardRequests = new Map<string, Promise<DashboardSummary>>()

function buildDashboardParams(
  farmId?: string,
  includeAcquisitionCost = true,
  currency?: CurrencyCode,
  filters?: DashboardFilters,
) {
  const selectedAnimalIds = filters?.animalIds ?? []

  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.startDate ? { startDate: filters.startDate } : {}),
    ...(filters?.endDate ? { endDate: filters.endDate } : {}),
    ...(selectedAnimalIds.length === 1 ? { animalId: selectedAnimalIds[0] } : {}),
    ...(selectedAnimalIds.length > 1 ? { animalIds: selectedAnimalIds.join(',') } : {}),
    ...(filters?.status ? { status: filters.status } : {}),
    includeAcquisitionCost,
    ...(currency ? { currency } : {}),
  }
}

export async function fetchDashboard(
  farmId?: string,
  includeAcquisitionCost = true,
  currency?: CurrencyCode,
  filters?: DashboardFilters,
): Promise<DashboardSummary> {
  const params = buildDashboardParams(farmId, includeAcquisitionCost, currency, filters)
  const requestKey = JSON.stringify(params)
  const existingRequest = inFlightDashboardRequests.get(requestKey)

  if (existingRequest) {
    return existingRequest
  }

  const request = api.get<DashboardSummary>('/dashboard', { params })
    .then((response) => response.data)
    .finally(() => {
      inFlightDashboardRequests.delete(requestKey)
    })

  inFlightDashboardRequests.set(requestKey, request)

  return request
}

export async function exportDashboardCsv(
  farmId?: string,
  includeAcquisitionCost = true,
  currency?: CurrencyCode,
  filters?: DashboardFilters,
): Promise<void> {
  await downloadCsv(
    '/dashboard/export',
    buildDashboardParams(farmId, includeAcquisitionCost, currency, filters),
    'dashboard-summary.csv',
  )
}
