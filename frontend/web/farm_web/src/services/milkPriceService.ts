import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type { CreateMilkPricePayload, MilkPrice, MilkPriceListFilters } from '../types/milkPrice'
import type { PaginatedResponse, PaginationParams } from '../types/pagination'

function buildMilkPriceListParams(farmId?: string, filters?: MilkPriceListFilters, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(filters?.search ? { search: filters.search } : {}),
    ...(filters?.effectiveDate ? { effectiveDate: filters.effectiveDate } : {}),
    ...(currency ? { currency } : {}),
  }
}

export async function createMilkPrice(data: CreateMilkPricePayload, farmId?: string): Promise<MilkPrice> {
  const response = await api.post<MilkPrice>('/milk-prices', {
    ...data,
    price: normalizeToTwoDecimals(data.price),
  }, {
    params: buildMilkPriceListParams(farmId),
  })

  return response.data
}

export async function getCurrentMilkPrice(farmId?: string): Promise<MilkPrice> {
  const response = await api.get<MilkPrice>('/milk-prices/current', {
    params: buildMilkPriceListParams(farmId),
  })

  return response.data
}

export async function getMilkPriceHistory(farmId?: string, filters?: MilkPriceListFilters): Promise<MilkPrice[]> {
  const response = await api.get<MilkPrice[]>('/milk-prices', {
    params: buildMilkPriceListParams(farmId, filters),
  })

  return response.data
}

export async function getMilkPriceHistoryPage(
  farmId: string | undefined,
  pagination: PaginationParams,
  filters?: MilkPriceListFilters,
): Promise<PaginatedResponse<MilkPrice>> {
  const response = await api.get<PaginatedResponse<MilkPrice>>('/milk-prices', {
    params: {
      ...buildMilkPriceListParams(farmId, filters),
      page: pagination.page,
      size: pagination.size,
    },
  })

  return response.data
}

export async function exportMilkPriceHistoryCsv(
  farmId?: string,
  currency?: CurrencyCode,
  filters?: MilkPriceListFilters,
): Promise<void> {
  await downloadCsv('/milk-prices/export', buildMilkPriceListParams(farmId, filters, currency), 'milk-prices.csv')
}
