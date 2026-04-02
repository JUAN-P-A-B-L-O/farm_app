export interface FeedType {
  id: string
  name: string
  costPerKg: number
}

export interface FeedTypeFormData {
  name: string
  costPerKg: number
}

export interface FeedTypeApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
