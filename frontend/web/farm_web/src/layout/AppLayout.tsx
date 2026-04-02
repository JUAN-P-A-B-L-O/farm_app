import { NavLink, Outlet } from 'react-router-dom'

const navigationItems = [
  { to: '/dashboard', label: 'Dashboard' },
  { to: '/animals', label: 'Animals' },
  { to: '/production', label: 'Production' },
  { to: '/feeding', label: 'Feeding' },
  { to: '/feed-types', label: 'Feed Types' },
  { to: '/users', label: 'Users' },
  { to: '/analytics', label: 'Analytics' },
]

function AppLayout() {
  return (
    <div className="app-layout">
      <aside className="app-layout__sidebar" aria-label="Primary">
        <div className="app-layout__brand">
          <span className="app-layout__eyebrow">Farm App</span>
          <strong className="app-layout__title">Management</strong>
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
              {item.label}
            </NavLink>
          ))}
        </nav>
      </aside>

      <div className="app-layout__content">
        <Outlet />
      </div>
    </div>
  )
}

export default AppLayout
