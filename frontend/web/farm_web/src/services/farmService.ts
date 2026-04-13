import api from './api'
import type { Farm, FarmFormData } from '../types/farm'

export async function getAccessibleFarms(): Promise<Farm[]> {
  const response = await api.get<Farm[]>('/farms')

  return response.data
}

export async function createFarm(data: FarmFormData): Promise<Farm> {
  const response = await api.post<Farm>('/farms', data)

  return response.data
}
