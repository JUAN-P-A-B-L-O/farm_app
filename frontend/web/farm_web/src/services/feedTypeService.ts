import api from './api'
import type { FeedType, FeedTypeFormData } from '../types/feedType'
import { normalizeToTwoDecimals } from '../utils/decimal'

function buildFarmParams(farmId?: string) {
  return farmId ? { farmId } : undefined
}

export async function getAllFeedTypes(farmId?: string): Promise<FeedType[]> {
  const response = await api.get<FeedType[]>('/feed-types', {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function createFeedType(data: FeedTypeFormData, farmId?: string): Promise<FeedType> {
  const response = await api.post<FeedType>('/feed-types', {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function updateFeedType(id: string, data: FeedTypeFormData, farmId?: string): Promise<FeedType> {
  const response = await api.put<FeedType>(`/feed-types/${id}`, {
    ...data,
    costPerKg: normalizeToTwoDecimals(data.costPerKg),
  }, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function deleteFeedType(id: string, farmId?: string): Promise<void> {
  await api.delete(`/feed-types/${id}`, {
    params: buildFarmParams(farmId),
  })
}
