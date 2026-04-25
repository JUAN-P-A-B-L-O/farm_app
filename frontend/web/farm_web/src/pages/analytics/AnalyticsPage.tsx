import { useEffect, useEffectEvent, useState } from 'react'
import axios from 'axios'
import BarChart from '../../components/analytics/BarChart'
import ChartErrorBoundary from '../../components/analytics/ChartErrorBoundary'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import LineChart from '../../components/analytics/LineChart'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import {
  exportAnalyticsFeedingCsv,
  exportAnalyticsProductionByAnimalCsv,
  exportAnalyticsProductionCsv,
  exportAnalyticsProfitCsv,
  getAnalyticsDataset,
} from '../../services/analyticsService'
import type { Animal, ApiErrorResponse } from '../../types/animal'
import type { AnalyticsDataset, AnalyticsFilters } from '../../types/analytics'
import { appendCurrencyCode, formatCurrencyValue } from '../../utils/currency'
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
  includeAcquisitionCost: true,
}

const emptyDataset: AnalyticsDataset = {
  productionSeries: [],
  feedingCostSeries: [],
  profitSeries: [],
  productionByAnimal: [],
}

function AnalyticsPage() {
  const { t, language } = useTranslation()
  const { currency } = useCurrency()
  const { selectedFarmId } = useFarm()
  const [animals, setAnimals] = useState<Array<{ id: string; tag: string }>>([])
  const [filters, setFilters] = useState<AnalyticsFilters>(initialFilters)
  const [appliedFilters, setAppliedFilters] = useState<AnalyticsFilters>(initialFilters)
  const [analytics, setAnalytics] = useState<AnalyticsDataset>(emptyDataset)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isChartsLoading, setIsChartsLoading] = useState(false)
  const [exportingKey, setExportingKey] = useState<string | null>(null)
  const [hasAppliedFilters, setHasAppliedFilters] = useState(false)
  const [animalsErrorMessage, setAnimalsErrorMessage] = useState('')
  const [chartsErrorMessage, setChartsErrorMessage] = useState('')
  const resolveErrorMessage = useEffectEvent((error: unknown, fallbackKey: string) =>
    getErrorMessage(error, t(fallbackKey)),
  )

  const errorMessage = animalsErrorMessage || chartsErrorMessage

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
          setAnimals(mapAnimalsToOptions(data))
        }
      } catch (error) {
        if (isActive) {
          setAnimalsErrorMessage(resolveErrorMessage(error, 'analytics.loadAnimalsError'))
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
    if (!hasAppliedFilters) {
      return
    }

    let isActive = true

    async function loadAnalytics() {
      if (!selectedFarmId) {
        if (isActive) {
          setAnalytics(emptyDataset)
          setChartsErrorMessage('')
          setIsChartsLoading(false)
        }
        return
      }

      if (isActive) {
        setIsChartsLoading(true)
        setChartsErrorMessage('')
      }

      try {
        const dataset = await getAnalyticsDataset(appliedFilters, selectedFarmId, currency)

        if (isActive) {
          setAnalytics(dataset)
        }
      } catch (error) {
        if (isActive) {
          setChartsErrorMessage(resolveErrorMessage(error, 'analytics.loadChartsError'))
          setAnalytics(emptyDataset)
        }
      } finally {
        if (isActive) {
          setIsChartsLoading(false)
        }
      }
    }

    void loadAnalytics()

    return () => {
      isActive = false
    }
  }, [appliedFilters, currency, hasAppliedFilters, selectedFarmId])

  const showCharts = hasAppliedFilters && !isChartsLoading && !errorMessage
  const shouldShowInitialState = !hasAppliedFilters && !isChartsLoading && !errorMessage
  const formatChartCurrency = (value: number) => formatCurrencyValue(value, language, currency)

  function renderChartEmptyState() {
    return <p className="analytics-chart__empty">{t('analytics.emptyState')}</p>
  }

  function updateFilter<Key extends keyof AnalyticsFilters>(key: Key, value: AnalyticsFilters[Key]) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [key]: value,
    }))
  }

  function applyFilters() {
    setAppliedFilters(filters)
    setHasAppliedFilters(true)
  }

  async function handleExport(
    key: 'production' | 'feeding' | 'profit' | 'productionByAnimal',
  ) {
    if (!selectedFarmId || !hasAppliedFilters) {
      return
    }

    setExportingKey(key)
    setChartsErrorMessage('')

    try {
      if (key === 'production') {
        await exportAnalyticsProductionCsv(appliedFilters, selectedFarmId, currency)
      } else if (key === 'feeding') {
        await exportAnalyticsFeedingCsv(appliedFilters, selectedFarmId, currency)
      } else if (key === 'profit') {
        await exportAnalyticsProfitCsv(appliedFilters, selectedFarmId, currency)
      } else {
        await exportAnalyticsProductionByAnimalCsv(appliedFilters, selectedFarmId, currency)
      }
    } catch (error) {
      setChartsErrorMessage(getErrorMessage(error, t('common.exportError')))
    } finally {
      setExportingKey(null)
    }
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

            <label htmlFor="analytics-include-acquisition-cost" className="analytics-controls__checkbox">
              <input
                id="analytics-include-acquisition-cost"
                type="checkbox"
                checked={filters.includeAcquisitionCost}
                onChange={(event) => updateFilter('includeAcquisitionCost', event.target.checked)}
              />
              <span>{t('analytics.includeAcquisitionCost')}</span>
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
              <div className="analytics-chart__header-copy">
                <h2>{t('analytics.productionTitle')}</h2>
                <p>{t('analytics.productionDescription')}</p>
              </div>
              <ExportCsvButton
                onClick={() => void handleExport('production')}
                label={t('common.exportCsv')}
                loadingLabel={t('common.exportingCsv')}
                isLoading={exportingKey === 'production'}
                disabled={!showCharts || analytics.productionSeries.length === 0}
              />
            </div>

            {shouldShowInitialState && renderChartEmptyState()}

            {showCharts && analytics.productionSeries.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <LineChart data={analytics.productionSeries} color="#2e6a46" />
              </ChartErrorBoundary>
            )}

            {showCharts && analytics.productionSeries.length === 0 && (
              renderChartEmptyState()
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <div className="analytics-chart__header-copy">
                <h2>{appendCurrencyCode(t('analytics.feedingCostTitle'), currency)}</h2>
                <p>{t('analytics.feedingCostDescription')}</p>
              </div>
              <ExportCsvButton
                onClick={() => void handleExport('feeding')}
                label={t('common.exportCsv')}
                loadingLabel={t('common.exportingCsv')}
                isLoading={exportingKey === 'feeding'}
                disabled={!showCharts || analytics.feedingCostSeries.length === 0}
              />
            </div>

            {shouldShowInitialState && renderChartEmptyState()}

            {showCharts && analytics.feedingCostSeries.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <LineChart
                  data={analytics.feedingCostSeries}
                  color="#c26b2c"
                  valueFormatter={formatChartCurrency}
                />
              </ChartErrorBoundary>
            )}

            {showCharts && analytics.feedingCostSeries.length === 0 && (
              renderChartEmptyState()
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <div className="analytics-chart__header-copy">
                <h2>{appendCurrencyCode(t('analytics.profitTitle'), currency)}</h2>
                <p>{t('analytics.profitDescription')}</p>
              </div>
              <ExportCsvButton
                onClick={() => void handleExport('profit')}
                label={t('common.exportCsv')}
                loadingLabel={t('common.exportingCsv')}
                isLoading={exportingKey === 'profit'}
                disabled={!showCharts || analytics.profitSeries.length === 0}
              />
            </div>

            {shouldShowInitialState && renderChartEmptyState()}

            {showCharts && analytics.profitSeries.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <LineChart
                  data={analytics.profitSeries}
                  color="#2e5b9a"
                  valueFormatter={formatChartCurrency}
                />
              </ChartErrorBoundary>
            )}

            {showCharts && analytics.profitSeries.length === 0 && (
              renderChartEmptyState()
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <div className="analytics-chart__header-copy">
                <h2>{t('analytics.productionByAnimalTitle')}</h2>
                <p>{t('analytics.productionByAnimalDescription')}</p>
              </div>
              <ExportCsvButton
                onClick={() => void handleExport('productionByAnimal')}
                label={t('common.exportCsv')}
                loadingLabel={t('common.exportingCsv')}
                isLoading={exportingKey === 'productionByAnimal'}
                disabled={!showCharts || analytics.productionByAnimal.length === 0}
              />
            </div>

            {shouldShowInitialState && renderChartEmptyState()}

            {showCharts && analytics.productionByAnimal.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <BarChart data={analytics.productionByAnimal} color="#7b8f2a" />
              </ChartErrorBoundary>
            )}

            {showCharts && analytics.productionByAnimal.length === 0 && (
              renderChartEmptyState()
            )}
          </article>
        </section>
      </section>
    </main>
  )
}

export default AnalyticsPage
