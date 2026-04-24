import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
import type { DashboardSummary } from '../types/dashboard'

const inFlightDashboardRequests = new Map<string, Promise<DashboardSummary>>()

function buildDashboardParams(farmId?: string, includeAcquisitionCost = true, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    includeAcquisitionCost,
    ...(currency ? { currency } : {}),
  }
}

export async function fetchDashboard(
  farmId?: string,
  includeAcquisitionCost = true,
  currency?: CurrencyCode,
): Promise<DashboardSummary> {
  const params = buildDashboardParams(farmId, includeAcquisitionCost, currency)
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
): Promise<void> {
  await downloadCsv(
    '/dashboard/export',
    buildDashboardParams(farmId, includeAcquisitionCost, currency),
    'dashboard-summary.csv',
  )
}
