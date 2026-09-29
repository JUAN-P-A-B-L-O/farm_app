import { createContext } from 'react'
import type { AppFeature } from '../utils/planAccess'

export interface PlanUpgradeContextValue {
  activeFeature: AppFeature | null
  closeUpgradePrompt: () => void
  navigateToPlans: () => void
  openUpgradePrompt: (feature: AppFeature) => void
}

export const PlanUpgradeContext = createContext<PlanUpgradeContextValue | undefined>(undefined)
