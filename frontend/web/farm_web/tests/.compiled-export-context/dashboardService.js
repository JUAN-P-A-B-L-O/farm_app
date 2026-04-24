import api from './api.js'
import { downloadCsv } from './csvExportService.js'

function buildDashboardParams(farmId, includeAcquisitionCost = true, currency) {
  return {
    ...(farmId ? { farmId } : {}),
    includeAcquisitionCost,
    ...(currency ? { currency } : {}),
  }
}

export async function fetchDashboard(farmId, includeAcquisitionCost = true, currency) {
  const response = await api.get('/dashboard', {
    params: buildDashboardParams(farmId, includeAcquisitionCost, currency),
  })

  return response.data
}

export async function exportDashboardCsv(farmId, includeAcquisitionCost = true, currency) {
  await downloadCsv(
    '/dashboard/export',
    buildDashboardParams(farmId, includeAcquisitionCost, currency),
    'dashboard-summary.csv',
  )
}
