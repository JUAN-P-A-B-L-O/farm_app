import api from './api'
import type {
  Production,
  ProductionFormData,
  ProductionTrendPoint,
  UpdateProductionPayload,
} from '../types/production'

export async function getAllProductions(): Promise<Production[]> {
  const response = await api.get<Production[]>('/productions')

  return response.data
}

export async function getProductionsByAnimalId(animalId: string): Promise<ProductionTrendPoint[]> {
  const response = await api.get<ProductionTrendPoint[]>('/productions', {
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

export async function getProductionById(id: string): Promise<Production> {
  const response = await api.get<Production>(`/productions/${id}`)

  return response.data
}

export async function updateProduction(id: string, data: ProductionFormData): Promise<Production> {
  const payload: UpdateProductionPayload = {
    animalId: data.animalId,
    date: data.date,
    quantity: data.quantity,
  }

  const response = await api.put<Production>(`/productions/${id}`, payload)

  return response.data
}

export async function deleteProduction(id: string): Promise<void> {
  await api.delete(`/productions/${id}`)
}
