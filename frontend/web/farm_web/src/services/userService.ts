import api from './api'
import type { User, UserFormData } from '../types/user'

export async function getAllUsers(): Promise<User[]> {
  const response = await api.get<User[]>('/users')

  return response.data
}

export async function createUser(data: UserFormData): Promise<User> {
  const payload = {
    name: data.name,
    email: data.email,
    role: data.role,
    password: data.active ? data.password : undefined,
    active: data.active,
    farmIds: data.farmIds,
  }

  const response = await api.post<User>('/users', payload)

  return response.data
}
