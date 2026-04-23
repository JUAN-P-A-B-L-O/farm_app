import api from './api'
import { downloadCsv } from './csvExportService'
import type { DashboardSummary } from '../types/dashboard'

export async function fetchDashboard(farmId?: string, includeAcquisitionCost = true): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/dashboard', {
    params: {
      ...(farmId ? { farmId } : {}),
      includeAcquisitionCost,
    },
  })

  return response.data
}

export async function exportDashboardCsv(farmId?: string, includeAcquisitionCost = true): Promise<void> {
  await downloadCsv('/dashboard/export', {
    ...(farmId ? { farmId } : {}),
    includeAcquisitionCost,
  }, 'dashboard-summary.csv')
}
