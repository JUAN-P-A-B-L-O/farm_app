import api from './api'
import { downloadCsv } from './csvExportService'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type {
  CreateFeedingPayload,
  Feeding,
  FeedingFormData,
  FeedingTrendPoint,
  UpdateFeedingPayload,
} from '../types/feeding'
export { getAllFeedTypes } from './feedTypeService'

function buildFarmParams(farmId?: string, params: Record<string, string> = {}) {
  return {
    ...(farmId ? { farmId } : {}),
    ...params,
  }
}

export async function getAllFeedings(farmId?: string): Promise<Feeding[]> {
  const response = await api.get<Feeding[]>('/feedings', {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getFeedingsByAnimalId(animalId: string, farmId?: string): Promise<FeedingTrendPoint[]> {
  const response = await api.get<FeedingTrendPoint[]>('/feedings', {
    params: buildFarmParams(farmId, { animalId }),
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
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getFeedingById(id: string, farmId?: string): Promise<Feeding> {
  const response = await api.get<Feeding>(`/feedings/${id}`, {
    params: buildFarmParams(farmId),
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
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function deleteFeeding(id: string, farmId?: string): Promise<void> {
  await api.delete(`/feedings/${id}`, {
    params: buildFarmParams(farmId),
  })
}

export async function exportFeedingsCsv(farmId?: string): Promise<void> {
  await downloadCsv('/feedings/export', buildFarmParams(farmId), 'feedings.csv')
}
