import api from './api'
import type {
  CreateFeedingPayload,
  Feeding,
  FeedingFeedTypeOption,
} from '../types/feeding'

export async function getAllFeedings(): Promise<Feeding[]> {
  const response = await api.get<Feeding[]>('/feedings')

  return response.data
}

export async function createFeeding(data: CreateFeedingPayload): Promise<Feeding> {
  const response = await api.post<Feeding>('/feedings', data)

  return response.data
}

export async function getAllFeedTypes(): Promise<FeedingFeedTypeOption[]> {
  const response = await api.get<FeedingFeedTypeOption[]>('/feed-types')

  return response.data
}
