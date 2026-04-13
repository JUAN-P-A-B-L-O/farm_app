export interface Farm {
  id: string
  name: string
}

export interface FarmFormData {
  name: string
}

export interface FarmApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
