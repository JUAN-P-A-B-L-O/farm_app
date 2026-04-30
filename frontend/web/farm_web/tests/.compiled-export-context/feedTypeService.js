import api from './api.js'
import { downloadCsv } from './csvExportService.js'
const normalizeToTwoDecimals = (value) => Number(value.toFixed(2))

function buildFeedTypeListParams(farmId, filters, currency) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
    ...(currency ? { currency } : {}),
  }
}

export async function getAllFeedTypes(farmId, filters) {
  const response = await api.get('/feed-types', {
    params: buildFeedTypeListParams(farmId, filters),
  })

  return response.data
}

export async function getFeedTypesPage(farmId, pagination, filters) {
  const response = await api.get('/feed-types', {
    params: {
      ...buildFeedTypeListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function createFeedType(data, farmId) {
  const response = await api.post('/feed-types', {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFeedTypeListParams(farmId),
  })

  return response.data
}

export async function updateFeedType(id, data, farmId) {
  const response = await api.put(`/feed-types/${id}`, {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFeedTypeListParams(farmId),
  })

  return response.data
}

export async function deleteFeedType(id, farmId) {
  await api.delete(`/feed-types/${id}`, {
    params: buildFeedTypeListParams(farmId),
  })
}

export async function exportFeedTypesCsv(farmId, currency, filters, measurementUnit) {
  await downloadCsv(
    '/feed-types/export',
    {
      ...buildFeedTypeListParams(farmId, filters, currency),
      ...(measurementUnit ? { measurementUnit } : {}),
    },
    'feed-types.csv',
  )
}
