import api from './api'
import type {
  AnalyticsBarChartPoint,
  AnalyticsDataset,
  AnalyticsFilters,
  AnalyticsLineChartPoint,
  AnalyticsProductionByAnimalApiPoint,
  AnalyticsProfitApiPoint,
  AnalyticsSeriesApiPoint,
} from '../types/analytics'

function isValidNumber(value: unknown): value is number {
  return typeof value === 'number' && Number.isFinite(value)
}

function isValidLabel(value: unknown): value is string {
  return typeof value === 'string' && value.trim().length > 0
}

function buildAnalyticsParams(filters: AnalyticsFilters) {
  return {
    ...(filters.startDate ? { startDate: filters.startDate } : {}),
    ...(filters.endDate ? { endDate: filters.endDate } : {}),
    ...(filters.animalId ? { animalId: filters.animalId } : {}),
    groupBy: filters.groupBy,
  }
}

function mapSeriesToLineChart(points: AnalyticsSeriesApiPoint[]): AnalyticsLineChartPoint[] {
  return Array.isArray(points)
    ? points
        .filter((point) => isValidLabel(point?.period) && isValidNumber(point?.value))
        .map((point) => ({
          label: point.period,
          value: point.value,
        }))
    : []
}

function mapProfitToLineChart(points: AnalyticsProfitApiPoint[]): AnalyticsLineChartPoint[] {
  return Array.isArray(points)
    ? points
        .filter((point) => isValidLabel(point?.period) && isValidNumber(point?.profit))
        .map((point) => ({
          label: point.period,
          value: point.profit,
        }))
    : []
}

function mapProductionByAnimal(points: AnalyticsProductionByAnimalApiPoint[]): AnalyticsBarChartPoint[] {
  return Array.isArray(points)
    ? points
        .filter((point) => isValidLabel(point?.animalTag) && isValidNumber(point?.quantity))
        .map((point) => ({
          label: point.animalTag,
          value: point.quantity,
        }))
    : []
}

export async function getAnalyticsDataset(filters: AnalyticsFilters): Promise<AnalyticsDataset> {
  const params = buildAnalyticsParams(filters)

  const [productionResponse, feedingResponse, profitResponse, productionByAnimalResponse] = await Promise.all([
    api.get<AnalyticsSeriesApiPoint[]>('/analytics/production', { params }),
    api.get<AnalyticsSeriesApiPoint[]>('/analytics/feeding', { params }),
    api.get<AnalyticsProfitApiPoint[]>('/analytics/profit', { params }),
    api.get<AnalyticsProductionByAnimalApiPoint[]>('/analytics/production/by-animal', {
      params: {
        ...(filters.startDate ? { startDate: filters.startDate } : {}),
        ...(filters.endDate ? { endDate: filters.endDate } : {}),
        ...(filters.animalId ? { animalId: filters.animalId } : {}),
      },
    }),
  ])

  return {
    productionSeries: mapSeriesToLineChart(productionResponse.data),
    feedingCostSeries: mapSeriesToLineChart(feedingResponse.data),
    profitSeries: mapProfitToLineChart(profitResponse.data),
    productionByAnimal: mapProductionByAnimal(productionByAnimalResponse.data),
  }
}
