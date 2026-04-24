import api from './api'
import { downloadCsv } from './csvExportService'
import type { CurrencyCode } from '../context/CurrencyContext'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type { CreateMilkPricePayload, MilkPrice } from '../types/milkPrice'

function buildFarmParams(farmId?: string, currency?: CurrencyCode) {
  return {
    ...(farmId ? { farmId } : {}),
    ...(currency ? { currency } : {}),
  }
}

export async function createMilkPrice(data: CreateMilkPricePayload, farmId?: string): Promise<MilkPrice> {
  const response = await api.post<MilkPrice>('/milk-prices', {
    ...data,
    price: normalizeToTwoDecimals(data.price),
  }, {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getCurrentMilkPrice(farmId?: string): Promise<MilkPrice> {
  const response = await api.get<MilkPrice>('/milk-prices/current', {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function getMilkPriceHistory(farmId?: string): Promise<MilkPrice[]> {
  const response = await api.get<MilkPrice[]>('/milk-prices', {
    params: buildFarmParams(farmId),
  })

  return response.data
}

export async function exportMilkPriceHistoryCsv(farmId?: string, currency?: CurrencyCode): Promise<void> {
  await downloadCsv('/milk-prices/export', buildFarmParams(farmId, currency), 'milk-prices.csv')
}
