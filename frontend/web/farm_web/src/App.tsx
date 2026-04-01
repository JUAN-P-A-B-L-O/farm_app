import { useEffect, useState } from 'react'
import './App.css'
import AnimalsPage from './pages/animals/AnimalsPage'
import AnimalDetailsPage from './pages/animals/AnimalDetailsPage'
import AnalyticsPage from './pages/analytics/AnalyticsPage'
import FeedingPage from './pages/feeding/FeedingPage'
import ProductionPage from './pages/production/ProductionPage'

interface RouteMatch {
  params: Record<string, string>
  routeKey: 'animals' | 'animal-details' | 'production' | 'feeding' | 'analytics'
}

function normalizePath(pathname: string): string {
  if (pathname.length > 1 && pathname.endsWith('/')) {
    return pathname.slice(0, -1)
  }

  return pathname
}

function matchRoute(pathname: string): RouteMatch {
  const normalizedPath = normalizePath(pathname)

  if (normalizedPath === '/animals') {
    return {
      routeKey: 'animals',
      params: {},
    }
  }

  if (normalizedPath.startsWith('/animals/')) {
    const animalId = normalizedPath.slice('/animals/'.length)

    return {
      routeKey: 'animal-details',
      params: {
        animalId,
      },
    }
  }

  if (normalizedPath === '/productions') {
    return {
      routeKey: 'production',
      params: {},
    }
  }

  if (normalizedPath === '/analytics') {
    return {
      routeKey: 'analytics',
      params: {},
    }
  }

  return {
    routeKey: 'feeding',
    params: {},
  }
}

function App() {
  const [pathname, setPathname] = useState(() => normalizePath(window.location.pathname))

  useEffect(() => {
    function handlePopState() {
      setPathname(normalizePath(window.location.pathname))
    }

    window.addEventListener('popstate', handlePopState)

    return () => {
      window.removeEventListener('popstate', handlePopState)
    }
  }, [])

  function navigate(path: string) {
    const nextPath = normalizePath(path)

    if (nextPath === pathname) {
      return
    }

    window.history.pushState({}, '', nextPath)
    setPathname(nextPath)
  }

  const route = matchRoute(pathname)

  return (
    <>
      <nav className="app-nav" aria-label="Primary">
        <button
          type="button"
          className={`app-nav__link${route.routeKey === 'animals' || route.routeKey === 'animal-details' ? ' app-nav__link--active' : ''}`}
          onClick={() => navigate('/animals')}
        >
          Animals
        </button>
        <button
          type="button"
          className={`app-nav__link${route.routeKey === 'production' ? ' app-nav__link--active' : ''}`}
          onClick={() => navigate('/productions')}
        >
          Production
        </button>
        <button
          type="button"
          className={`app-nav__link${route.routeKey === 'feeding' ? ' app-nav__link--active' : ''}`}
          onClick={() => navigate('/feedings')}
        >
          Feeding
        </button>
        <button
          type="button"
          className={`app-nav__link${route.routeKey === 'analytics' ? ' app-nav__link--active' : ''}`}
          onClick={() => navigate('/analytics')}
        >
          Analytics
        </button>
      </nav>

      {route.routeKey === 'animals' && <AnimalsPage onOpenDetails={(animalId) => navigate(`/animals/${animalId}`)} />}
      {route.routeKey === 'animal-details' && (
        <AnimalDetailsPage
          animalId={route.params.animalId}
          onBackToAnimals={() => navigate('/animals')}
        />
      )}
      {route.routeKey === 'production' && <ProductionPage />}
      {route.routeKey === 'feeding' && <FeedingPage />}
      {route.routeKey === 'analytics' && <AnalyticsPage />}
    </>
  )
}

export default App
