import { useCallback, useMemo, useState, type ReactNode } from 'react'
import { useLocation, useNavigate } from 'react-router-dom'
import PlanUpgradeModal from '../components/common/PlanUpgradeModal'
import { PlanUpgradeContext } from './planUpgradeContext'
import type { AppFeature } from '../utils/planAccess'

export function PlanUpgradeProvider({ children }: { children: ReactNode }) {
  const navigate = useNavigate()
  const location = useLocation()
  const [activeFeature, setActiveFeature] = useState<AppFeature | null>(null)

  const closeUpgradePrompt = useCallback(() => {
    setActiveFeature(null)
  }, [])

  const openUpgradePrompt = useCallback((feature: AppFeature) => {
    setActiveFeature(feature)
  }, [])

  const navigateToPlans = useCallback(() => {
    setActiveFeature(null)
    navigate('/plans', {
      state: {
        feature: activeFeature,
        from: location.pathname,
      },
    })
  }, [activeFeature, location.pathname, navigate])

  const value = useMemo(
    () => ({
      activeFeature,
      closeUpgradePrompt,
      navigateToPlans,
      openUpgradePrompt,
    }),
    [activeFeature, closeUpgradePrompt, navigateToPlans, openUpgradePrompt],
  )

  return (
    <PlanUpgradeContext.Provider value={value}>
      {children}
      {activeFeature && (
        <PlanUpgradeModal
          feature={activeFeature}
          onClose={closeUpgradePrompt}
          onUpgrade={navigateToPlans}
        />
      )}
    </PlanUpgradeContext.Provider>
  )
}
