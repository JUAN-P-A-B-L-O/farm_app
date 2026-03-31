import api from './api'
import type { DashboardSummary } from '../types/dashboard'

export async function fetchDashboard(): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/dashboard')

  return response.data
}
