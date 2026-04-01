import api from './api'
import type {
  CreateFeedingPayload,
  Feeding,
  FeedingFeedTypeOption,
  FeedingFormData,
} from '../types/feeding'

export async function getAllFeedings(): Promise<Feeding[]> {
  const response = await api.get<Feeding[]>('/feedings')

  return response.data
}

export async function getFeedingsByAnimalId(animalId: string): Promise<Feeding[]> {
  const response = await api.get<Feeding[]>('/feedings', {
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

export async function getAllFeedTypes(): Promise<FeedingFeedTypeOption[]> {
  const response = await api.get<FeedingFeedTypeOption[]>('/feed-types')

  return response.data
}
