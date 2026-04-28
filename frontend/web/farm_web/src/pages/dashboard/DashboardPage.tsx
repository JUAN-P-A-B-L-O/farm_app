import { useEffect, useEffectEvent, useState } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import StatCard from '../../components/dashboard/StatCard'
import { ANIMAL_STATUSES, getAnimalStatusLabel } from '../../i18n/domainLabels'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useMeasurementUnits } from '../../hooks/useMeasurementUnits'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import { exportDashboardCsv, fetchDashboard } from '../../services/dashboardService'
import { appendCurrencyCode } from '../../utils/currency'
import {
  appendUnitToLabel,
  convertMeasurementFromBase,
  getMeasurementUnitShortLabelKey,
} from '../../utils/measurementUnits'
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

interface DashboardFilterState extends DashboardFilters {
  includeAcquisitionCost: boolean
}

function createInitialFilters(): DashboardFilterState {
  return {
    startDate: '',
    endDate: '',
    animalIds: [],
    status: '',
    includeAcquisitionCost: true,
  }
}

const animalStatusOptions: AnimalStatus[] = [...ANIMAL_STATUSES]

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

function filterAvailableAnimalIds(
  currentAnimalIds: string[],
  nextAnimals: Array<{ id: string; tag: string }>,
) {
  if (currentAnimalIds.length === 0) {
    return currentAnimalIds
  }

  const availableAnimalIds = new Set(nextAnimals.map((animal) => animal.id))
  return currentAnimalIds.filter((animalId) => availableAnimalIds.has(animalId))
}

function buildDashboardFilters(filters: DashboardFilterState): DashboardFilters {
  return {
    startDate: filters.startDate,
    endDate: filters.endDate,
    animalIds: [...filters.animalIds],
    status: filters.status,
  }
}

function DashboardPage() {
  const { t } = useTranslation()
  const { currency } = useCurrency()
  const { selectedFarmId } = useFarm()
  const { productionUnit } = useMeasurementUnits()
  const [animals, setAnimals] = useState<Array<{ id: string; tag: string }>>([])
  const [filters, setFilters] = useState<DashboardFilterState>(createInitialFilters)
  const [appliedFilters, setAppliedFilters] = useState<DashboardFilterState>(createInitialFilters)
  const [summary, setSummary] = useState<DashboardSummary | null>(null)
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
            current.animalIds.length > 0
              ? { ...current, animalIds: filterAvailableAnimalIds(current.animalIds, nextAnimals) }
              : current
          ))
          setFilters((current) => (
            current.animalIds.length > 0
              ? { ...current, animalIds: filterAvailableAnimalIds(current.animalIds, nextAnimals) }
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
        const data = await fetchDashboard(
          selectedFarmId,
          appliedFilters.includeAcquisitionCost,
          currency,
          buildDashboardFilters(appliedFilters),
        )

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
  }, [appliedFilters, currency, selectedFarmId])

  function updateFilter<Key extends keyof DashboardFilterState>(key: Key, value: DashboardFilterState[Key]) {
    setFilters((currentFilters) => ({
      ...currentFilters,
      [key]: value,
    }))
  }

  function applyFilters() {
    setAppliedFilters({
      ...filters,
      animalIds: [...filters.animalIds],
    })
  }

  function clearFilters() {
    setFilters(createInitialFilters())
    setAppliedFilters(createInitialFilters())
  }

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)
    setSummaryErrorMessage('')

    try {
      await exportDashboardCsv(
        selectedFarmId,
        appliedFilters.includeAcquisitionCost,
        currency,
        buildDashboardFilters(appliedFilters),
        productionUnit,
      )
    } catch (error) {
      setSummaryErrorMessage(getErrorMessage(error, t('common.exportError')))
    } finally {
      setIsExporting(false)
    }
  }

  const productionTitle = appendUnitToLabel(
    t('dashboard.stats.totalProduction'),
    t(getMeasurementUnitShortLabelKey(productionUnit)),
  )

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
        <ListingFiltersBar
          onApply={applyFilters}
          onClear={clearFilters}
          applyLabel={t('dashboard.filters.apply')}
          clearLabel={t('dashboard.filters.clear')}
          filters={[
            {
              type: 'date',
              id: 'dashboard-start-date',
              label: t('dashboard.filters.startDateLabel'),
              value: filters.startDate,
              onChange: (value) => updateFilter('startDate', value),
            },
            {
              type: 'date',
              id: 'dashboard-end-date',
              label: t('dashboard.filters.endDateLabel'),
              value: filters.endDate,
              onChange: (value) => updateFilter('endDate', value),
            },
            {
              type: 'multiselect',
              id: 'dashboard-animal-select',
              label: t('dashboard.filters.animalLabel'),
              value: filters.animalIds,
              disabled: isAnimalsLoading || animals.length === 0,
              helpText: t('dashboard.filters.allAnimalsHint'),
              size: Math.min(Math.max(animals.length, 2), 6),
              onChange: (value) => updateFilter('animalIds', value),
              options: animals.map((animal) => ({ value: animal.id, label: animal.tag })),
            },
            {
              type: 'select',
              id: 'dashboard-status-select',
              label: t('dashboard.filters.statusLabel'),
              value: filters.status,
              onChange: (value) => updateFilter('status', value as DashboardFilterState['status']),
              options: [
                { value: '', label: t('dashboard.filters.allStatuses') },
                ...animalStatusOptions.map((status) => ({
                  value: status,
                  label: getAnimalStatusLabel(t, status),
                })),
              ],
            },
            {
              type: 'checkbox',
              id: 'dashboard-include-acquisition-cost',
              label: t('dashboard.includeAcquisitionCost'),
              checked: filters.includeAcquisitionCost,
              onChange: (checked) => updateFilter('includeAcquisitionCost', checked),
            },
          ]}
        />
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
              title={stat.key === 'totalProduction'
                ? productionTitle
                : stat.format === 'currency'
                  ? appendCurrencyCode(t(stat.titleKey), currency)
                  : t(stat.titleKey)}
              value={stat.key === 'totalProduction'
                ? convertMeasurementFromBase(summary.totalProduction, productionUnit)
                : summary[stat.key] ?? 0}
              format={stat.format}
            />
          ))}
        </section>
      )}
    </main>
  )
}

export default DashboardPage
