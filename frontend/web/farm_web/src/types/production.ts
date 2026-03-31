export interface ProductionAnimalOption {
  id: string
  tag: string
}

export interface Production {
  id: string
  animalId: string
  date: string
  quantity: number
  animal: ProductionAnimalOption
}

export interface ProductionFormData {
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
