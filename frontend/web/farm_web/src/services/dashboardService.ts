import api from './api'
import type { DashboardSummary } from '../types/dashboard'

export async function fetchDashboard(farmId?: string): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/dashboard', {
    params: farmId ? { farmId } : undefined,
  })

  return response.data
}
