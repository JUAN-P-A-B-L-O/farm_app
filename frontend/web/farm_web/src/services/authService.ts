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

export interface RegisterAccountRequest {
  name: string
  email: string
  password: string
}

export interface MessageResponse {
  message: string
}

export async function login(email: string, password: string): Promise<LoginResponse> {
  const response = await api.post<LoginResponse>('/auth/login', {
    email,
    password,
  })

  return response.data
}

export async function registerAccount(data: RegisterAccountRequest): Promise<User> {
  const response = await api.post<User>('/auth/register', data)
  return response.data
}

export async function confirmAccountEmail(token: string): Promise<MessageResponse> {
  const response = await api.get<MessageResponse>('/auth/confirm-email', {
    params: {
      token,
    },
  })

  return response.data
}

export async function resendConfirmationEmail(email: string): Promise<MessageResponse> {
  const response = await api.post<MessageResponse>('/auth/confirm-email/resend', {
    email,
  })

  return response.data
}
