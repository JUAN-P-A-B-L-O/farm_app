import api from './api'
import { downloadCsv } from './csvExportService'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type { ProductionUnit } from '../utils/measurementUnits'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'
import type {
  CreateProductionPayload,
  Production,
  ProductionFormData,
  ProductionListFilters,
  ProductionTrendPoint,
  UpdateProductionPayload,
} from '../types/production'

function buildProductionListParams(farmId?: string, filters?: ProductionListFilters) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
    ...(filters?.animalId ? { animalId: filters.animalId } : {}),
    ...(filters?.date ? { date: filters.date } : {}),
  }
}

export async function getAllProductions(farmId?: string): Promise<Production[]> {
  const response = await api.get<Production[]>('/productions', {
    params: buildProductionListParams(farmId),
  })

  return response.data
}

export async function getProductionsPage(
  farmId: string | undefined,
  pagination: PaginationParams,
  filters?: ProductionListFilters,
): Promise<PaginatedResponse<Production>> {
  const response = await api.get<PaginatedResponse<Production>>('/productions', {
    params: {
      ...buildProductionListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function getProductionsByAnimalId(animalId: string, farmId?: string): Promise<ProductionTrendPoint[]> {
  const response = await api.get<ProductionTrendPoint[]>('/productions', {
    params: buildProductionListParams(farmId, { search: '', animalId, date: '' }),
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
    params: buildProductionListParams(farmId),
  })

  return response.data
}

export async function getProductionById(id: string, farmId?: string): Promise<Production> {
  const response = await api.get<Production>(`/productions/${id}`, {
    params: buildProductionListParams(farmId),
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
    params: buildProductionListParams(farmId),
  })

  return response.data
}

export async function deleteProduction(id: string, farmId?: string): Promise<void> {
  await api.delete(`/productions/${id}`, {
    params: buildProductionListParams(farmId),
  })
}

export async function exportProductionsCsv(
  farmId?: string,
  filters?: ProductionListFilters,
  measurementUnit?: ProductionUnit,
): Promise<void> {
  await downloadCsv(
    '/productions/export',
    {
      ...buildProductionListParams(farmId, filters),
      ...(measurementUnit ? { measurementUnit } : {}),
    },
    'productions.csv',
  )
}
