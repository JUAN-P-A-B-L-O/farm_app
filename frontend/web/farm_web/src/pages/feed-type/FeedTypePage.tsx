import { useEffect, useState } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import FeedTypeForm from '../../components/feed-type/FeedTypeForm'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import PaginationControls from '../../components/common/PaginationControls'
import { useAuth } from '../../hooks/useAuth'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useMeasurementUnits } from '../../hooks/useMeasurementUnits'
import { useTranslation } from '../../hooks/useTranslation'
import {
  createFeedType,
  deleteFeedType,
  exportFeedTypesCsv,
  getFeedTypesPage,
  updateFeedType,
} from '../../services/feedTypeService'
import type { FeedType, FeedTypeApiErrorResponse, FeedTypeFormData, FeedTypeListFilters } from '../../types/feedType'
import { createEmptyPaginatedResponse, DEFAULT_PAGE_SIZE } from '../../utils/pagination'
import { appendCurrencyCode, formatDisplayMoney } from '../../utils/currency'
import { isManager } from '../../utils/authorization'
import {
  convertFeedCostFromBase,
  getMeasurementUnitShortLabelKey,
} from '../../utils/measurementUnits'
import '../../App.css'

const emptyFeedTypeForm: FeedTypeFormData = {
  name: '',
  costPerKg: 0,
}

const defaultFilters: FeedTypeListFilters = {
  search: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<FeedTypeApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? t('feedType.errors.validationSave')
    }

    if (status === 404) {
      return apiMessage ?? t('feedType.errors.notFound')
    }

    if (status === 409) {
      return apiMessage ?? t('feedType.errors.duplicateName')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function FeedTypePage() {
  const { t, language } = useTranslation()
  const { currency } = useCurrency()
  const { user } = useAuth()
  const { selectedFarmId } = useFarm()
  const { feedingUnit } = useMeasurementUnits()
  const canDeleteResources = isManager(user)
  const [feedTypes, setFeedTypes] = useState<FeedType[]>([])
  const [pagination, setPagination] = useState(createEmptyPaginatedResponse<FeedType>())
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingFeedTypeId, setEditingFeedTypeId] = useState<string | null>(null)
  const [formInitialValues, setFormInitialValues] = useState<FeedTypeFormData>(emptyFeedTypeForm)
  const [isExporting, setIsExporting] = useState(false)
  const [filters, setFilters] = useState<FeedTypeListFilters>(defaultFilters)
  const [appliedFilters, setAppliedFilters] = useState<FeedTypeListFilters>(defaultFilters)

  async function loadFeedTypes(
    nextFilters: FeedTypeListFilters = appliedFilters,
    targetPage = page,
    targetSize = pageSize,
  ) {
    if (!selectedFarmId) {
      setFeedTypes([])
      setPagination(createEmptyPaginatedResponse<FeedType>(targetSize))
      setPage(0)
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getFeedTypesPage(selectedFarmId, { page: targetPage, size: targetSize }, nextFilters)

      if (data.content.length === 0 && data.totalElements > 0 && data.totalPages > 0 && targetPage >= data.totalPages) {
        await loadFeedTypes(nextFilters, data.totalPages - 1, targetSize)
        return
      }

      setFeedTypes(data.content)
      setPagination(data)
      setPage(data.page)
      setPageSize(data.size)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('feedType.errors.loadList'), t))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    setPage(0)
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    void loadFeedTypes(defaultFilters, 0, pageSize)
  }, [selectedFarmId])

  function handlePageChange(nextPage: number) {
    if (nextPage === page) {
      return
    }

    void loadFeedTypes(appliedFilters, nextPage, pageSize)
  }

  function handlePageSizeChange(nextSize: number) {
    setPage(0)
    setPageSize(nextSize)
    void loadFeedTypes(appliedFilters, 0, nextSize)
  }

  async function handleCreateOrUpdate(data: FeedTypeFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingFeedTypeId) {
        await updateFeedType(editingFeedTypeId, data, selectedFarmId)
      } else {
        await createFeedType(data, selectedFarmId)
      }

      setEditingFeedTypeId(null)
      setFormInitialValues(emptyFeedTypeForm)
      await loadFeedTypes()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingFeedTypeId ? t('feedType.errors.update') : t('feedType.errors.create'),
          t,
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleEdit(feedType: FeedType) {
    setFormErrorMessage('')
    setEditingFeedTypeId(feedType.id)
    setFormInitialValues({
      name: feedType.name,
      costPerKg: feedType.costPerKg,
    })
  }

  function handleCancelEdit() {
    setEditingFeedTypeId(null)
    setFormErrorMessage('')
    setFormInitialValues(emptyFeedTypeForm)
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm(t('feedType.confirmDelete'))

    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteFeedType(id, selectedFarmId)

      if (editingFeedTypeId === id) {
        handleCancelEdit()
      }

      await loadFeedTypes()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('feedType.errors.delete'), t))
    } finally {
      setIsDeletingId(null)
    }
  }

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)
    setListErrorMessage('')

    try {
      await exportFeedTypesCsv(selectedFarmId, currency, appliedFilters, feedingUnit)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError'), t))
    } finally {
      setIsExporting(false)
    }
  }

  function applyFilters() {
    setAppliedFilters(filters)
    setPage(0)
    void loadFeedTypes(filters, 0, pageSize)
  }

  function clearFilters() {
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    setPage(0)
    void loadFeedTypes(defaultFilters, 0, pageSize)
  }

  const unitLabel = t(getMeasurementUnitShortLabelKey(feedingUnit))
  const costLabel = `${appendCurrencyCode(t('feedType.table.costPerKg'), currency)} / ${unitLabel}`
  const currencyFormatOptions = feedingUnit === 'GRAM'
    ? { minimumFractionDigits: 5, maximumFractionDigits: 5, conversionPrecision: 5 }
    : { minimumFractionDigits: 2, maximumFractionDigits: 2, conversionPrecision: 2 }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('feedType.eyebrow')}</p>
        <h1>{t('feedType.title')}</h1>
        <p className="animals-page__description">
          {t('feedType.description')}
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingFeedTypeId ? t('feedType.updateTitle') : t('feedType.createTitle')}</h2>
              <p>
                {editingFeedTypeId
                  ? t('feedType.updateDescription')
                  : t('feedType.createDescription')}
              </p>
            </div>
          </div>

          <FeedTypeForm
            key={editingFeedTypeId ?? 'new'}
            initialValues={formInitialValues}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingFeedTypeId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingFeedTypeId ? t('feedType.submitUpdate') : t('feedType.submitCreate')}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('feedType.listTitle')}</h2>
              <p>{t('feedType.listDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={!selectedFarmId || isLoading || feedTypes.length === 0}
            />
          </div>

          <ListingFiltersBar
            search={{
              id: 'feed-type-search',
              label: t('feedType.filters.searchLabel'),
              placeholder: t('feedType.filters.searchPlaceholder'),
              value: filters.search,
              onChange: (value) => setFilters({ search: value }),
            }}
            onApply={applyFilters}
            onClear={clearFilters}
            applyLabel={t('feedType.filters.apply')}
            clearLabel={t('feedType.filters.clear')}
          />

          {isLoading && <p className="animals-page__status">{t('feedType.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && feedTypes.length === 0 && (
            <p className="animals-page__status">{t('feedType.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && feedTypes.length > 0 && (
            <>
              <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('feedType.table.name')}</th>
                    <th>{costLabel}</th>
                    <th>{t('feedType.table.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {feedTypes.map((feedType) => (
                    <tr key={feedType.id}>
                      <td>{feedType.name}</td>
                      <td>{formatDisplayMoney(
                        convertFeedCostFromBase(feedType.costPerKg, feedingUnit),
                        language,
                        currency,
                        currencyFormatOptions,
                      )}</td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => handleEdit(feedType)}
                          disabled={isSubmitting || isDeletingId === feedType.id}
                        >
                          {t('feedType.edit')}
                        </button>
                        {canDeleteResources && (
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--danger"
                            onClick={() => handleDelete(feedType.id)}
                            disabled={isSubmitting || isDeletingId === feedType.id}
                          >
                            {isDeletingId === feedType.id ? t('feedType.deleting') : t('feedType.delete')}
                          </button>
                        )}
                      </td>
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

export default FeedTypePage
