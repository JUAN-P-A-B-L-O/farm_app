import { useEffect, useState } from 'react'
import StatCard from '../../components/dashboard/StatCard'
import { fetchDashboard } from '../../services/dashboardService'
import type { DashboardSummary } from '../../types/dashboard'
import '../../App.css'

const dashboardStats: Array<{
  key: keyof DashboardSummary
  title: string
}> = [
  { key: 'totalProduction', title: 'Total Production' },
  { key: 'totalFeedingCost', title: 'Feeding Cost' },
  { key: 'totalRevenue', title: 'Total Revenue' },
  { key: 'totalProfit', title: 'Profit' },
  { key: 'animalCount', title: 'Number of Animals' },
]

function DashboardPage() {
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    async function loadDashboard() {
      try {
        const data = await fetchDashboard()
        setSummary(data)
      } catch {
        setErrorMessage('Unable to load dashboard data.')
      } finally {
        setIsLoading(false)
      }
    }

    void loadDashboard()
  }, [])
  return (
    <main className="dashboard-page">
      <section className="dashboard-page__header">
        <p className="dashboard-page__eyebrow">Farm Overview</p>
        <h1>Dashboard</h1>
        <p className="dashboard-page__description">
          Monitor the most important farm metrics from a single summary view.
        </p>
      </section>

      {isLoading && <p className="dashboard-page__status">Loading dashboard...</p>}

      {errorMessage && (
        <p className="dashboard-page__status dashboard-page__status--error">
          {errorMessage}
        </p>
      )}

      {summary && !isLoading && !errorMessage && (
        <section className="dashboard-grid" aria-label="Farm summary metrics">

          {dashboardStats.map((stat) => (
            <StatCard
              key={stat.key}
              title={stat.title}
              value={summary[stat.key] ?? 0}
            />
          ))}
        </section>
      )}
    </main>
  )
}

export default DashboardPage
