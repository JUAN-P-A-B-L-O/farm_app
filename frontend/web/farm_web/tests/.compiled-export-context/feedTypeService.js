import api from './api.js'
import { downloadCsv } from './csvExportService.js'
import { publishSuccess } from './feedbackService'
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
  publishSuccess('feedType.success.create', { dedupeKey: 'feed-type:create' })

  return response.data
}

export async function updateFeedType(id, data, farmId) {
  const response = await api.put(`/feed-types/${id}`, {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFeedTypeListParams(farmId),
  })
  publishSuccess('feedType.success.update', { dedupeKey: 'feed-type:update' })

  return response.data
}

export async function deleteFeedType(id, farmId) {
  await api.delete(`/feed-types/${id}`, {
    params: buildFeedTypeListParams(farmId),
  })
  publishSuccess('feedType.success.delete', { dedupeKey: 'feed-type:delete' })
}

export async function exportFeedTypesCsv(farmId, currency, filters, measurementUnit) {
  await downloadCsv(
    '/feed-types/export',
    {
      ...buildFeedTypeListParams(farmId, filters, currency),
      ...(measurementUnit ? { measurementUnit } : {}),
    },
    {
      fallbackFileName: 'feed-types.csv',
      successDedupeKey: 'feed-type:export',
      successMessageKey: 'feedType.success.export',
    },
  )
}
