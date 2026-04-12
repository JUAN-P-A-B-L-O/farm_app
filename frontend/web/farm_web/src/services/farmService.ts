import api from './api'
import type { Farm } from '../types/farm'

export async function getAccessibleFarms(): Promise<Farm[]> {
  const response = await api.get<Farm[]>('/farms')

  return response.data
}
