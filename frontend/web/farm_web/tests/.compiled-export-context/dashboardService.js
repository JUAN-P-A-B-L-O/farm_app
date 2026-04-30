import api from './api.js'
import { downloadCsv } from './csvExportService.js'

const inFlightDashboardRequests = new Map()

function buildDashboardParams(farmId, includeAcquisitionCost = true, currency, filters) {
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

export async function fetchDashboard(farmId, includeAcquisitionCost = true, currency, filters) {
  const params = buildDashboardParams(farmId, includeAcquisitionCost, currency, filters)
  const requestKey = JSON.stringify(params)
  const existingRequest = inFlightDashboardRequests.get(requestKey)

  if (existingRequest) {
    return existingRequest
  }

  const request = api.get('/dashboard', { params })
    .then((response) => response.data)
    .finally(() => {
      inFlightDashboardRequests.delete(requestKey)
    })

  inFlightDashboardRequests.set(requestKey, request)

  return request
}

export async function exportDashboardCsv(farmId, includeAcquisitionCost = true, currency, filters, productionUnit) {
  await downloadCsv(
    '/dashboard/export',
    {
      ...buildDashboardParams(farmId, includeAcquisitionCost, currency, filters),
      ...(productionUnit ? { productionUnit } : {}),
    },
    'dashboard-summary.csv',
  )
}
