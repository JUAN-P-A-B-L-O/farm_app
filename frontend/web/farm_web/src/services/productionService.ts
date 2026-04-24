import api from './api'
import { downloadCsv } from './csvExportService'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'
import type {
  CreateProductionPayload,
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

export async function getProductionsPage(
  farmId: string | undefined,
  pagination: PaginationParams,
): Promise<PaginatedResponse<Production>> {
  const response = await api.get<PaginatedResponse<Production>>('/productions', {
    params: {
      ...buildFarmParams(farmId),
      page: pagination.page,
      size: pagination.size,
    },
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
  const payload: CreateProductionPayload = {
    animalId: data.animalId,
    quantity: normalizeToTwoDecimals(data.quantity),
    userId: data.userId,
    ...(data.date ? { date: data.date } : {}),
  }

  const response = await api.post<Production>('/productions', payload, {
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
    quantity: normalizeToTwoDecimals(data.quantity),
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

export async function exportProductionsCsv(farmId?: string): Promise<void> {
  await downloadCsv('/productions/export', buildFarmParams(farmId), 'productions.csv')
}
