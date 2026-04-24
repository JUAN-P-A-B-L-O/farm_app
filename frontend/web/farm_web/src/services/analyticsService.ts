import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
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

function buildAnalyticsParams(filters: AnalyticsFilters, farmId?: string, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters.startDate ? { startDate: filters.startDate } : {}),
    ...(filters.endDate ? { endDate: filters.endDate } : {}),
    ...(filters.animalId ? { animalId: filters.animalId } : {}),
    groupBy: filters.groupBy,
    includeAcquisitionCost: filters.includeAcquisitionCost,
    ...(currency ? { currency } : {}),
  }
}

function buildProductionByAnimalParams(filters: AnalyticsFilters, farmId?: string, currency?: CurrencyCode) {
  return {
    ...(filters.startDate ? { startDate: filters.startDate } : {}),
    ...(filters.endDate ? { endDate: filters.endDate } : {}),
    ...(filters.animalId ? { animalId: filters.animalId } : {}),
    ...(farmId ? { farmId } : {}),
    ...(currency ? { currency } : {}),
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

export async function getAnalyticsDataset(
  filters: AnalyticsFilters,
  farmId?: string,
  currency?: CurrencyCode,
): Promise<AnalyticsDataset> {
  const params = buildAnalyticsParams(filters, farmId, currency)

  const [productionResponse, feedingResponse, profitResponse, productionByAnimalResponse] = await Promise.all([
    api.get<AnalyticsSeriesApiPoint[]>('/analytics/production', { params }),
    api.get<AnalyticsSeriesApiPoint[]>('/analytics/feeding', { params }),
    api.get<AnalyticsProfitApiPoint[]>('/analytics/profit', { params }),
    api.get<AnalyticsProductionByAnimalApiPoint[]>('/analytics/production/by-animal', {
      params: buildProductionByAnimalParams(filters, farmId, currency),
    }),
  ])

  return {
    productionSeries: mapSeriesToLineChart(productionResponse.data),
    feedingCostSeries: mapSeriesToLineChart(feedingResponse.data),
    profitSeries: mapProfitToLineChart(profitResponse.data),
    productionByAnimal: mapProductionByAnimal(productionByAnimalResponse.data),
  }
}

export async function exportAnalyticsProductionCsv(
  filters: AnalyticsFilters,
  farmId?: string,
  currency?: CurrencyCode,
): Promise<void> {
  await downloadCsv(
    '/analytics/production/export',
    buildAnalyticsParams(filters, farmId, currency),
    'analytics-production.csv',
  )
}

export async function exportAnalyticsFeedingCsv(
  filters: AnalyticsFilters,
  farmId?: string,
  currency?: CurrencyCode,
): Promise<void> {
  await downloadCsv('/analytics/feeding/export', buildAnalyticsParams(filters, farmId, currency), 'analytics-feeding.csv')
}

export async function exportAnalyticsProfitCsv(
  filters: AnalyticsFilters,
  farmId?: string,
  currency?: CurrencyCode,
): Promise<void> {
  await downloadCsv('/analytics/profit/export', buildAnalyticsParams(filters, farmId, currency), 'analytics-profit.csv')
}

export async function exportAnalyticsProductionByAnimalCsv(
  filters: AnalyticsFilters,
  farmId?: string,
  currency?: CurrencyCode,
): Promise<void> {
  await downloadCsv(
    '/analytics/production/by-animal/export',
    buildProductionByAnimalParams(filters, farmId, currency),
    'analytics-production-by-animal.csv',
  )
}
