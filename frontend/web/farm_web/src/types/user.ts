export interface User {
  id: string
  name: string
  email: string
  role: string
}

export interface UserFormData {
  name: string
  email: string
  role: string
  password: string
  active: boolean
  farmIds: string[]
}

export interface UserApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
