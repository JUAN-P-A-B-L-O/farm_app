
const planMetadata = {
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

const featureMetadata = {
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

export const availablePlans = ['FREE', 'PRO']
export const appFeatures = ['DASHBOARD', 'ANALYTICS', 'CSV_EXPORT']

export function resolvePlan(user) {
  return user?.plan ?? 'FREE'
}

export function getPlanMetadata(plan) {
  return planMetadata[plan]
}

export function getCurrentPlanMetadata(user) {
  return getPlanMetadata(resolvePlan(user))
}

export function getFeatureAccessState(user, feature) {
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

export function hasFeatureAccess(user, feature) {
  return getFeatureAccessState(user, feature).allowed
}

export function getFeatureMetadata(feature) {
  return featureMetadata[feature]
}

export function getPlanFeatureSummaries(plan) {
  const currentPlan = planMetadata[plan] ? plan : resolvePlan(null)

  return appFeatures.map((feature) => {
    const metadata = getFeatureMetadata(feature)

    return {
      feature,
      included: planMetadata[currentPlan].rank >= planMetadata[metadata.minimumPlan].rank,
      metadata,
    }
  })
}
