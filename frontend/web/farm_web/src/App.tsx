import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import './App.css'
import DashboardPage from './pages/dashboard/DashboardPage'
import AnimalsPage from './pages/animals/AnimalsPage'
import AnimalDetailsPage from './pages/animals/AnimalDetailsPage'
import AnalyticsPage from './pages/analytics/AnalyticsPage'
import FeedingPage from './pages/feeding/FeedingPage'
import FeedTypePage from './pages/feed-type/FeedTypePage'
import ProductionPage from './pages/production/ProductionPage'
import UsersPage from './pages/users/UsersPage'
import AppLayout from './layout/AppLayout'
import LoginPage from './pages/login/LoginPage'
import ProtectedRoute from './components/auth/ProtectedRoute'
import FarmCreatePage from './pages/farm/FarmCreatePage'

function AnimalsRoute() {
  const navigate = useNavigate()

  return <AnimalsPage onOpenDetails={(animalId) => navigate(`/animals/${animalId}`)} />
}

function AnimalDetailsRoute() {
  const navigate = useNavigate()
  const { id = '' } = useParams()

  return <AnimalDetailsPage animalId={id} onBackToAnimals={() => navigate('/animals')} />
}

function App() {
  return (
    <Routes>
      <Route path="/login" element={<LoginPage />} />
      <Route element={<ProtectedRoute />}>
        <Route element={<AppLayout />}>
          <Route index element={<Navigate to="/dashboard" replace />} />
          <Route path="/dashboard" element={<DashboardPage />} />
          <Route path="/animals" element={<AnimalsRoute />} />
          <Route path="/animals/:id" element={<AnimalDetailsRoute />} />
          <Route path="/production" element={<ProductionPage />} />
          <Route path="/feeding" element={<FeedingPage />} />
          <Route path="/feed-types" element={<FeedTypePage />} />
          <Route path="/users" element={<UsersPage />} />
          <Route path="/analytics" element={<AnalyticsPage />} />
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
