import api from './api'
import { publishSuccess } from './feedbackService'
import type { Farm, FarmFormData } from '../types/farm'

interface GetAccessibleFarmsOptions {
  ownedOnly?: boolean
}

export async function getAccessibleFarms(options?: GetAccessibleFarmsOptions): Promise<Farm[]> {
  const response = await api.get<Farm[]>('/farms', {
    params: options?.ownedOnly ? { ownedOnly: true } : undefined,
  })

  return response.data
}

export async function createFarm(data: FarmFormData): Promise<Farm> {
  const response = await api.post<Farm>('/farms', data)
  publishSuccess('farm.success.create', { dedupeKey: 'farm:create' })

  return response.data
}
