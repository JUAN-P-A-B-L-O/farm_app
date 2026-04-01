import { Navigate, Route, Routes, useNavigate, useParams } from 'react-router-dom'
import './App.css'
import DashboardPage from './pages/dashboard/DashboardPage'
import AnimalsPage from './pages/animals/AnimalsPage'
import AnimalDetailsPage from './pages/animals/AnimalDetailsPage'
import AnalyticsPage from './pages/analytics/AnalyticsPage'
import FeedingPage from './pages/feeding/FeedingPage'
import ProductionPage from './pages/production/ProductionPage'
import AppLayout from './layout/AppLayout'

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
      <Route element={<AppLayout />}>
        <Route index element={<Navigate to="/dashboard" replace />} />
        <Route path="/dashboard" element={<DashboardPage />} />
        <Route path="/animals" element={<AnimalsRoute />} />
        <Route path="/animals/:id" element={<AnimalDetailsRoute />} />
        <Route path="/production" element={<ProductionPage />} />
        <Route path="/feeding" element={<FeedingPage />} />
        <Route path="/analytics" element={<AnalyticsPage />} />
        <Route path="/productions" element={<Navigate to="/production" replace />} />
        <Route path="/feedings" element={<Navigate to="/feeding" replace />} />
      </Route>
      <Route path="*" element={<Navigate to="/dashboard" replace />} />
    </Routes>
  )
}

export default App
