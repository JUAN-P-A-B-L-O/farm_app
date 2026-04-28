import type { AnimalBatch } from './animalBatch'

export interface ProductionAnimalOption {
  id: string
  tag: string
}

export type ProductionBatchOption = AnimalBatch
export type ProductionOperationMode = 'INDIVIDUAL' | 'BATCH'

export interface Production {
  id: string
  animalId: string
  date: string
  quantity: number
  animal: ProductionAnimalOption
}

export interface ProductionTrendPoint {
  date: string
  quantity: number
}

export interface ProductionFormData {
  operationMode: ProductionOperationMode
  animalId: string
  batchId: string
  date: string
  quantity: number
  userId: string
}

export interface CreateProductionPayload {
  animalId: string
  quantity: number
  userId: string
  date?: string
}

export interface CreateBatchProductionPayload {
  batchId: string
  quantity: number
  userId: string
  date?: string
}

export interface UpdateProductionPayload {
  animalId: string
  date: string
  quantity: number
}

export interface ProductionApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}

export interface ProductionListFilters {
  search: string
  animalId: string
  date: string
}
