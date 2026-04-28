import type { FeedType } from './feedType'
import type { AnimalBatch } from './animalBatch'

export interface FeedingAnimalOption {
  id: string
  tag: string
}

export type FeedingFeedTypeOption = FeedType
export type FeedingBatchOption = AnimalBatch
export type FeedingOperationMode = 'INDIVIDUAL' | 'BATCH'

export interface Feeding {
  id: string
  animalId: string
  feedTypeId: string
  date: string
  quantity: number
  animal: FeedingAnimalOption
  feedType: Pick<FeedingFeedTypeOption, 'id' | 'name'>
}

export interface FeedingTrendPoint {
  date: string
  quantity: number
}

export interface FeedingFormData {
  operationMode: FeedingOperationMode
  animalId: string
  batchId: string
  feedTypeId: string
  date: string
  quantity: number
  userId: string
}

export interface CreateFeedingPayload {
  animalId: string
  feedTypeId: string
  quantity: number
  userId: string
  date?: string
}

export interface CreateBatchFeedingPayload {
  batchId: string
  feedTypeId: string
  quantity: number
  userId: string
  date?: string
}

export interface UpdateFeedingPayload {
  animalId: string
  feedTypeId: string
  date: string
  quantity: number
}

export interface FeedingApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}

export interface FeedingListFilters {
  search: string
  animalId: string
  feedTypeId: string
  date: string
}
