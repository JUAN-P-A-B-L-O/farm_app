import { Navigate, Outlet, useLocation } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'

function ProtectedRoute() {
  const { isAuthenticated } = useAuth()
  const { hasResolvedFarms, isLoading, needsOnboarding } = useFarm()
  const { t } = useTranslation()
  const location = useLocation()
  const isOnboardingRoute = location.pathname === '/onboarding/farm'

  if (!isAuthenticated) {
    return <Navigate to="/login" replace state={{ from: location }} />
  }

  if (!hasResolvedFarms || isLoading) {
    return (
      <main className="animals-page">
        <section className="animals-page__header">
          <p className="animals-page__eyebrow">{t('farm.eyebrow')}</p>
          <h1>{t('layout.loadingFarms')}</h1>
        </section>
      </main>
    )
  }

  if (needsOnboarding && !isOnboardingRoute) {
    return <Navigate to="/onboarding/farm" replace />
  }

  if (!needsOnboarding && isOnboardingRoute) {
    return <Navigate to="/" replace />
  }

  return <Outlet />
}

export default ProtectedRoute
