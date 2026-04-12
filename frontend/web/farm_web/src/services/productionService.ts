import api from './api'
import type {
  Production,
  ProductionFormData,
  ProductionTrendPoint,
  UpdateProductionPayload,
} from '../types/production'

function buildFarmParams(farmId?: string, params: Record<string, string> = {}) {
  return {
    ...(farmId ? { farmId } : {}),
    ...params,
  }
}

export async function getAllProductions(farmId?: string): Promise<Production[]> {
  const response = await api.get<Production[]>('/productions', {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getProductionsByAnimalId(animalId: string, farmId?: string): Promise<ProductionTrendPoint[]> {
  const response = await api.get<ProductionTrendPoint[]>('/productions', {
    params: buildFarmParams(farmId, { animalId }),
  })

  return response.data
}

export async function createProduction(data: ProductionFormData, farmId?: string): Promise<Production> {
  const response = await api.post<Production>('/productions', data, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getProductionById(id: string, farmId?: string): Promise<Production> {
  const response = await api.get<Production>(`/productions/${id}`, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function updateProduction(id: string, data: ProductionFormData, farmId?: string): Promise<Production> {
  const payload: UpdateProductionPayload = {
    animalId: data.animalId,
    date: data.date,
    quantity: data.quantity,
  }

  const response = await api.put<Production>(`/productions/${id}`, payload, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function deleteProduction(id: string, farmId?: string): Promise<void> {
  await api.delete(`/productions/${id}`, {
    params: buildFarmParams(farmId),
  })
}
