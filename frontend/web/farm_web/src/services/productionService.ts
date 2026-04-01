import api from './api'
import type { Production, ProductionFormData } from '../types/production'

export async function getAllProductions(): Promise<Production[]> {
  const response = await api.get<Production[]>('/productions')

  return response.data
}

export async function getProductionsByAnimalId(animalId: string): Promise<Production[]> {
  const response = await api.get<Production[]>('/productions', {
    params: {
      animalId,
    },
  })

  return response.data
}

export async function createProduction(data: ProductionFormData): Promise<Production> {
  const response = await api.post<Production>('/productions', data)

  return response.data
}
