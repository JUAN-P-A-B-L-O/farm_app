import type { User, UserPlan } from '../types/user'

export type AppFeature = 'DASHBOARD' | 'ANALYTICS' | 'CSV_EXPORT'

interface FeatureMetadata {
  descriptionKey: string
  minimumPlan: UserPlan
  titleKey: string
}

interface PlanMetadata {
  labelKey: string
  paid: boolean
  rank: number
}

export interface FeatureAccessState {
  allowed: boolean
  currentPlan: UserPlan
  feature: AppFeature
  metadata: FeatureMetadata
  minimumPlan: UserPlan
}

const planMetadata: Record<UserPlan, PlanMetadata> = {
  FREE: {
    labelKey: 'plan.labels.FREE',
    paid: false,
    rank: 0,
  },
  PRO: {
    labelKey: 'plan.labels.PRO',
    paid: true,
    rank: 1,
  },
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

export function resolvePlan(user: Pick<User, 'plan'> | null | undefined): UserPlan {
  return user?.plan ?? 'FREE'
}

export function getPlanMetadata(plan: UserPlan) {
  return planMetadata[plan]
}

export function getCurrentPlanMetadata(user: Pick<User, 'plan'> | null | undefined) {
  return getPlanMetadata(resolvePlan(user))
}

export function getFeatureAccessState(
  user: Pick<User, 'plan'> | null | undefined,
  feature: AppFeature,
): FeatureAccessState {
  const currentPlan = resolvePlan(user)
  const metadata = featureMetadata[feature]

  return {
    allowed: planMetadata[currentPlan].rank >= planMetadata[metadata.minimumPlan].rank,
    currentPlan,
    feature,
    metadata,
    minimumPlan: metadata.minimumPlan,
  }
}

export function hasFeatureAccess(user: Pick<User, 'plan'> | null | undefined, feature: AppFeature) {
  return getFeatureAccessState(user, feature).allowed
}

export function getFeatureMetadata(feature: AppFeature) {
  return featureMetadata[feature]
}
