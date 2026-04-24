import api from './api'
import { downloadCsv } from './csvExportService'
import type { DashboardSummary } from '../types/dashboard'

function buildDashboardParams(farmId?: string, includeAcquisitionCost = true) {
  return {
    ...(farmId ? { farmId } : {}),
    includeAcquisitionCost,
  }
}

export async function fetchDashboard(farmId?: string, includeAcquisitionCost = true): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/dashboard', {
    params: buildDashboardParams(farmId, includeAcquisitionCost),
  })

  return response.data
}

export async function exportDashboardCsv(farmId?: string, includeAcquisitionCost = true): Promise<void> {
  await downloadCsv('/dashboard/export', buildDashboardParams(farmId, includeAcquisitionCost), 'dashboard-summary.csv')
}
