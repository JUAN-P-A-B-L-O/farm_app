import api from './api'
import type {
  CreateFeedingPayload,
  Feeding,
  FeedingFormData,
  FeedingTrendPoint,
} from '../types/feeding'
export { getAllFeedTypes } from './feedTypeService'

export async function getAllFeedings(): Promise<Feeding[]> {
  const response = await api.get<Feeding[]>('/feedings')

  return response.data
}

export async function getFeedingsByAnimalId(animalId: string): Promise<FeedingTrendPoint[]> {
  const response = await api.get<FeedingTrendPoint[]>('/feedings', {
    params: {
      animalId,
    },
  })

  return response.data
}

export async function createFeeding(data: FeedingFormData): Promise<Feeding> {
  const payload: CreateFeedingPayload = data

  const response = await api.post<Feeding>('/feedings', payload)

  return response.data
}
