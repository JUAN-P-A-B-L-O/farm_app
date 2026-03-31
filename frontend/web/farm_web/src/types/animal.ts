export type AnimalStatus = 'ACTIVE' | 'INACTIVE'

export interface Animal {
  id: string
  tag: string
  breed: string
  birthDate: string
  status: AnimalStatus
  farmId: string
}

export interface AnimalFormData {
  tag: string
  breed: string
  birthDate: string
  farmId: string
}

export interface ApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
