import { NavLink, Outlet, useNavigate } from 'react-router-dom'
import { useAuth } from '../hooks/useAuth'
import { useFarm } from '../hooks/useFarm'
import { useLanguage, type Language } from '../context/LanguageContext'
import { useTranslation } from '../hooks/useTranslation'

const navigationItems = [
  { to: '/dashboard', labelKey: 'layout.navigation.dashboard' },
  { to: '/animals', labelKey: 'layout.navigation.animals' },
  { to: '/production', labelKey: 'layout.navigation.production' },
  { to: '/feeding', labelKey: 'layout.navigation.feeding' },
  { to: '/feed-types', labelKey: 'layout.navigation.feedTypes' },
  { to: '/users', labelKey: 'layout.navigation.users' },
  { to: '/analytics', labelKey: 'layout.navigation.analytics' },
]

function AppLayout() {
  const navigate = useNavigate()
  const { user, logout } = useAuth()
  const {
    farms,
    selectedFarmId,
    selectedFarm,
    isLoading: isFarmsLoading,
    errorMessage: farmsErrorMessage,
    setSelectedFarmId,
  } = useFarm()
  const { language, setLanguage } = useLanguage()
  const { t } = useTranslation()

  function handleLanguageChange(nextLanguage: Language) {
    setLanguage(nextLanguage)
  }

  function handleLogout() {
    logout()
    navigate('/login', { replace: true })
  }

  return (
    <div className="app-layout">
      <aside className="app-layout__sidebar" aria-label={t('layout.ariaLabel')}>
        <div className="app-layout__brand">
          <span className="app-layout__eyebrow">{t('layout.brandEyebrow')}</span>
          <strong className="app-layout__title">{t('layout.brandTitle')}</strong>
        </div>

        <nav className="app-layout__nav">
          {navigationItems.map((item) => (
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
              PT
            </button>
            <button
              type="button"
              className={`app-layout__language-button${language === 'en' ? ' app-layout__language-button--active' : ''}`}
              onClick={() => handleLanguageChange('en')}
              aria-pressed={language === 'en'}
            >
              EN
            </button>
          </div>
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
        ) : !selectedFarm ? (
          <main className="animals-page">
            <section className="animals-page__header">
              <p className="animals-page__eyebrow">{t('farm.eyebrow')}</p>
              <h1>{t('farm.selectionRequiredTitle')}</h1>
              <p className="animals-page__description">{t('farm.selectionRequiredDescription')}</p>
            </section>
          </main>
        ) : (
          <Outlet />
        )}
      </div>
    </div>
  )
}

export default AppLayout
