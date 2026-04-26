import { useEffect, useEffectEvent, useState } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import StatCard from '../../components/dashboard/StatCard'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import { exportDashboardCsv, fetchDashboard } from '../../services/dashboardService'
import { appendCurrencyCode } from '../../utils/currency'
import type { Animal, ApiErrorResponse, AnimalStatus } from '../../types/animal'
import type { DashboardFilters, DashboardSummary } from '../../types/dashboard'
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

const initialFilters: DashboardFilters = {
  startDate: '',
  endDate: '',
  animalId: '',
  status: '',
}

const animalStatusOptions: AnimalStatus[] = ['ACTIVE', 'SOLD', 'DEAD', 'INACTIVE']

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const apiMessage = error.response?.data?.error

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function mapAnimalsToOptions(animals: Animal[]) {
  return animals.map(({ id, tag }) => ({
    id,
    tag,
  }))
}

function DashboardPage() {
  const { t } = useTranslation()
  const { currency } = useCurrency()
  const { selectedFarmId } = useFarm()
  const [animals, setAnimals] = useState<Array<{ id: string; tag: string }>>([])
  const [filters, setFilters] = useState<DashboardFilters>(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState<DashboardFilters>(initialFilters)
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
  const [includeAcquisitionCost, setIncludeAcquisitionCost] = useState(true)
  const [isLoading, setIsLoading] = useState(true)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isExporting, setIsExporting] = useState(false)
  const [summaryErrorMessage, setSummaryErrorMessage] = useState('')
  const [animalsErrorMessage, setAnimalsErrorMessage] = useState('')
  const resolveErrorMessage = useEffectEvent((error: unknown, fallbackKey: string) =>
    getErrorMessage(error, t(fallbackKey)),
  )

  useEffect(() => {
    let isActive = true

    async function loadAnimals() {
      if (isActive) {
        setIsAnimalsLoading(true)
        setAnimalsErrorMessage('')
      }

      try {
        const data = selectedFarmId ? await getAllAnimals(selectedFarmId) : []

        if (isActive) {
          const nextAnimals = mapAnimalsToOptions(data)
          setAnimals(nextAnimals)
          setAppliedFilters((current) => (
            current.animalId && !nextAnimals.some((animal) => animal.id === current.animalId)
              ? { ...current, animalId: '' }
              : current
          ))
          setFilters((current) => (
            current.animalId && !nextAnimals.some((animal) => animal.id === current.animalId)
              ? { ...current, animalId: '' }
              : current
          ))
        }
      } catch (error) {
        if (isActive) {
          setAnimalsErrorMessage(resolveErrorMessage(error, 'dashboard.loadAnimalsError'))
        }
      } finally {
        if (isActive) {
          setIsAnimalsLoading(false)
        }
      }
    }

    void loadAnimals()

    return () => {
      isActive = false
    }
  }, [selectedFarmId])

  useEffect(() => {
    let isActive = true

    async function loadDashboard() {
      if (isActive) {
        setIsLoading(true)
        setSummaryErrorMessage('')
      }

      if (!selectedFarmId) {
        if (isActive) {
          setSummary(null)
          setIsLoading(false)
        }
        return
      }

      try {
        const data = await fetchDashboard(selectedFarmId, includeAcquisitionCost, currency, appliedFilters)

        if (isActive) {
          setSummary(data)
        }
      } catch (error) {
        if (isActive) {
          setSummaryErrorMessage(resolveErrorMessage(error, 'dashboard.error'))
        }
      } finally {
        if (isActive) {
          setIsLoading(false)
        }
      }
    }

    void loadDashboard()

    return () => {
      isActive = false
    }
  }, [appliedFilters, currency, includeAcquisitionCost, selectedFarmId])

  function updateFilter<Key extends keyof DashboardFilters>(key: Key, value: DashboardFilters[Key]) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [key]: value,
    }))
  }

  function applyFilters() {
    setAppliedFilters(filters)
  }

  function clearFilters() {
    setFilters(initialFilters)
    setAppliedFilters(initialFilters)
  }

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)
    setSummaryErrorMessage('')

    try {
      await exportDashboardCsv(selectedFarmId, includeAcquisitionCost, currency, appliedFilters)
    } catch (error) {
      setSummaryErrorMessage(getErrorMessage(error, t('common.exportError')))
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
        <div className="analytics-controls">
          <label htmlFor="dashboard-start-date">
            {t('dashboard.filters.startDateLabel')}
            <input
              id="dashboard-start-date"
              type="date"
              value={filters.startDate}
              onChange={(event) => updateFilter('startDate', event.target.value)}
            />
          </label>

          <label htmlFor="dashboard-end-date">
            {t('dashboard.filters.endDateLabel')}
            <input
              id="dashboard-end-date"
              type="date"
              value={filters.endDate}
              onChange={(event) => updateFilter('endDate', event.target.value)}
            />
          </label>

          <label htmlFor="dashboard-animal-select">
            {t('dashboard.filters.animalLabel')}
            <select
              id="dashboard-animal-select"
              value={filters.animalId}
              onChange={(event) => updateFilter('animalId', event.target.value)}
              disabled={isAnimalsLoading}
            >
              <option value="">{t('dashboard.filters.allAnimals')}</option>
              {animals.map((animal) => (
                <option key={animal.id} value={animal.id}>
                  {animal.tag}
                </option>
              ))}
            </select>
          </label>

          <label htmlFor="dashboard-status-select">
            {t('dashboard.filters.statusLabel')}
            <select
              id="dashboard-status-select"
              value={filters.status}
              onChange={(event) => updateFilter('status', event.target.value as DashboardFilters['status'])}
            >
              <option value="">{t('dashboard.filters.allStatuses')}</option>
              {animalStatusOptions.map((status) => (
                <option key={status} value={status}>
                  {t(`animals.statuses.${status}`)}
                </option>
              ))}
            </select>
          </label>

          <label htmlFor="dashboard-include-acquisition-cost" className="analytics-controls__checkbox">
            <input
              id="dashboard-include-acquisition-cost"
              type="checkbox"
              checked={includeAcquisitionCost}
              onChange={(event) => setIncludeAcquisitionCost(event.target.checked)}
            />
            <span>{t('dashboard.includeAcquisitionCost')}</span>
          </label>
        </div>

        <div className="analytics-actions">
          <button type="button" className="animals-table__action-button" onClick={applyFilters}>
            {t('dashboard.filters.apply')}
          </button>
          <button
            type="button"
            className="animals-table__action-button animals-table__action-button--secondary"
            onClick={clearFilters}
          >
            {t('dashboard.filters.clear')}
          </button>
        </div>
      </section>

      {isAnimalsLoading && <p className="dashboard-page__status">{t('dashboard.loadingAnimals')}</p>}

      {animalsErrorMessage && (
        <p className="dashboard-page__status dashboard-page__status--error">
          {animalsErrorMessage}
        </p>
      )}

      {isLoading && <p className="dashboard-page__status">{t('dashboard.loading')}</p>}

      {summaryErrorMessage && (
        <p className="dashboard-page__status dashboard-page__status--error">
          {summaryErrorMessage}
        </p>
      )}

      {summary && !isLoading && !summaryErrorMessage && (
        <section className="dashboard-grid" aria-label={t('dashboard.ariaSummary')}>
          {dashboardStats.map((stat) => (
            <StatCard
              key={stat.key}
              title={stat.format === 'currency'
                ? appendCurrencyCode(t(stat.titleKey), currency)
                : t(stat.titleKey)}
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
