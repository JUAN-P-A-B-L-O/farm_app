import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
import type { DashboardSummary } from '../types/dashboard'

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
  const response = await api.get<DashboardSummary>('/dashboard', {
    params: buildDashboardParams(farmId, includeAcquisitionCost, currency),
  })

  return response.data
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
