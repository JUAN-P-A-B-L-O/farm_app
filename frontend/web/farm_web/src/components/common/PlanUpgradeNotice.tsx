import { useAuth } from '../../hooks/useAuth'
import { usePlanUpgrade } from '../../hooks/usePlanUpgrade'
import { useTranslation } from '../../hooks/useTranslation'
import {
  getCurrentPlanMetadata,
  getFeatureAccessState,
  type AppFeature,
} from '../../utils/planAccess'

interface PlanUpgradeNoticeProps {
  feature: AppFeature
}

function PlanUpgradeNotice({ feature }: PlanUpgradeNoticeProps) {
  const { user } = useAuth()
  const { navigateToPlans } = usePlanUpgrade()
  const { t } = useTranslation()
  const accessState = getFeatureAccessState(user, feature)
  const currentPlanMetadata = getCurrentPlanMetadata(user)

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('plan.eyebrow')}</p>
        <h1>{t('plan.title')}</h1>
        <p className="animals-page__description">{t('plan.description')}</p>
      </section>

      <section className="animals-panel plan-upgrade-notice">
        <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
        <h2>{t(accessState.metadata.titleKey)}</h2>
        <p>{t(accessState.metadata.descriptionKey)}</p>
        <p>{t('plan.notice.currentPlan', { plan: t(currentPlanMetadata.labelKey) })}</p>
        <p>{t('plan.upgradeHint')}</p>
        <div className="plan-upgrade-notice__actions">
          <button type="button" className="animals-table__action-button" onClick={navigateToPlans}>
            {t('plan.modal.cta')}
          </button>
        </div>
      </section>
    </main>
  )
}

export default PlanUpgradeNotice
