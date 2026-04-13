const SELECTED_FARM_STORAGE_KEY = 'farm-web-selected-farm'

export function getStoredFarmId(): string | null {
  return window.localStorage.getItem(SELECTED_FARM_STORAGE_KEY)
}

export function persistFarmId(farmId: string) {
  window.localStorage.setItem(SELECTED_FARM_STORAGE_KEY, farmId)
}

export function clearStoredFarmId() {
  window.localStorage.removeItem(SELECTED_FARM_STORAGE_KEY)
}
