export interface User {
  id: string
  name: string
  email: string
  role: string
  active: boolean
  avatarUrl?: string | null
  farmIds: string[]
}

export interface UserFormData {
  name: string
  email: string
  role: string
  password: string
  active: boolean
  avatarUrl: string
  farmIds: string[]
}

export interface UserListFilters {
  search: string
  active: '' | 'true' | 'false'
  role: string
}

export interface UserApiErrorResponse {
  timestamp: string
  status: number
  error: string
  path: string
}
