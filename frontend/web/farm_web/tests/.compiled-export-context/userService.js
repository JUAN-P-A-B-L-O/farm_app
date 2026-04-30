import api from './api.js'
import { downloadCsv } from './csvExportService.js'

function buildUserListParams(filters) {
  return {
    ...(filters?.search ? { search: filters.search } : {}),
    ...(filters?.active ? { active: filters.active } : {}),
    ...(filters?.role ? { role: filters.role } : {}),
  }
}

export async function getAllUsers(filters) {
  const response = await api.get('/users', {
    params: buildUserListParams(filters),
  })

  return response.data
}

export async function getUsersPage(filters, pagination) {
  const response = await api.get('/users', {
    params: {
      ...buildUserListParams(filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function createUser(data) {
  const payload = {
    name: data.name,
    email: data.email,
    role: data.role,
    password: data.active ? data.password : undefined,
    active: data.active,
    avatarUrl: data.avatarUrl || undefined,
    farmIds: data.farmIds,
  }

  const response = await api.post('/users', payload)

  return response.data
}

export async function updateUser(id, data) {
  const payload = {
    name: data.name,
    email: data.email,
    role: data.role,
    avatarUrl: data.avatarUrl || undefined,
    farmIds: data.farmIds,
  }

  const response = await api.put(`/users/${id}`, payload)

  return response.data
}

export async function inactivateUser(id) {
  const response = await api.patch(`/users/${id}/inactivate`)

  return response.data
}

export async function activateUser(id, password) {
  const response = await api.patch(`/users/${id}/activate`, {
    password: password || undefined,
  })

  return response.data
}

export async function deleteUser(id) {
  await api.delete(`/users/${id}`)
}

export async function updateOwnPassword(currentPassword, newPassword) {
  await api.put('/users/me/password', {
    currentPassword,
    newPassword,
  })
}

export async function exportUsersCsv(filters) {
  await downloadCsv('/users/export', buildUserListParams(filters), 'users.csv')
}
