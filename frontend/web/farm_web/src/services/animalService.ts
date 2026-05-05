import api from './api'
import { downloadCsv } from './csvExportService'
import { publishSuccess } from './feedbackService'
import type { CurrencyCode } from '../context/CurrencyContext'
import type { Animal, AnimalFormData, AnimalListFilters, SellAnimalData } from '../types/animal'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'

const inFlightAnimalListRequests = new Map<string, Promise<Animal[]>>()

function buildAnimalListParams(farmId?: string, filters?: AnimalListFilters, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
    ...(filters?.status ? { status: filters.status } : {}),
    ...(filters?.origin ? { origin: filters.origin } : {}),
    ...(currency ? { currency } : {}),
  }
}

export async function getAllAnimals(farmId?: string, filters?: AnimalListFilters): Promise<Animal[]> {
  const params = buildAnimalListParams(farmId, filters)
  const requestKey = JSON.stringify(params)
  const existingRequest = inFlightAnimalListRequests.get(requestKey)

  if (existingRequest) {
    return existingRequest
  }

  const request = api.get<Animal[]>('/animals', { params })
    .then((response) => response.data)
    .finally(() => {
      inFlightAnimalListRequests.delete(requestKey)
    })

  inFlightAnimalListRequests.set(requestKey, request)

  return request
}

export async function getAnimalsPage(
  farmId: string | undefined,
  pagination: PaginationParams,
  filters?: AnimalListFilters,
): Promise<PaginatedResponse<Animal>> {
  const response = await api.get<PaginatedResponse<Animal>>('/animals', {
    params: {
      ...buildAnimalListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function getAnimalById(id: string, farmId?: string): Promise<Animal> {
  const response = await api.get<Animal>(`/animals/${id}`, {
    params: buildAnimalListParams(farmId),
  })

  return response.data
}

export async function createAnimal(data: AnimalFormData): Promise<Animal> {
  const response = await api.post<Animal>('/animals', data)
  publishSuccess('animals.success.create', { dedupeKey: 'animals:create' })

  return response.data
}

export async function updateAnimal(id: string, data: AnimalFormData, farmId?: string): Promise<Animal> {
  const response = await api.put<Animal>(`/animals/${id}`, data, {
    params: buildAnimalListParams(farmId),
  })
  publishSuccess('animals.success.update', { dedupeKey: 'animals:update' })

  return response.data
}

export async function deleteAnimal(id: string, farmId?: string): Promise<void> {
  await api.delete(`/animals/${id}`, {
    params: buildAnimalListParams(farmId),
  })
  publishSuccess('animals.success.delete', { dedupeKey: 'animals:delete' })
}

export async function sellAnimal(id: string, data: SellAnimalData, farmId?: string): Promise<Animal> {
  const response = await api.post<Animal>(`/animals/${id}/sell`, data, {
    params: buildAnimalListParams(farmId),
  })
  publishSuccess('animals.success.sell', { dedupeKey: 'animals:sell' })

  return response.data
}

export async function exportAnimalsCsv(
  farmId?: string,
  currency?: CurrencyCode,
  filters?: AnimalListFilters,
): Promise<void> {
  await downloadCsv('/animals/export', buildAnimalListParams(farmId, filters, currency), {
    fallbackFileName: 'animals.csv',
    successDedupeKey: 'animals:export',
    successMessageKey: 'animals.success.export',
  })
}
