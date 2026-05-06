import api from './api'
import { downloadCsv } from './csvExportService'
import { publishSuccess } from './feedbackService'
import type { CurrencyCode } from '../context/CurrencyContext'
import type { FeedingUnit } from '../utils/measurementUnits'
import type { FeedType, FeedTypeFormData, FeedTypeListFilters } from '../types/feedType'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'
import { normalizeToTwoDecimals } from '../utils/decimal'

function buildFeedTypeListParams(farmId?: string, filters?: FeedTypeListFilters, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
    ...(currency ? { currency } : {}),
  }
}

export async function getAllFeedTypes(farmId?: string, filters?: FeedTypeListFilters): Promise<FeedType[]> {
  const response = await api.get<FeedType[]>('/feed-types', {
    params: buildFeedTypeListParams(farmId, filters),
  })

  return response.data
}

export async function getFeedTypesPage(
  farmId: string | undefined,
  pagination: PaginationParams,
  filters?: FeedTypeListFilters,
): Promise<PaginatedResponse<FeedType>> {
  const response = await api.get<PaginatedResponse<FeedType>>('/feed-types', {
    params: {
      ...buildFeedTypeListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function createFeedType(data: FeedTypeFormData, farmId?: string): Promise<FeedType> {
  const response = await api.post<FeedType>('/feed-types', {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFeedTypeListParams(farmId),
  })
  publishSuccess('feedType.success.create', { dedupeKey: 'feed-type:create' })

  return response.data
}

export async function updateFeedType(id: string, data: FeedTypeFormData, farmId?: string): Promise<FeedType> {
  const response = await api.put<FeedType>(`/feed-types/${id}`, {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFeedTypeListParams(farmId),
  })
  publishSuccess('feedType.success.update', { dedupeKey: 'feed-type:update' })

  return response.data
}

export async function deleteFeedType(id: string, farmId?: string): Promise<void> {
  await api.delete(`/feed-types/${id}`, {
    params: buildFeedTypeListParams(farmId),
  })
  publishSuccess('feedType.success.delete', { dedupeKey: 'feed-type:delete' })
}

export async function exportFeedTypesCsv(
  farmId?: string,
  currency?: CurrencyCode,
  filters?: FeedTypeListFilters,
  measurementUnit?: FeedingUnit,
): Promise<void> {
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
