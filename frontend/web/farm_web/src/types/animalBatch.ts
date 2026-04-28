export interface AnimalBatchAnimal {
  id: string
  tag: string
}

export interface AnimalBatch {
  id: string
  name: string
  farmId: string
  animals: AnimalBatchAnimal[]
}

export interface AnimalBatchFormData {
  name: string
  animalIds: string[]
}

export interface AnimalBatchListFilters {
  search: string
}

export interface AnimalBatchApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
