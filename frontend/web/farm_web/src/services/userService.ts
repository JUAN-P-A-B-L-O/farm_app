import api from './api'
import type { User } from '../types/user'

export async function getAllUsers(): Promise<User[]> {
  const response = await api.get<User[]>('/users')

  return response.data
}
