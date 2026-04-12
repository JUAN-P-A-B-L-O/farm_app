import { createContext } from 'react'
import type { Farm } from '../types/farm'

export interface FarmContextValue {
  farms: Farm[]
  selectedFarmId: string
  selectedFarm: Farm | null
  isLoading: boolean
  errorMessage: string
  setSelectedFarmId: (farmId: string) => void
  refreshFarms: () => Promise<void>
}

export const FarmContext = createContext<FarmContextValue | undefined>(undefined)
