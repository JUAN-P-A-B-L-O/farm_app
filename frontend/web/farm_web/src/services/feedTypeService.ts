import api from './api'
import type { FeedType, FeedTypeFormData } from '../types/feedType'

export async function getAllFeedTypes(): Promise<FeedType[]> {
  const response = await api.get<FeedType[]>('/feed-types')

  return response.data
}

export async function createFeedType(data: FeedTypeFormData): Promise<FeedType> {
  const response = await api.post<FeedType>('/feed-types', data)

  return response.data
}

export async function updateFeedType(id: string, data: FeedTypeFormData): Promise<FeedType> {
  const response = await api.put<FeedType>(`/feed-types/${id}`, data)

  return response.data
}

export async function deleteFeedType(id: string): Promise<void> {
  await api.delete(`/feed-types/${id}`)
}
