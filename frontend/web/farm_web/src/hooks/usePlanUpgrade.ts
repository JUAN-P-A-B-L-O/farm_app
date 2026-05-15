import { useContext } from 'react'
import { PlanUpgradeContext } from '../context/planUpgradeContext'

export function usePlanUpgrade() {
  const context = useContext(PlanUpgradeContext)

  if (!context) {
    throw new Error('usePlanUpgrade must be used within a PlanUpgradeProvider')
  }

  return context
}
