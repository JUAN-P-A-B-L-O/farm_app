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
  }

  console.log('User payload:', payload)

  const response = await api.post<User>('/users', payload)

  return response.data
}

export async function updateUser(id: string, data: UserFormData): Promise<User> {
  const payload = {
    name: data.name,
    email: data.email,
    role: data.role,
  }

  console.log('User payload:', payload)

  const response = await api.put<User>(`/users/${id}`, payload)

  return response.data
}

export async function deleteUser(id: string): Promise<void> {
  await api.delete(`/users/${id}`)
}
