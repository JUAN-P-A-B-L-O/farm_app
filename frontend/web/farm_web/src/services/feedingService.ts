import api from './api'
import { downloadCsv } from './csvExportService'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type { FeedingUnit } from '../utils/measurementUnits'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'
import type {
  CreateFeedingPayload,
  Feeding,
  FeedingFormData,
  FeedingListFilters,
  FeedingTrendPoint,
  UpdateFeedingPayload,
} from '../types/feeding'
export { getAllFeedTypes } from './feedTypeService'

function buildFeedingListParams(farmId?: string, filters?: FeedingListFilters) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
    ...(filters?.animalId ? { animalId: filters.animalId } : {}),
    ...(filters?.feedTypeId ? { feedTypeId: filters.feedTypeId } : {}),
    ...(filters?.date ? { date: filters.date } : {}),
  }
}

export async function getAllFeedings(farmId?: string): Promise<Feeding[]> {
  const response = await api.get<Feeding[]>('/feedings', {
    params: buildFeedingListParams(farmId),
  })

  return response.data
}

export async function getFeedingsPage(
  farmId: string | undefined,
  pagination: PaginationParams,
  filters?: FeedingListFilters,
): Promise<PaginatedResponse<Feeding>> {
  const response = await api.get<PaginatedResponse<Feeding>>('/feedings', {
    params: {
      ...buildFeedingListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function getFeedingsByAnimalId(animalId: string, farmId?: string): Promise<FeedingTrendPoint[]> {
  const response = await api.get<FeedingTrendPoint[]>('/feedings', {
    params: buildFeedingListParams(farmId, { search: '', animalId, feedTypeId: '', date: '' }),
  })

  return response.data
}

export async function createFeeding(data: FeedingFormData, farmId?: string): Promise<Feeding> {
  const payload: CreateFeedingPayload = {
    animalId: data.animalId,
    feedTypeId: data.feedTypeId,
    quantity: normalizeToTwoDecimals(data.quantity),
    userId: data.userId,
    ...(data.date ? { date: data.date } : {}),
  }

  const response = await api.post<Feeding>('/feedings', payload, {
    params: buildFeedingListParams(farmId),
  })

  return response.data
}

export async function getFeedingById(id: string, farmId?: string): Promise<Feeding> {
  const response = await api.get<Feeding>(`/feedings/${id}`, {
    params: buildFeedingListParams(farmId),
  })

  return response.data
}

export async function updateFeeding(id: string, data: FeedingFormData, farmId?: string): Promise<Feeding> {
  const payload: UpdateFeedingPayload = {
    animalId: data.animalId,
    feedTypeId: data.feedTypeId,
    date: data.date,
    quantity: normalizeToTwoDecimals(data.quantity),
  }

  const response = await api.put<Feeding>(`/feedings/${id}`, payload, {
    params: buildFeedingListParams(farmId),
  })

  return response.data
}

export async function deleteFeeding(id: string, farmId?: string): Promise<void> {
  await api.delete(`/feedings/${id}`, {
    params: buildFeedingListParams(farmId),
  })
}

export async function exportFeedingsCsv(
  farmId?: string,
  filters?: FeedingListFilters,
  measurementUnit?: FeedingUnit,
): Promise<void> {
  await downloadCsv(
    '/feedings/export',
    {
      ...buildFeedingListParams(farmId, filters),
      ...(measurementUnit ? { measurementUnit } : {}),
    },
    'feedings.csv',
  )
}
