import api from './api'
import { downloadCsv } from './csvExportService'
import type { User, UserFormData, UserListFilters } from '../types/user'

function buildUserListParams(filters?: UserListFilters) {
  return {
    ...(filters?.search ? { search: filters.search } : {}),
    ...(filters?.active ? { active: filters.active } : {}),
    ...(filters?.role ? { role: filters.role } : {}),
  }
}

export async function getAllUsers(filters?: UserListFilters): Promise<User[]> {
  const response = await api.get<User[]>('/users', {
    params: buildUserListParams(filters),
  })

  return response.data
}

export async function createUser(data: UserFormData): Promise<User> {
  const payload = {
    name: data.name,
    email: data.email,
    role: data.role,
    password: data.active ? data.password : undefined,
    active: data.active,
    avatarUrl: data.avatarUrl || undefined,
    farmIds: data.farmIds,
  }

  const response = await api.post<User>('/users', payload)

  return response.data
}

export async function updateUser(id: string, data: UserFormData): Promise<User> {
  const payload = {
    name: data.name,
    email: data.email,
    role: data.role,
    avatarUrl: data.avatarUrl || undefined,
    farmIds: data.farmIds,
  }

  const response = await api.put<User>(`/users/${id}`, payload)

  return response.data
}

export async function inactivateUser(id: string): Promise<User> {
  const response = await api.patch<User>(`/users/${id}/inactivate`)

  return response.data
}

export async function activateUser(id: string, password?: string): Promise<User> {
  const response = await api.patch<User>(`/users/${id}/activate`, {
    password: password || undefined,
  })

  return response.data
}

export async function deleteUser(id: string): Promise<void> {
  await api.delete(`/users/${id}`)
}

export async function updateOwnPassword(currentPassword: string, newPassword: string): Promise<void> {
  await api.put('/users/me/password', {
    currentPassword,
    newPassword,
  })
}

export async function exportUsersCsv(filters?: UserListFilters): Promise<void> {
  await downloadCsv('/users/export', buildUserListParams(filters), 'users.csv')
}
