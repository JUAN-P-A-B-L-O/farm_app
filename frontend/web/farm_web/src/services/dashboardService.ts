import api from './api'
import type { DashboardSummary } from '../types/dashboard'

export async function fetchDashboard(farmId?: string, includeAcquisitionCost = true): Promise<DashboardSummary> {
  const response = await api.get<DashboardSummary>('/dashboard', {
    params: {
      ...(farmId ? { farmId } : {}),
      includeAcquisitionCost,
    },
  })

  return response.data
}
