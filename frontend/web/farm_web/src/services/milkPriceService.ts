import api from './api'
import { downloadCsv } from './csvExportService'
import { normalizeToTwoDecimals } from '../utils/decimal'
import type { CreateMilkPricePayload, MilkPrice } from '../types/milkPrice'

function buildFarmParams(farmId?: string) {
  return farmId ? { farmId } : {}
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

export async function exportMilkPriceHistoryCsv(farmId?: string): Promise<void> {
  await downloadCsv('/milk-prices/export', buildFarmParams(farmId), 'milk-prices.csv')
}
