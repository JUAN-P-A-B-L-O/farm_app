export interface MilkPrice {
  id: string | null
  farmId: string
  price: number
  effectiveDate: string | null
  createdAt: string | null
  createdBy: string | null
  fallbackDefault: boolean
}

export interface CreateMilkPricePayload {
  price: number
  effectiveDate: string
}

export interface MilkPriceApiErrorResponse {
  error?: string
}

export interface MilkPriceListFilters {
  search: string
  effectiveDate: string
}
