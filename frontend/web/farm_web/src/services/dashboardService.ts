import api from './api'
import type { DashboardSummary } from '../types/dashboard'

export async function fetchDashboard() {
  const response = await api.get<DashboardSummary>('/dashboard')

  console.log(response)

  return response.data
}
