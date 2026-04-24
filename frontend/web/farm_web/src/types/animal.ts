export type AnimalStatus = 'ACTIVE' | 'INACTIVE' | 'SOLD' | 'DEAD'
export type AnimalOrigin = 'PURCHASED' | 'BORN'

export interface Animal {
  id: string
  tag: string
  breed: string
  birthDate: string
  status: AnimalStatus
  origin: AnimalOrigin
  acquisitionCost: number | null
  salePrice: number | null
  saleDate: string | null
  farmId: string
}

export interface AnimalFormData {
  tag: string
  breed: string
  birthDate: string
  origin: AnimalOrigin
  acquisitionCost: number | null
  status?: AnimalStatus
  farmId: string
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}

export interface SellAnimalData {
  salePrice: number
  saleDate?: string
}

export interface AnimalListFilters {
  search: string
  status: '' | AnimalStatus
  origin: '' | AnimalOrigin
}
