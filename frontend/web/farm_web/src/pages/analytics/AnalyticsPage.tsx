import { useEffect, useEffectEvent, useRef, useState } from 'react'
import axios from 'axios'
import BarChart from '../../components/analytics/BarChart'
import ChartErrorBoundary from '../../components/analytics/ChartErrorBoundary'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import LineChart from '../../components/analytics/LineChart'
import { useAutoAppliedFilters } from '../../hooks/useAutoAppliedFilters'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useMeasurementUnits } from '../../hooks/useMeasurementUnits'
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
import {
  appendUnitToLabel,
  convertMeasurementFromBase,
  formatConvertedMeasurementValue,
  getMeasurementUnitShortLabelKey,
} from '../../utils/measurementUnits'
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

function createInitialFilters(): AnalyticsFilters {
  return {
    startDate: '',
    endDate: '',
    animalId: '',
    groupBy: 'day',
    includeAcquisitionCost: true,
  }
}

const emptyDataset: AnalyticsDataset = {
  productionSeries: [],
  feedingCostSeries: [],
  profitSeries: [],
  productionByAnimal: [],
}

function filterAvailableAnimalId(currentAnimalId: string, nextAnimals: Array<{ id: string; tag: string }>) {
  if (!currentAnimalId) {
    return currentAnimalId
  }

  return nextAnimals.some((animal) => animal.id === currentAnimalId) ? currentAnimalId : ''
}

function AnalyticsPage() {
  const { t, language } = useTranslation()
  const { currency } = useCurrency()
  const { selectedFarmId } = useFarm()
  const { productionUnit } = useMeasurementUnits()
  const [animals, setAnimals] = useState<Array<{ id: string; tag: string }>>([])
  const [analytics, setAnalytics] = useState<AnalyticsDataset>(emptyDataset)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isChartsLoading, setIsChartsLoading] = useState(false)
  const [exportingKey, setExportingKey] = useState<string | null>(null)
  const [animalsErrorMessage, setAnimalsErrorMessage] = useState('')
  const [chartsErrorMessage, setChartsErrorMessage] = useState('')
  const previousSelectedFarmIdRef = useRef(selectedFarmId)
  const { filters, appliedFilters, setFilters, resetFilters } = useAutoAppliedFilters(createInitialFilters)
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
          const nextAnimals = mapAnimalsToOptions(data)
          setAnimals(nextAnimals)
          setFilters((current) => {
            if (!current.animalId) {
              return current
            }

            const nextAnimalId = filterAvailableAnimalId(current.animalId, nextAnimals)
            return nextAnimalId === current.animalId
              ? current
              : { ...current, animalId: nextAnimalId }
          })
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
  }, [selectedFarmId, setFilters])

  useEffect(() => {
    if (previousSelectedFarmIdRef.current === selectedFarmId) {
      return
    }

    previousSelectedFarmIdRef.current = selectedFarmId
    resetFilters()
  }, [resetFilters, selectedFarmId])

  useEffect(() => {
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
  }, [appliedFilters, currency, selectedFarmId])

  const showCharts = !isChartsLoading && !errorMessage
  const formatChartCurrency = (value: number) => formatCurrencyValue(value, language, currency)
  const formatChartProduction = (value: number) => (
    `${formatConvertedMeasurementValue(value, language)} ${t(getMeasurementUnitShortLabelKey(productionUnit))}`
  )
  const productionSeries = analytics.productionSeries.map((point) => ({
    ...point,
    value: convertMeasurementFromBase(point.value, productionUnit),
  }))
  const productionByAnimal = analytics.productionByAnimal.map((point) => ({
    ...point,
    value: convertMeasurementFromBase(point.value, productionUnit),
  }))
  const productionTitle = appendUnitToLabel(
    t('analytics.productionTitle'),
    t(getMeasurementUnitShortLabelKey(productionUnit)),
  )
  const productionByAnimalTitle = appendUnitToLabel(
    t('analytics.productionByAnimalTitle'),
    t(getMeasurementUnitShortLabelKey(productionUnit)),
  )

  function renderChartEmptyState() {
    return <p className="analytics-chart__empty">{t('analytics.emptyState')}</p>
  }

  function updateFilter<Key extends keyof AnalyticsFilters>(key: Key, value: AnalyticsFilters[Key]) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [key]: value,
    }))
  }

  function clearFilters() {
    resetFilters()
  }

  async function handleExport(
    key: 'production' | 'feeding' | 'profit' | 'productionByAnimal',
  ) {
    if (!selectedFarmId) {
      return
    }

    setExportingKey(key)
    setChartsErrorMessage('')

    try {
      if (key === 'production') {
        await exportAnalyticsProductionCsv(appliedFilters, selectedFarmId, currency, productionUnit)
      } else if (key === 'feeding') {
        await exportAnalyticsFeedingCsv(appliedFilters, selectedFarmId, currency)
      } else if (key === 'profit') {
        await exportAnalyticsProfitCsv(appliedFilters, selectedFarmId, currency, productionUnit)
      } else {
        await exportAnalyticsProductionByAnimalCsv(appliedFilters, selectedFarmId, currency, productionUnit)
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

          <ListingFiltersBar
            onClear={clearFilters}
            clearLabel={t('analytics.clearFilters')}
            filters={[
              {
                type: 'date',
                id: 'analytics-start-date',
                label: t('analytics.startDateLabel'),
                value: filters.startDate,
                onChange: (value) => updateFilter('startDate', value),
              },
              {
                type: 'date',
                id: 'analytics-end-date',
                label: t('analytics.endDateLabel'),
                value: filters.endDate,
                onChange: (value) => updateFilter('endDate', value),
              },
              {
                type: 'select',
                id: 'analytics-animal-select',
                label: t('analytics.animalLabel'),
                value: filters.animalId,
                disabled: isAnimalsLoading,
                onChange: (value) => updateFilter('animalId', value),
                options: [
                  { value: '', label: t('analytics.allAnimals') },
                  ...animals.map((animal) => ({ value: animal.id, label: animal.tag })),
                ],
              },
              {
                type: 'select',
                id: 'analytics-group-by',
                label: t('analytics.groupByLabel'),
                value: filters.groupBy,
                onChange: (value) => updateFilter('groupBy', value as AnalyticsFilters['groupBy']),
                options: [
                  { value: 'day', label: t('analytics.groupByDay') },
                  { value: 'month', label: t('analytics.groupByMonth') },
                ],
              },
              {
                type: 'checkbox',
                id: 'analytics-include-acquisition-cost',
                label: t('analytics.includeAcquisitionCost'),
                checked: filters.includeAcquisitionCost,
                onChange: (checked) => updateFilter('includeAcquisitionCost', checked),
              },
            ]}
          />

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
                <h2>{productionTitle}</h2>
                <p>{t('analytics.productionDescription')}</p>
              </div>
              <ExportCsvButton
                onClick={() => void handleExport('production')}
                label={t('common.exportCsv')}
                loadingLabel={t('common.exportingCsv')}
                isLoading={exportingKey === 'production'}
                disabled={!showCharts || productionSeries.length === 0}
              />
            </div>

            {showCharts && productionSeries.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <LineChart
                  data={productionSeries}
                  color="#2e6a46"
                  valueFormatter={formatChartProduction}
                />
              </ChartErrorBoundary>
            )}

            {!isChartsLoading && !errorMessage && productionSeries.length === 0 && (
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

            {showCharts && analytics.feedingCostSeries.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <LineChart
                  data={analytics.feedingCostSeries}
                  color="#c26b2c"
                  valueFormatter={formatChartCurrency}
                />
              </ChartErrorBoundary>
            )}

            {!isChartsLoading && !errorMessage && analytics.feedingCostSeries.length === 0 && (
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

            {showCharts && analytics.profitSeries.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <LineChart
                  data={analytics.profitSeries}
                  color="#2e5b9a"
                  valueFormatter={formatChartCurrency}
                />
              </ChartErrorBoundary>
            )}

            {!isChartsLoading && !errorMessage && analytics.profitSeries.length === 0 && (
              renderChartEmptyState()
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <div className="analytics-chart__header-copy">
                <h2>{productionByAnimalTitle}</h2>
                <p>{t('analytics.productionByAnimalDescription')}</p>
              </div>
              <ExportCsvButton
                onClick={() => void handleExport('productionByAnimal')}
                label={t('common.exportCsv')}
                loadingLabel={t('common.exportingCsv')}
                isLoading={exportingKey === 'productionByAnimal'}
                disabled={!showCharts || productionByAnimal.length === 0}
              />
            </div>

            {showCharts && productionByAnimal.length > 0 && (
              <ChartErrorBoundary fallback={renderChartEmptyState()}>
                <BarChart
                  data={productionByAnimal}
                  color="#7b8f2a"
                  valueFormatter={formatChartProduction}
                />
              </ChartErrorBoundary>
            )}

            {!isChartsLoading && !errorMessage && productionByAnimal.length === 0 && (
              renderChartEmptyState()
            )}
          </article>
        </section>
      </section>
    </main>
  )
}

export default AnalyticsPage
