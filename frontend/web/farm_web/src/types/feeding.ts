export interface FeedingAnimalOption {
  id: string
  tag: string
}

export interface FeedingFeedTypeOption {
  id: string
  name: string
  costPerKg: number
}

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
  animalId: string
  feedTypeId: string
  date: string
  quantity: number
  userId: string
}

export interface CreateFeedingPayload extends FeedingFormData {
  userId: string
}

export interface FeedingApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
