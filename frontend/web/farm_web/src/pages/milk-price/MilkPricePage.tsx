import { useEffect, useRef, useState, type FormEvent } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import { useAutoAppliedFilters } from '../../hooks/useAutoAppliedFilters'
import PaginationControls from '../../components/common/PaginationControls'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import {
  createMilkPrice,
  exportMilkPriceHistoryCsv,
  getCurrentMilkPrice,
  getMilkPriceHistoryPage,
} from '../../services/milkPriceService'
import { appendCurrencyCode, formatDisplayMoney } from '../../utils/currency'
import type {
  CreateMilkPricePayload,
  MilkPrice,
  MilkPriceApiErrorResponse,
  MilkPriceListFilters,
} from '../../types/milkPrice'
import { createEmptyPaginatedResponse, DEFAULT_PAGE_SIZE } from '../../utils/pagination'
import '../../App.css'

const emptyForm: CreateMilkPricePayload = {
  price: 0,
  effectiveDate: '',
}

const defaultFilters: MilkPriceListFilters = {
  search: '',
  effectiveDate: '',
}

const debouncedMilkPriceFilterKeys: Array<keyof MilkPriceListFilters> = ['search']

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<MilkPriceApiErrorResponse>(error)) {
    return error.response?.data?.error ?? fallbackMessage
  }

  return fallbackMessage
}

function MilkPricePage() {
  const { selectedFarmId } = useFarm()
  const { t, language } = useTranslation()
  const { currency } = useCurrency()
  const [currentPrice, setCurrentPrice] = useState<MilkPrice | null>(null)
  const [history, setHistory] = useState<MilkPrice[]>([])
  const [pagination, setPagination] = useState(createEmptyPaginatedResponse<MilkPrice>())
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [formData, setFormData] = useState<CreateMilkPricePayload>(emptyForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isExporting, setIsExporting] = useState(false)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const previousSelectedFarmIdRef = useRef(selectedFarmId)
  const { filters, appliedFilters, setFilters, resetFilters } = useAutoAppliedFilters(defaultFilters, {
    debounceKeys: debouncedMilkPriceFilterKeys,
    onAppliedChange: (nextFilters) => {
      setPage(0)
      void loadMilkPrices(nextFilters, 0, pageSize)
    },
  })

  async function loadMilkPrices(
    nextFilters: MilkPriceListFilters = appliedFilters,
    targetPage = page,
    targetSize = pageSize,
  ) {
    if (!selectedFarmId) {
      setCurrentPrice(null)
      setHistory([])
      setPagination(createEmptyPaginatedResponse<MilkPrice>(targetSize))
      setPage(0)
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const [current, priceHistory] = await Promise.all([
        getCurrentMilkPrice(selectedFarmId),
        getMilkPriceHistoryPage(selectedFarmId, { page: targetPage, size: targetSize }, nextFilters),
      ])

      setCurrentPrice(current)
      if (
        priceHistory.content.length === 0 &&
        priceHistory.totalElements > 0 &&
        priceHistory.totalPages > 0 &&
        targetPage >= priceHistory.totalPages
      ) {
        await loadMilkPrices(nextFilters, priceHistory.totalPages - 1, targetSize)
        return
      }

      setHistory(priceHistory.content)
      setPagination(priceHistory)
      setPage(priceHistory.page)
      setPageSize(priceHistory.size)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('milkPrice.errors.load')))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    if (previousSelectedFarmIdRef.current === selectedFarmId) {
      return
    }

    previousSelectedFarmIdRef.current = selectedFarmId
    setPage(0)
    resetFilters()
  }, [resetFilters, selectedFarmId])

  function handlePageChange(nextPage: number) {
    if (nextPage === page) {
      return
    }

    void loadMilkPrices(appliedFilters, nextPage, pageSize)
  }

  function handlePageSizeChange(nextSize: number) {
    setPage(0)
    setPageSize(nextSize)
    void loadMilkPrices(appliedFilters, 0, nextSize)
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!selectedFarmId) {
      setFormErrorMessage(t('milkPrice.errors.selectFarm'))
      return
    }

    if (!formData.effectiveDate || !Number.isFinite(formData.price) || formData.price <= 0) {
      setFormErrorMessage(t('milkPrice.errors.invalid'))
      return
    }

    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      await createMilkPrice(formData, selectedFarmId)
      setFormData(emptyForm)
      await loadMilkPrices()
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('milkPrice.errors.create')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)
    setListErrorMessage('')

    try {
      await exportMilkPriceHistoryCsv(selectedFarmId, currency, appliedFilters)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError')))
    } finally {
      setIsExporting(false)
    }
  }

  function clearFilters() {
    setPage(0)
    resetFilters()
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('milkPrice.eyebrow')}</p>
        <h1>{t('milkPrice.title')}</h1>
        <p className="animals-page__description">{t('milkPrice.description')}</p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('milkPrice.currentTitle')}</h2>
              <p>{t('milkPrice.currentDescription')}</p>
            </div>
          </div>

          <ListingFiltersBar
            search={{
              id: 'milk-price-search',
              label: t('milkPrice.filters.searchLabel'),
              placeholder: t('milkPrice.filters.searchPlaceholder'),
              value: filters.search,
              onChange: (value) => setFilters((current) => ({ ...current, search: value })),
            }}
            onClear={clearFilters}
            clearLabel={t('milkPrice.filters.clear')}
            filters={[
              {
                type: 'date',
                id: 'milk-price-effective-date-filter',
                label: t('milkPrice.filters.effectiveDateLabel'),
                value: filters.effectiveDate,
                onChange: (value) => setFilters((current) => ({ ...current, effectiveDate: value })),
                max: new Date().toISOString().slice(0, 10),
              },
            ]}
          />

          {isLoading && <p className="animals-page__status">{t('milkPrice.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">{listErrorMessage}</p>
          )}

          {!isLoading && !listErrorMessage && currentPrice && (
            <>
              <dl className="animal-details-grid">
                <div className="animal-details-grid__item">
                  <dt>{appendCurrencyCode(t('milkPrice.summary.currentPrice'), currency)}</dt>
                  <dd>{formatDisplayMoney(currentPrice.price, language, currency)}</dd>
                </div>
                <div className="animal-details-grid__item">
                  <dt>{t('milkPrice.summary.effectiveDate')}</dt>
                  <dd>{currentPrice.effectiveDate ?? t('milkPrice.summary.defaultFallback')}</dd>
                </div>
              </dl>

              {currentPrice.fallbackDefault && (
                <p className="animals-page__status">{t('milkPrice.fallbackNotice')}</p>
              )}
            </>
          )}

          <div className="animals-panel__header" style={{ marginTop: 24 }}>
            <div>
              <h2>{t('milkPrice.createTitle')}</h2>
              <p>{t('milkPrice.createDescription')}</p>
            </div>
          </div>

          <form className="animal-form" onSubmit={handleSubmit}>
            <div className="animal-form__grid">
              <label className="animal-form__field" htmlFor="milk-price-value">
                <span>{t('milkPrice.form.price')}</span>
                <input
                  id="milk-price-value"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={formData.price || ''}
                  onChange={(event) =>
                    setFormData((current) => ({ ...current, price: Number(event.target.value) }))
                  }
                />
              </label>

              <label className="animal-form__field" htmlFor="milk-price-effective-date">
                <span>{t('milkPrice.form.effectiveDate')}</span>
                <input
                  id="milk-price-effective-date"
                  type="date"
                  value={formData.effectiveDate}
                  onChange={(event) =>
                    setFormData((current) => ({ ...current, effectiveDate: event.target.value }))
                  }
                />
              </label>
            </div>

            {formErrorMessage && (
              <p className="animal-form__feedback animal-form__feedback--error">{formErrorMessage}</p>
            )}

            <div className="animal-form__actions">
              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? t('milkPrice.submitting') : t('milkPrice.submit')}
              </button>
            </div>
          </form>
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('milkPrice.historyTitle')}</h2>
              <p>{t('milkPrice.historyDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={!selectedFarmId || isLoading || history.length === 0}
            />
          </div>

          {isLoading && <p className="animals-page__status">{t('milkPrice.loading')}</p>}

          {!isLoading && !listErrorMessage && history.length === 0 && (
            <p className="animals-page__status">{t('milkPrice.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && history.length > 0 && (
            <>
              <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{appendCurrencyCode(t('milkPrice.table.price'), currency)}</th>
                    <th>{t('milkPrice.table.effectiveDate')}</th>
                    <th>{t('milkPrice.table.createdAt')}</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((entry) => (
                    <tr key={entry.id ?? `${entry.effectiveDate}-${entry.price}`}>
                      <td>{formatDisplayMoney(entry.price, language, currency)}</td>
                      <td>{entry.effectiveDate ?? '-'}</td>
                      <td>{entry.createdAt ? entry.createdAt.replace('T', ' ') : '-'}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
              </div>

              <PaginationControls
                pagination={pagination}
                isLoading={isLoading}
                onPageChange={handlePageChange}
                onPageSizeChange={handlePageSizeChange}
              />
            </>
          )}
        </article>
      </section>
    </main>
  )
}

export default MilkPricePage
