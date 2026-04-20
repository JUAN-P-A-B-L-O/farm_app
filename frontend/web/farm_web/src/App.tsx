import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import './App.css'
import DashboardPage from './pages/dashboard/DashboardPage'
import AnimalsPage from './pages/animals/AnimalsPage'
import AnimalDetailsPage from './pages/animals/AnimalDetailsPage'
import AnalyticsPage from './pages/analytics/AnalyticsPage'
import FeedingPage from './pages/feeding/FeedingPage'
import FeedTypePage from './pages/feed-type/FeedTypePage'
import ProductionPage from './pages/production/ProductionPage'
import MilkPricePage from './pages/milk-price/MilkPricePage'
import UsersPage from './pages/users/UsersPage'
import AppLayout from './layout/AppLayout'
import LoginPage from './pages/login/LoginPage'
import ProtectedRoute from './components/auth/ProtectedRoute'
import ManagerRoute from './components/auth/ManagerRoute'
import FarmCreatePage from './pages/farm/FarmCreatePage'
import SettingsPage from './pages/settings/SettingsPage'
import { useAuth } from './hooks/useAuth'
import { isManager } from './utils/authorization'

function AnimalsRoute() {
  const navigate = useNavigate()

  return <AnimalsPage onOpenDetails={(animalId) => navigate(`/animals/${animalId}`)} />
}

function AnimalDetailsRoute() {
  const navigate = useNavigate()
  const { id = '' } = useParams()

  return <AnimalDetailsPage animalId={id} onBackToAnimals={() => navigate('/animals')} />
}

function DefaultRoute() {
  const { user } = useAuth()

  return <Navigate to={isManager(user) ? '/dashboard' : '/animals'} replace />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<DefaultRoute />} />
          <Route
            path="/dashboard"
            element={(
              <ManagerRoute>
                <DashboardPage />
              </ManagerRoute>
            )}
          />
          <Route path="/animals" element={<AnimalsRoute />} />
          <Route path="/animals/:id" element={<AnimalDetailsRoute />} />
          <Route path="/production" element={<ProductionPage />} />
          <Route path="/milk-prices" element={<MilkPricePage />} />
          <Route path="/feeding" element={<FeedingPage />} />
          <Route path="/feed-types" element={<FeedTypePage />} />
          <Route path="/settings" element={<SettingsPage />} />
          <Route
            path="/users"
            element={(
              <ManagerRoute>
                <UsersPage />
              </ManagerRoute>
            )}
          />
          <Route
            path="/analytics"
            element={(
              <ManagerRoute>
                <AnalyticsPage />
              </ManagerRoute>
            )}
          />
          <Route path="/farms/new" element={<FarmCreatePage />} />
          <Route path="/productions" element={<Navigate to="/production" replace />} />
          <Route path="/feedings" element={<Navigate to="/feeding" replace />} />
        </Route>
      </Route>
      <Route path="*" element={<Navigate to="/" replace />} />
    </Routes>
  )
}

export default App
