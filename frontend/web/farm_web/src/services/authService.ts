import api from './api'
import type { User } from '../types/user'

export interface LoginResponse {
  accessToken: string
  user: User
}

export interface LoginCredentials {
  email: string
  password: string
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/auth/login', {
    email,
    password,
  })

  return response.data
}
