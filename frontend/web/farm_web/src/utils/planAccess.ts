import type { User, UserPlan } from '../types/user'

export type AppFeature = 'DASHBOARD' | 'ANALYTICS' | 'CSV_EXPORT'

interface FeatureMetadata {
  descriptionKey: string
  minimumPlan: UserPlan
  titleKey: string
}

const planRanks: Record<UserPlan, number> = {
  FREE: 0,
  PRO: 1,
}

const featureMetadata: Record<AppFeature, FeatureMetadata> = {
  DASHBOARD: {
    minimumPlan: 'PRO',
    titleKey: 'plan.features.dashboard.title',
    descriptionKey: 'plan.features.dashboard.description',
  },
  ANALYTICS: {
    minimumPlan: 'PRO',
    titleKey: 'plan.features.analytics.title',
    descriptionKey: 'plan.features.analytics.description',
  },
  CSV_EXPORT: {
    minimumPlan: 'PRO',
    titleKey: 'plan.features.csvExport.title',
    descriptionKey: 'plan.features.csvExport.description',
  },
}

function resolvePlan(user: Pick<User, 'plan'> | null | undefined): UserPlan {
  return user?.plan ?? 'FREE'
}

export function hasFeatureAccess(user: Pick<User, 'plan'> | null | undefined, feature: AppFeature) {
  const metadata = featureMetadata[feature]
  return planRanks[resolvePlan(user)] >= planRanks[metadata.minimumPlan]
}

export function getFeatureMetadata(feature: AppFeature) {
  return featureMetadata[feature]
}
