import api from './api'
import type { Animal, AnimalFormData } from '../types/animal'

export async function getAllAnimals(): Promise<Animal[]> {
  const response = await api.get<Animal[]>('/animals')

  return response.data
}

export async function getAnimalById(id: string): Promise<Animal> {
  const response = await api.get<Animal>(`/animals/${id}`)

  return response.data
}

export async function createAnimal(data: AnimalFormData): Promise<Animal> {
  const response = await api.post<Animal>('/animals', data)

  return response.data
}

export async function updateAnimal(id: string, data: AnimalFormData): Promise<Animal> {
  const response = await api.put<Animal>(`/animals/${id}`, data)

  return response.data
}

export async function deleteAnimal(id: string): Promise<void> {
  await api.delete(`/animals/${id}`)
}
