import { useEffect, useState } from 'react'
import { useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useTranslation } from '../../hooks/useTranslation'
import { createCheckoutSession, createPortalSession } from '../../services/billingService'
import {
  availablePlans,
  getCurrentPlanMetadata,
  getFeatureMetadata,
  getPlanFeatureSummaries,
  getPlanMetadata,
  type AppFeature,
} from '../../utils/planAccess'
import '../../App.css'

interface PlansPageLocationState {
  feature?: AppFeature
  from?: string
}

const upgradeEntrySectionId = 'plan-upgrade-entry'

function PlansPage() {
  const location = useLocation()
  const { user, refreshUser } = useAuth()
  const { t } = useTranslation()
  const currentPlanMetadata = getCurrentPlanMetadata(user)
  const state = (location.state ?? null) as PlansPageLocationState | null
  const highlightedFeatureMetadata = state?.feature ? getFeatureMetadata(state.feature) : null
  const searchParams = new URLSearchParams(location.search)
  const billingStatus = searchParams.get('billing')
  const [isStartingCheckout, setIsStartingCheckout] = useState(false)
  const [isOpeningPortal, setIsOpeningPortal] = useState(false)
  const [isRefreshingPlan, setIsRefreshingPlan] = useState(false)

  useEffect(() => {
    if (billingStatus !== 'success') {
      return
    }

    let isMounted = true
    setIsRefreshingPlan(true)

    refreshUser()
      .catch(() => undefined)
      .finally(() => {
        if (isMounted) {
          setIsRefreshingPlan(false)
        }
      })

    return () => {
      isMounted = false
    }
  }, [billingStatus, refreshUser])

  function scrollToUpgradeEntry() {
    document.getElementById(upgradeEntrySectionId)?.scrollIntoView({ behavior: 'smooth', block: 'start' })
  }

  async function handleUpgradeClick() {
    setIsStartingCheckout(true)

    try {
      const response = await createCheckoutSession()
      window.location.assign(response.url)
    } finally {
      setIsStartingCheckout(false)
    }
  }

  async function handlePortalClick() {
    setIsOpeningPortal(true)

    try {
      const response = await createPortalSession()
      window.location.assign(response.url)
    } finally {
      setIsOpeningPortal(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('plan.page.eyebrow')}</p>
        <h1>{t('plan.page.title')}</h1>
        <p className="animals-page__description">{t('plan.page.description')}</p>
      </section>

      {highlightedFeatureMetadata && (
        <section className="animals-panel plan-page__highlight">
          <span className="plan-upgrade-notice__badge">{t('plan.badge')}</span>
          <h2>{t(highlightedFeatureMetadata.titleKey)}</h2>
          <p>{t(highlightedFeatureMetadata.descriptionKey)}</p>
          <p>{t('plan.page.highlightHint')}</p>
          <div className="plan-upgrade-notice__actions">
            <button type="button" className="animals-table__action-button" onClick={scrollToUpgradeEntry}>
              {t('plan.page.upgradeCta')}
            </button>
          </div>
        </section>
      )}

      <section className="plan-page__grid">
        {availablePlans.map((plan) => {
          const planMetadata = getPlanMetadata(plan)
          const isCurrentPlan = currentPlanMetadata === planMetadata

          return (
            <article
              key={plan}
              className={`animals-panel plan-page__card${planMetadata.paid ? ' plan-page__card--premium' : ''}`}
            >
              <div className="plan-page__card-header">
                <div>
                  <p className="plan-page__price">{t(`plan.page.pricing.${plan}`)}</p>
                  <h2>{t(planMetadata.labelKey)}</h2>
                </div>
                {isCurrentPlan && (
                  <span className="plan-page__current-badge">{t('plan.page.currentBadge')}</span>
                )}
              </div>

              <p>{t(`plan.page.summaries.${plan}`)}</p>

              {planMetadata.paid && !isCurrentPlan && (
                <div className="plan-page__card-actions">
                  <button type="button" className="animals-table__action-button" onClick={scrollToUpgradeEntry}>
                    {t('plan.page.upgradeCta')}
                  </button>
                </div>
              )}

              <ul className="plan-page__feature-list">
                {getPlanFeatureSummaries(plan).map((featureSummary) => (
                  <li key={featureSummary.feature} className="plan-page__feature-item">
                    <span
                      className={`plan-page__feature-status${featureSummary.included ? ' plan-page__feature-status--included' : ''}`}
                    >
                      {featureSummary.included
                        ? t('plan.page.featureIncluded')
                        : t('plan.page.featureUpgradeRequired')}
                    </span>
                    <div>
                      <strong>{t(featureSummary.metadata.titleKey)}</strong>
                      <p>{t(featureSummary.metadata.descriptionKey)}</p>
                    </div>
                  </li>
                ))}
              </ul>
            </article>
          )
        })}
      </section>

      <section id={upgradeEntrySectionId} className="animals-panel plan-page__checkout-placeholder">
        <span className="plan-upgrade-notice__badge">
          {user?.plan === 'PRO' ? t('plan.page.portalBadge') : t('plan.page.checkoutBadge')}
        </span>
        <p className="plan-upgrade-modal__eyebrow">{t('plan.page.checkoutEyebrow')}</p>
        <h2>{user?.plan === 'PRO' ? t('plan.page.portalTitle') : t('plan.page.checkoutTitle')}</h2>
        <p>{user?.plan === 'PRO' ? t('plan.page.portalDescription') : t('plan.page.checkoutDescription')}</p>
        <p>{user?.plan === 'PRO' ? t('plan.page.portalHint') : t('plan.page.checkoutHint')}</p>
        {billingStatus === 'success' && (
          <p>{isRefreshingPlan ? t('plan.page.refreshingStatus') : t('plan.page.successStatus')}</p>
        )}
        {billingStatus === 'cancelled' && <p>{t('plan.page.cancelledStatus')}</p>}
        {billingStatus === 'portal' && <p>{t('plan.page.portalReturnStatus')}</p>}
        <div className="plan-page__card-actions">
          {user?.plan === 'PRO' ? (
            <button
              type="button"
              className="animals-table__action-button"
              onClick={handlePortalClick}
              disabled={isOpeningPortal}
            >
              {isOpeningPortal ? t('plan.page.portalLoadingCta') : t('plan.page.portalCta')}
            </button>
          ) : (
            <button
              type="button"
              className="animals-table__action-button"
              onClick={handleUpgradeClick}
              disabled={isStartingCheckout}
            >
              {isStartingCheckout ? t('plan.page.checkoutLoadingCta') : t('plan.page.checkoutCta')}
            </button>
          )}
        </div>
      </section>
    </main>
  )
}

export default PlansPage
