import { NavLink, Navigate, Outlet, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useCurrency } from '../hooks/useCurrency'
import { useFarm } from '../hooks/useFarm'
import { useMeasurementUnits } from '../hooks/useMeasurementUnits'
import { useLanguage, type Language } from '../context/LanguageContext'
import { useTranslation } from '../hooks/useTranslation'
import { isManager } from '../utils/authorization'

const navigationItems = [
  { to: '/dashboard', labelKey: 'layout.navigation.dashboard', managerOnly: true },
  { to: '/animals', labelKey: 'layout.navigation.animals' },
  { to: '/production', labelKey: 'layout.navigation.production' },
  { to: '/milk-prices', labelKey: 'layout.navigation.milkPrices' },
  { to: '/feeding', labelKey: 'layout.navigation.feeding' },
  { to: '/feed-types', labelKey: 'layout.navigation.feedTypes' },
  { to: '/users', labelKey: 'layout.navigation.users', managerOnly: true },
  { to: '/analytics', labelKey: 'layout.navigation.analytics', managerOnly: true },
  { to: '/settings', labelKey: 'layout.navigation.settings' },
]

function AppLayout() {
  const navigate = useNavigate()
  const location = useLocation()
  const { user, logout } = useAuth()
  const {
    farms,
    selectedFarmId,
    selectedFarm,
    isLoading: isFarmsLoading,
    errorMessage: farmsErrorMessage,
    setSelectedFarmId,
  } = useFarm()
  const { currency, setCurrency } = useCurrency()
  const {
    productionUnit,
    feedingUnit,
    setProductionUnit,
    setFeedingUnit,
  } = useMeasurementUnits()
  const { language, setLanguage } = useLanguage()
  const { t } = useTranslation()
  const canManageRestrictedFeatures = isManager(user)

  function handleLanguageChange(nextLanguage: Language) {
    setLanguage(nextLanguage)
  }

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  const isFarmSelectionOptionalRoute =
    location.pathname === '/farms/new' || location.pathname === '/settings'

  return (
    <div className="app-layout">
      <aside className="app-layout__sidebar" aria-label={t('layout.ariaLabel')}>
        <div className="app-layout__brand">
          <span className="app-layout__eyebrow">{t('layout.brandEyebrow')}</span>
          <strong className="app-layout__title">{t('layout.brandTitle')}</strong>
        </div>

        <nav className="app-layout__nav">
          {navigationItems.filter((item) => !item.managerOnly || canManageRestrictedFeatures).map((item) => (
            <NavLink
              key={item.to}
              to={item.to}
              className={({ isActive }) =>
                `app-layout__nav-link${isActive ? ' app-layout__nav-link--active' : ''}`
              }
            >
              {t(item.labelKey)}
            </NavLink>
          ))}
        </nav>

        <div className="app-layout__farm-selector">
          <label className="app-layout__language-label" htmlFor="farm-selector">
            {t('layout.farmLabel')}
          </label>
          <select
            id="farm-selector"
            className="app-layout__farm-select"
            value={selectedFarmId}
            onChange={(event) => setSelectedFarmId(event.target.value)}
            disabled={isFarmsLoading || farms.length === 0}
          >
            <option value="">
              {isFarmsLoading ? t('layout.loadingFarms') : t('layout.selectFarm')}
            </option>
            {farms.map((farm) => (
              <option key={farm.id} value={farm.id}>
                {farm.name}
              </option>
            ))}
          </select>
          {farmsErrorMessage && (
            <p className="animals-page__status animals-page__status--error">{t('farm.loadError')}</p>
          )}
          <button
            type="button"
            className="animals-table__action-button animals-table__action-button--secondary"
            onClick={() => navigate('/farms/new')}
          >
            {t('farm.createAction')}
          </button>
        </div>

        <div className="app-layout__language-switcher" role="group" aria-label={t('layout.languageLabel')}>
          <span className="app-layout__language-label">{t('layout.languageLabel')}</span>
          <div className="app-layout__language-options">
            <button
              type="button"
              className={`app-layout__language-button${language === 'pt-BR' ? ' app-layout__language-button--active' : ''}`}
              onClick={() => handleLanguageChange('pt-BR')}
              aria-pressed={language === 'pt-BR'}
            >
              {t('layout.languageOptions.pt-BR')}
            </button>
            <button
              type="button"
              className={`app-layout__language-button${language === 'en' ? ' app-layout__language-button--active' : ''}`}
              onClick={() => handleLanguageChange('en')}
              aria-pressed={language === 'en'}
            >
              {t('layout.languageOptions.en')}
            </button>
          </div>
        </div>

        <div className="app-layout__farm-selector">
          <label className="app-layout__language-label" htmlFor="currency-selector">
            {t('layout.currencyLabel')}
          </label>
          <select
            id="currency-selector"
            className="app-layout__farm-select"
            value={currency}
            onChange={(event) => setCurrency(event.target.value as 'BRL' | 'USD')}
          >
            <option value="BRL">{t('layout.currencyOptions.BRL')}</option>
            <option value="USD">{t('layout.currencyOptions.USD')}</option>
          </select>
        </div>

        <div className="app-layout__farm-selector">
          <label className="app-layout__language-label" htmlFor="production-unit-selector">
            {t('measurementUnits.productionLabel')}
          </label>
          <select
            id="production-unit-selector"
            className="app-layout__farm-select"
            value={productionUnit}
            onChange={(event) => setProductionUnit(event.target.value as typeof productionUnit)}
          >
            <option value="LITER">{t('measurementUnits.options.LITER')}</option>
            <option value="MILLILITER">{t('measurementUnits.options.MILLILITER')}</option>
          </select>
        </div>

        <div className="app-layout__farm-selector">
          <label className="app-layout__language-label" htmlFor="feeding-unit-selector">
            {t('measurementUnits.feedingLabel')}
          </label>
          <select
            id="feeding-unit-selector"
            className="app-layout__farm-select"
            value={feedingUnit}
            onChange={(event) => setFeedingUnit(event.target.value as typeof feedingUnit)}
          >
            <option value="KILOGRAM">{t('measurementUnits.options.KILOGRAM')}</option>
            <option value="GRAM">{t('measurementUnits.options.GRAM')}</option>
          </select>
        </div>

        <div className="app-layout__session">
          <p className="app-layout__session-user">{user?.name ?? user?.email}</p>
          <button type="button" className="app-layout__logout-button" onClick={handleLogout}>
            {t('layout.logout')}
          </button>
        </div>
      </aside>

      <div className="app-layout__content">
        {isFarmsLoading ? (
          <main className="animals-page">
            <section className="animals-page__header">
              <p className="animals-page__eyebrow">{t('farm.eyebrow')}</p>
              <h1>{t('layout.loadingFarms')}</h1>
            </section>
          </main>
        ) : !selectedFarm && !isFarmSelectionOptionalRoute ? (
          <Navigate to="/farms/new" replace />
        ) : (
          <Outlet />
        )}
      </div>
    </div>
  )
}

export default AppLayout
