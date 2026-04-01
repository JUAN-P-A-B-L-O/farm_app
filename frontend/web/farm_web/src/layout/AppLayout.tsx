import { NavLink, Outlet } from 'react-router-dom'
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
  const { language, setLanguage } = useLanguage()
  const { t } = useTranslation()

  function handleLanguageChange(nextLanguage: Language) {
    setLanguage(nextLanguage)
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
      </aside>

      <div className="app-layout__content">
        <Outlet />
      </div>
    </div>
  )
}

export default AppLayout
