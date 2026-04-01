export interface User {
  id: string
  name: string
}

export interface UserFormData {
  name: string
}

export interface UserApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
