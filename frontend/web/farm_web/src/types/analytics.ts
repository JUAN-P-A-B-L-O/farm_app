export type AnalyticsGroupBy = 'day' | 'month'

export interface AnalyticsFilters {
  startDate: string
  endDate: string
  animalId: string
  groupBy: AnalyticsGroupBy
  includeAcquisitionCost: boolean
}

export interface AnalyticsSeriesApiPoint {
  period: string
  value: number
}

export interface AnalyticsProfitApiPoint {
  period: string
  production: number
  feedingCost: number
  revenue: number
  profit: number
}

export interface AnalyticsProductionByAnimalApiPoint {
  animalId: string
  animalTag: string
  quantity: number
}

export interface AnalyticsLineChartPoint {
  label: string
  value: number
}

export interface AnalyticsBarChartPoint {
  label: string
  value: number
}

export interface AnalyticsDataset {
  productionSeries: AnalyticsLineChartPoint[]
  feedingCostSeries: AnalyticsLineChartPoint[]
  profitSeries: AnalyticsLineChartPoint[]
  productionByAnimal: AnalyticsBarChartPoint[]
}
