import { useEffect, useState } from 'react'
import axios from 'axios'
import BarChart from '../../components/analytics/BarChart'
import LineChart from '../../components/analytics/LineChart'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import { getAnalyticsDataset } from '../../services/analyticsService'
import type { Animal, ApiErrorResponse } from '../../types/animal'
import type { AnalyticsDataset, AnalyticsFilters } from '../../types/analytics'
import '../../App.css'

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

const initialFilters: AnalyticsFilters = {
  startDate: '',
  endDate: '',
  animalId: '',
  groupBy: 'day',
}

const emptyDataset: AnalyticsDataset = {
  productionSeries: [],
  feedingCostSeries: [],
  profitSeries: [],
  productionByAnimal: [],
}

function AnalyticsPage() {
  const { t, language } = useTranslation()
  const [animals, setAnimals] = useState<Array<{ id: string; tag: string }>>([])
  const [filters, setFilters] = useState<AnalyticsFilters>(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState<AnalyticsFilters>(initialFilters)
  const [analytics, setAnalytics] = useState<AnalyticsDataset>(emptyDataset)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isChartsLoading, setIsChartsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    async function loadAnimals() {
      setIsAnimalsLoading(true)
      setErrorMessage('')

      try {
        const data = await getAllAnimals()
        setAnimals(mapAnimalsToOptions(data))
      } catch (error) {
        setErrorMessage(getErrorMessage(error, t('analytics.loadAnimalsError')))
      } finally {
        setIsAnimalsLoading(false)
      }
    }

    void loadAnimals()
  }, [language])

  useEffect(() => {
    async function loadAnalytics() {
      setIsChartsLoading(true)
      setErrorMessage('')

      try {
        const dataset = await getAnalyticsDataset(appliedFilters)
        setAnalytics(dataset)
      } catch (error) {
        setErrorMessage(getErrorMessage(error, t('analytics.loadChartsError')))
        setAnalytics(emptyDataset)
      } finally {
        setIsChartsLoading(false)
      }
    }

    void loadAnalytics()
  }, [appliedFilters, language, t])

  const showCharts = !isChartsLoading && !errorMessage

  function updateFilter<Key extends keyof AnalyticsFilters>(key: Key, value: AnalyticsFilters[Key]) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [key]: value,
    }))
  }

  function applyFilters() {
    setAppliedFilters(filters)
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('analytics.eyebrow')}</p>
        <h1>{t('analytics.title')}</h1>
        <p className="animals-page__description">
          {t('analytics.description')}
        </p>
      </section>

      <section className="analytics-layout">
        <article className="analytics-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('analytics.filtersTitle')}</h2>
              <p>{t('analytics.filtersDescription')}</p>
            </div>
          </div>

          <div className="analytics-controls">
            <label htmlFor="analytics-start-date">
              {t('analytics.startDateLabel')}
              <input
                id="analytics-start-date"
                type="date"
                value={filters.startDate}
                onChange={(event) => updateFilter('startDate', event.target.value)}
              />
            </label>

            <label htmlFor="analytics-end-date">
              {t('analytics.endDateLabel')}
              <input
                id="analytics-end-date"
                type="date"
                value={filters.endDate}
                onChange={(event) => updateFilter('endDate', event.target.value)}
              />
            </label>

            <label htmlFor="analytics-animal-select">
              {t('analytics.animalLabel')}
              <select
                id="analytics-animal-select"
                value={filters.animalId}
                onChange={(event) => updateFilter('animalId', event.target.value)}
                disabled={isAnimalsLoading}
              >
                <option value="">{t('analytics.allAnimals')}</option>
                {animals.map((animal) => (
                  <option key={animal.id} value={animal.id}>
                    {animal.tag}
                  </option>
                ))}
              </select>
            </label>

            <label htmlFor="analytics-group-by">
              {t('analytics.groupByLabel')}
              <select
                id="analytics-group-by"
                value={filters.groupBy}
                onChange={(event) => updateFilter('groupBy', event.target.value as AnalyticsFilters['groupBy'])}
              >
                <option value="day">{t('analytics.groupByDay')}</option>
                <option value="month">{t('analytics.groupByMonth')}</option>
              </select>
            </label>
          </div>

          <div className="analytics-actions">
            <button type="button" className="animals-panel__button" onClick={applyFilters}>
              {t('analytics.applyFilters')}
            </button>
          </div>

          {isAnimalsLoading && <p className="animals-page__status">{t('analytics.loadingAnimals')}</p>}

          {!isAnimalsLoading && !errorMessage && animals.length === 0 && (
            <p className="animals-page__status">{t('analytics.emptyAnimals')}</p>
          )}

          {isChartsLoading && <p className="animals-page__status">{t('analytics.loadingCharts')}</p>}

          {errorMessage && (
            <p className="animals-page__status animals-page__status--error">{errorMessage}</p>
          )}
        </article>

        <section className="analytics-grid">
          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>{t('analytics.productionTitle')}</h2>
              <p>{t('analytics.productionDescription')}</p>
            </div>

            {showCharts && analytics.productionSeries.length > 0 && (
              <LineChart data={analytics.productionSeries} color="#2e6a46" />
            )}

            {showCharts && analytics.productionSeries.length === 0 && (
              <p className="analytics-chart__empty">{t('analytics.emptyState')}</p>
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>{t('analytics.feedingCostTitle')}</h2>
              <p>{t('analytics.feedingCostDescription')}</p>
            </div>

            {showCharts && analytics.feedingCostSeries.length > 0 && (
              <LineChart data={analytics.feedingCostSeries} color="#c26b2c" />
            )}

            {showCharts && analytics.feedingCostSeries.length === 0 && (
              <p className="analytics-chart__empty">{t('analytics.emptyState')}</p>
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>{t('analytics.profitTitle')}</h2>
              <p>{t('analytics.profitDescription')}</p>
            </div>

            {showCharts && analytics.profitSeries.length > 0 && (
              <LineChart data={analytics.profitSeries} color="#2e5b9a" />
            )}

            {showCharts && analytics.profitSeries.length === 0 && (
              <p className="analytics-chart__empty">{t('analytics.emptyState')}</p>
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>{t('analytics.productionByAnimalTitle')}</h2>
              <p>{t('analytics.productionByAnimalDescription')}</p>
            </div>

            {showCharts && analytics.productionByAnimal.length > 0 && (
              <BarChart data={analytics.productionByAnimal} color="#7b8f2a" />
            )}

            {showCharts && analytics.productionByAnimal.length === 0 && (
              <p className="analytics-chart__empty">{t('analytics.emptyState')}</p>
            )}
          </article>
        </section>
      </section>
    </main>
  )
}

export default AnalyticsPage
