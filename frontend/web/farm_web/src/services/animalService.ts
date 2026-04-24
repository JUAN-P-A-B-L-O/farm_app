import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
import type { Animal, AnimalFormData, SellAnimalData } from '../types/animal'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'

function buildFarmParams(farmId?: string, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(currency ? { currency } : {}),
  }
}

export async function getAllAnimals(farmId?: string): Promise<Animal[]> {
  const response = await api.get<Animal[]>('/animals', {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getAnimalsPage(
  farmId: string | undefined,
  pagination: PaginationParams,
): Promise<PaginatedResponse<Animal>> {
  const response = await api.get<PaginatedResponse<Animal>>('/animals', {
    params: {
      ...buildFarmParams(farmId),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function getAnimalById(id: string, farmId?: string): Promise<Animal> {
  const response = await api.get<Animal>(`/animals/${id}`, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function createAnimal(data: AnimalFormData): Promise<Animal> {
  const response = await api.post<Animal>('/animals', data)

  return response.data
}

export async function updateAnimal(id: string, data: AnimalFormData, farmId?: string): Promise<Animal> {
  const response = await api.put<Animal>(`/animals/${id}`, data, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function deleteAnimal(id: string, farmId?: string): Promise<void> {
  await api.delete(`/animals/${id}`, {
    params: buildFarmParams(farmId),
  })
}

export async function sellAnimal(id: string, data: SellAnimalData, farmId?: string): Promise<Animal> {
  const response = await api.post<Animal>(`/animals/${id}/sell`, data, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function exportAnimalsCsv(farmId?: string, currency?: CurrencyCode): Promise<void> {
  await downloadCsv('/animals/export', buildFarmParams(farmId, currency), 'animals.csv')
}
