import type { ReactElement } from 'react'
import { useAuth } from '../../hooks/useAuth'
import PlanUpgradeNotice from '../common/PlanUpgradeNotice'
import { hasFeatureAccess, type AppFeature } from '../../utils/planAccess'

interface PlanRouteProps {
  children: ReactElement
  feature: AppFeature
}

function PlanRoute({ children, feature }: PlanRouteProps) {
  const { user } = useAuth()

  if (!hasFeatureAccess(user, feature)) {
    return <PlanUpgradeNotice feature={feature} />
  }

  return children
}

export default PlanRoute
