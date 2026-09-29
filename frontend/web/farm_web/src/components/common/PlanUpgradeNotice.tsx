import { useTranslation } from '../../hooks/useTranslation'
import { getFeatureMetadata, type AppFeature } from '../../utils/planAccess'

interface PlanUpgradeNoticeProps {
  feature: AppFeature
}

function PlanUpgradeNotice({ feature }: PlanUpgradeNoticeProps) {
  const { t } = useTranslation()
  const metadata = getFeatureMetadata(feature)

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('plan.eyebrow')}</p>
        <h1>{t('plan.title')}</h1>
        <p className="animals-page__description">{t('plan.description')}</p>
      </section>

      <section className="animals-panel plan-upgrade-notice">
        <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
        <h2>{t(metadata.titleKey)}</h2>
        <p>{t(metadata.descriptionKey)}</p>
        <p>{t('plan.upgradeHint')}</p>
      </section>
    </main>
  )
}

export default PlanUpgradeNotice
