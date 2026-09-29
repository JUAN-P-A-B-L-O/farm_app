import { useEffect } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { useTranslation } from '../../hooks/useTranslation'
import {
  getCurrentPlanMetadata,
  getFeatureAccessState,
  getPlanMetadata,
  type AppFeature,
} from '../../utils/planAccess'

interface PlanUpgradeModalProps {
  feature: AppFeature
  onClose: () => void
  onUpgrade: () => void
}

function PlanUpgradeModal({ feature, onClose, onUpgrade }: PlanUpgradeModalProps) {
  const { user } = useAuth()
  const { t } = useTranslation()
  const accessState = getFeatureAccessState(user, feature)
  const currentPlanMetadata = getCurrentPlanMetadata(user)
  const requiredPlanMetadata = getPlanMetadata(accessState.minimumPlan)

  useEffect(() => {
    function handleEscape(event: KeyboardEvent) {
      if (event.key === 'Escape') {
        onClose()
      }
    }

    window.addEventListener('keydown', handleEscape)

    return () => {
      window.removeEventListener('keydown', handleEscape)
    }
  }, [onClose])

  return (
    <div className="plan-upgrade-modal" role="dialog" aria-modal="true" aria-labelledby="plan-upgrade-modal-title">
      <button
        type="button"
        className="plan-upgrade-modal__backdrop"
        aria-label={t('plan.modal.close')}
        onClick={onClose}
      />

      <section className="plan-upgrade-modal__panel">
        <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
        <p className="plan-upgrade-modal__eyebrow">{t('plan.modal.eyebrow')}</p>
        <h2 id="plan-upgrade-modal-title">{t('plan.modal.title')}</h2>
        <p>{t(accessState.metadata.descriptionKey)}</p>
        <p>{t('plan.modal.description')}</p>

        <dl className="plan-upgrade-modal__details">
          <div>
            <dt>{t('plan.modal.currentPlanLabel')}</dt>
            <dd>{t(currentPlanMetadata.labelKey)}</dd>
          </div>
          <div>
            <dt>{t('plan.modal.requiredPlanLabel')}</dt>
            <dd>{t(requiredPlanMetadata.labelKey)}</dd>
          </div>
        </dl>

        <div className="plan-upgrade-modal__actions">
          <button
            type="button"
            className="animals-table__action-button animals-table__action-button--secondary"
            onClick={onClose}
          >
            {t('plan.modal.dismiss')}
          </button>
          <button type="button" className="animals-table__action-button" onClick={onUpgrade}>
            {t('plan.modal.cta')}
          </button>
        </div>
      </section>
    </div>
  )
}

export default PlanUpgradeModal
