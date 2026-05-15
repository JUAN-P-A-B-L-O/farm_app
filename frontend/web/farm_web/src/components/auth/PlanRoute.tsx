import { useEffect, type ReactElement } from 'react'
import { usePlanUpgrade } from '../../hooks/usePlanUpgrade'
import { useAuth } from '../../hooks/useAuth'
import PlanUpgradeNotice from '../common/PlanUpgradeNotice'
import { getFeatureAccessState, type AppFeature } from '../../utils/planAccess'

interface PlanRouteProps {
  children: ReactElement
  feature: AppFeature
}

function PlanRoute({ children, feature }: PlanRouteProps) {
  const { user } = useAuth()
  const { openUpgradePrompt } = usePlanUpgrade()
  const accessState = getFeatureAccessState(user, feature)

  useEffect(() => {
    if (!accessState.allowed) {
      openUpgradePrompt(feature)
    }
  }, [accessState.allowed, feature, openUpgradePrompt])

  if (!accessState.allowed) {
    return <PlanUpgradeNotice feature={feature} />
  }

  return children
}

export default PlanRoute
