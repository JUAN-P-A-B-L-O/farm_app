import { useEffect, useState } from 'react'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import StatCard from '../../components/dashboard/StatCard'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { exportDashboardCsv, fetchDashboard } from '../../services/dashboardService'
import type { DashboardSummary } from '../../types/dashboard'
import '../../App.css'

const dashboardStats: Array<{
  key: keyof DashboardSummary
  titleKey: string
  format: 'number' | 'currency'
}> = [
  { key: 'totalProduction', titleKey: 'dashboard.stats.totalProduction', format: 'number' },
  { key: 'totalFeedingCost', titleKey: 'dashboard.stats.totalFeedingCost', format: 'currency' },
  { key: 'totalRevenue', titleKey: 'dashboard.stats.totalRevenue', format: 'currency' },
  { key: 'totalProfit', titleKey: 'dashboard.stats.totalProfit', format: 'currency' },
  { key: 'animalCount', titleKey: 'dashboard.stats.animalCount', format: 'number' },
]

function DashboardPage() {
  const { t, language } = useTranslation()
  const { selectedFarmId } = useFarm()
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [includeAcquisitionCost, setIncludeAcquisitionCost] = useState(true)
  const [isLoading, setIsLoading] = useState(true)
  const [isExporting, setIsExporting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    async function loadDashboard() {
      setIsLoading(true)
      setErrorMessage('')

      if (!selectedFarmId) {
        setSummary(null)
        setIsLoading(false)
        return
      }

      try {
        const data = await fetchDashboard(selectedFarmId, includeAcquisitionCost)
        setSummary(data)
      } catch {
        setErrorMessage(t('dashboard.error'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadDashboard()
  }, [includeAcquisitionCost, language, selectedFarmId])

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)
    setErrorMessage('')

    try {
      await exportDashboardCsv(selectedFarmId, includeAcquisitionCost)
    } catch {
      setErrorMessage(t('common.exportError'))
    } finally {
      setIsExporting(false)
    }
  }

  return (
    <main className="dashboard-page">
      <section className="dashboard-page__header">
        <div className="dashboard-page__header-actions">
          <div>
            <p className="dashboard-page__eyebrow">{t('dashboard.eyebrow')}</p>
            <h1>{t('dashboard.title')}</h1>
            <p className="dashboard-page__description">
              {t('dashboard.description')}
            </p>
          </div>
          <ExportCsvButton
            onClick={() => void handleExport()}
            label={t('common.exportCsv')}
            loadingLabel={t('common.exportingCsv')}
            isLoading={isExporting}
            disabled={!selectedFarmId || isLoading || !summary}
          />
        </div>
        <label className="analytics-controls__checkbox">
          <input
            type="checkbox"
            checked={includeAcquisitionCost}
            onChange={(event) => setIncludeAcquisitionCost(event.target.checked)}
          />
          <span>{t('dashboard.includeAcquisitionCost')}</span>
        </label>
      </section>

      {isLoading && <p className="dashboard-page__status">{t('dashboard.loading')}</p>}

      {errorMessage && (
        <p className="dashboard-page__status dashboard-page__status--error">
          {errorMessage}
        </p>
      )}

      {summary && !isLoading && !errorMessage && (
        <section className="dashboard-grid" aria-label={t('dashboard.ariaSummary')}>

          {dashboardStats.map((stat) => (
            <StatCard
              key={stat.key}
              title={t(stat.titleKey)}
              value={summary[stat.key] ?? 0}
              format={stat.format}
            />
          ))}
        </section>
      )}
    </main>
  )
}

export default DashboardPage
