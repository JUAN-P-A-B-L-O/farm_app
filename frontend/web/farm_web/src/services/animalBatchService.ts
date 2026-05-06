import api from './api'
import { publishSuccess } from './feedbackService'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'
import type {
  AnimalBatch,
  AnimalBatchFormData,
  AnimalBatchListFilters,
} from '../types/animalBatch'

function buildAnimalBatchListParams(farmId?: string, filters?: AnimalBatchListFilters) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
  }
}

export async function getAllAnimalBatches(farmId?: string, filters?: AnimalBatchListFilters): Promise<AnimalBatch[]> {
  const response = await api.get<AnimalBatch[]>('/animal-batches', {
    params: buildAnimalBatchListParams(farmId, filters),
  })

  return response.data
}

export async function getAnimalBatchesPage(
  farmId: string | undefined,
  pagination: PaginationParams,
  filters?: AnimalBatchListFilters,
): Promise<PaginatedResponse<AnimalBatch>> {
  const response = await api.get<PaginatedResponse<AnimalBatch>>('/animal-batches', {
    params: {
      ...buildAnimalBatchListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function createAnimalBatch(data: AnimalBatchFormData, farmId?: string): Promise<AnimalBatch> {
  const response = await api.post<AnimalBatch>('/animal-batches', data, {
    params: buildAnimalBatchListParams(farmId),
  })
  publishSuccess('batches.success.create', { dedupeKey: 'batches:create' })

  return response.data
}

export async function updateAnimalBatch(id: string, data: AnimalBatchFormData, farmId?: string): Promise<AnimalBatch> {
  const response = await api.put<AnimalBatch>(`/animal-batches/${id}`, data, {
    params: buildAnimalBatchListParams(farmId),
  })
  publishSuccess('batches.success.update', { dedupeKey: 'batches:update' })

  return response.data
}

export async function deleteAnimalBatch(id: string, farmId?: string): Promise<void> {
  await api.delete(`/animal-batches/${id}`, {
    params: buildAnimalBatchListParams(farmId),
  })
  publishSuccess('batches.success.delete', { dedupeKey: 'batches:delete' })
}
