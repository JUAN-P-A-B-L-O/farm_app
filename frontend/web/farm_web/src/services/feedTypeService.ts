import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
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

  return response.data
}

export async function updateFeedType(id: string, data: FeedTypeFormData, farmId?: string): Promise<FeedType> {
  const response = await api.put<FeedType>(`/feed-types/${id}`, {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFeedTypeListParams(farmId),
  })

  return response.data
}

export async function deleteFeedType(id: string, farmId?: string): Promise<void> {
  await api.delete(`/feed-types/${id}`, {
    params: buildFeedTypeListParams(farmId),
  })
}

export async function exportFeedTypesCsv(
  farmId?: string,
  currency?: CurrencyCode,
  filters?: FeedTypeListFilters,
): Promise<void> {
  await downloadCsv('/feed-types/export', buildFeedTypeListParams(farmId, filters, currency), 'feed-types.csv')
}
