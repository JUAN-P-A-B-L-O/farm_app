import api from './api'

export interface BillingSessionResponse {
  url: string
}

export async function createCheckoutSession(): Promise<BillingSessionResponse> {
  const response = await api.post<BillingSessionResponse>('/billing/checkout-session')
  return response.data
}

export async function createPortalSession(): Promise<BillingSessionResponse> {
  const response = await api.post<BillingSessionResponse>('/billing/portal-session')
  return response.data
}
