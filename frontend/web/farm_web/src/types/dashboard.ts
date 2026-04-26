import type { AnimalStatus } from './animal'

export interface DashboardSummary {
  totalProduction: number
  totalFeedingCost: number
  totalRevenue: number
  totalProfit: number
  animalCount: number
}

export interface DashboardFilters {
  startDate: string
  endDate: string
  animalId: string
  status: '' | AnimalStatus
}
