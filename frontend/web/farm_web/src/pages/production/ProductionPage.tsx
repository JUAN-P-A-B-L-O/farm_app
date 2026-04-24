import { useEffect, useState } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import PaginationControls from '../../components/common/PaginationControls'
import ProductionForm from '../../components/production/ProductionForm'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import {
  createProduction,
  deleteProduction,
  exportProductionsCsv,
  getProductionsPage,
  getProductionById,
  updateProduction,
} from '../../services/productionService'
import type { Animal } from '../../types/animal'
import type {
  Production,
  ProductionApiErrorResponse,
  ProductionAnimalOption,
  ProductionFormData,
  ProductionListFilters,
} from '../../types/production'
import { createEmptyPaginatedResponse, DEFAULT_PAGE_SIZE } from '../../utils/pagination'
import { isManager } from '../../utils/authorization'
import '../../App.css'

const emptyProductionForm: ProductionFormData = {
  animalId: '',
  date: '',
  quantity: 0,
  userId: '',
}

const defaultFilters: ProductionListFilters = {
  search: '',
  animalId: '',
  date: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<ProductionApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? t('production.errors.validationSave')
    }

    if (status === 404) {
      return apiMessage ?? t('production.errors.animalNotFound')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function mapAnimalsToOptions(animals: Animal[]): ProductionAnimalOption[] {
  return animals
    .filter((animal) => animal.status === 'ACTIVE')
    .map(({ id, tag }) => ({
      id,
      tag,
    }))
}

function ProductionPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const { selectedFarmId } = useFarm()
  const canSelectCreateDate = isManager(user)
  const canDeleteResources = isManager(user)
  const [productions, setProductions] = useState<Production[]>([])
  const [pagination, setPagination] = useState(createEmptyPaginatedResponse<Production>())
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [animals, setAnimals] = useState<ProductionAnimalOption[]>([])
  const [formInitialValues, setFormInitialValues] = useState<ProductionFormData>(emptyProductionForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingProductionId, setEditingProductionId] = useState<string | null>(null)
  const [isExporting, setIsExporting] = useState(false)
  const [filters, setFilters] = useState<ProductionListFilters>(defaultFilters)
  const [appliedFilters, setAppliedFilters] = useState<ProductionListFilters>(defaultFilters)

  async function loadProductions(
    nextFilters: ProductionListFilters = appliedFilters,
    targetPage = page,
    targetSize = pageSize,
  ) {
    if (!selectedFarmId) {
      setProductions([])
      setPagination(createEmptyPaginatedResponse<Production>(targetSize))
      setPage(0)
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getProductionsPage(selectedFarmId, { page: targetPage, size: targetSize }, nextFilters)

      if (data.content.length === 0 && data.totalElements > 0 && data.totalPages > 0 && targetPage >= data.totalPages) {
        await loadProductions(nextFilters, data.totalPages - 1, targetSize)
        return
      }

      setProductions(data.content)
      setPagination(data)
      setPage(data.page)
      setPageSize(data.size)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('production.errors.loadRecords'), t))
    } finally {
      setIsLoading(false)
    }
  }

  async function loadAnimals() {
    if (!selectedFarmId) {
      setAnimals([])
      setFormErrorMessage('')
      setIsAnimalsLoading(false)
      return
    }

    setIsAnimalsLoading(true)
    setFormErrorMessage('')

    try {
      const data = await getAllAnimals(selectedFarmId)
      setAnimals(mapAnimalsToOptions(data))
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('production.errors.loadAnimals'), t))
    } finally {
      setIsAnimalsLoading(false)
    }
  }

  useEffect(() => {
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    void Promise.all([loadProductions(defaultFilters), loadAnimals()])
  }, [selectedFarmId])

  function handlePageChange(nextPage: number) {
    if (nextPage === page) {
      return
    }

    void loadProductions(appliedFilters, nextPage, pageSize)
  }

  function handlePageSizeChange(nextSize: number) {
    setPage(0)
    setPageSize(nextSize)
    void loadProductions(appliedFilters, 0, nextSize)
  }

  async function handleCreateOrUpdateProduction(data: ProductionFormData) {
    const requiresDate = editingProductionId !== null || canSelectCreateDate
    const payload: ProductionFormData = {
      animalId: data.animalId.trim(),
      date: data.date,
      quantity: Number(data.quantity),
      userId: data.userId.trim(),
    }

    if (
      !payload.animalId ||
      (requiresDate && !payload.date) ||
      !Number.isFinite(payload.quantity) ||
      payload.quantity <= 0 ||
      (!editingProductionId && !payload.userId)
    ) {
      setFormErrorMessage(t('production.errors.missingFields'))
      return
    }

    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingProductionId) {
        await updateProduction(editingProductionId, payload, selectedFarmId)
      } else {
        await createProduction(payload, selectedFarmId)
      }

      setEditingProductionId(null)
      setFormInitialValues({ ...emptyProductionForm })
      await loadProductions()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingProductionId ? t('production.errors.update') : t('production.errors.create'),
          t,
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleEdit(id: string) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      const production = await getProductionById(id, selectedFarmId)

      setEditingProductionId(production.id)
      setFormInitialValues({
        animalId: production.animalId,
        date: production.date,
        quantity: production.quantity,
        userId: '',
      })
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('production.errors.loadDetails'), t))
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleCancelEdit() {
    setEditingProductionId(null)
    setFormErrorMessage('')
    setFormInitialValues({ ...emptyProductionForm })
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm(t('production.confirmDelete'))

    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteProduction(id, selectedFarmId)

      if (editingProductionId === id) {
        handleCancelEdit()
      }

      await loadProductions()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('production.errors.delete'), t))
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
      await exportProductionsCsv(selectedFarmId, appliedFilters)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError'), t))
    } finally {
      setIsExporting(false)
    }
  }

  function applyFilters() {
    setAppliedFilters(filters)
    setPage(0)
    void loadProductions(filters, 0, pageSize)
  }

  function clearFilters() {
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    setPage(0)
    void loadProductions(defaultFilters, 0, pageSize)
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('production.eyebrow')}</p>
        <h1>{t('production.title')}</h1>
        <p className="animals-page__description">
          {t('production.description')}
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingProductionId ? t('production.updateTitle') : t('production.createTitle')}</h2>
              <p>
                {editingProductionId
                  ? t('production.updateDescription')
                  : t('production.createDescription')}
              </p>
            </div>
          </div>

          {isAnimalsLoading && <p className="animals-page__status">{t('production.loadingAnimals')}</p>}

          {!isAnimalsLoading && animals.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">{t('production.emptyAnimals')}</p>
          )}

          {!isAnimalsLoading && (
            <ProductionForm
              initialValues={formInitialValues}
              animals={animals}
              onSubmit={handleCreateOrUpdateProduction}
              onCancel={editingProductionId ? handleCancelEdit : undefined}
              isSubmitting={isSubmitting}
              submitLabel={editingProductionId ? t('production.submitUpdate') : t('production.submitCreate')}
              errorMessage={formErrorMessage}
              requireUserSelection={!editingProductionId}
              allowDateSelection={editingProductionId !== null || canSelectCreateDate}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('production.listTitle')}</h2>
              <p>{t('production.listDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={!selectedFarmId || isLoading || productions.length === 0}
            />
          </div>

          <ListingFiltersBar
            searchId="production-search"
            searchLabel={t('production.filters.searchLabel')}
            searchPlaceholder={t('production.filters.searchPlaceholder')}
            searchValue={filters.search}
            onSearchChange={(value) => setFilters((current) => ({ ...current, search: value }))}
            onApply={applyFilters}
            onClear={clearFilters}
            applyLabel={t('production.filters.apply')}
            clearLabel={t('production.filters.clear')}
            filters={[
              {
                id: 'production-animal-filter',
                label: t('production.filters.animalLabel'),
                value: filters.animalId,
                onChange: (value) => setFilters((current) => ({ ...current, animalId: value })),
                options: [
                  { value: '', label: t('production.filters.allAnimals') },
                  ...animals.map((animal) => ({ value: animal.id, label: animal.tag })),
                ],
              },
              {
                id: 'production-date-filter',
                label: t('production.filters.dateLabel'),
                value: filters.date,
                onChange: (value) => setFilters((current) => ({ ...current, date: value })),
                max: new Date().toISOString().slice(0, 10),
              },
            ]}
          />

          {isLoading && <p className="animals-page__status">{t('production.loadingRecords')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && productions.length === 0 && (
            <p className="animals-page__status">{t('production.emptyRecords')}</p>
          )}

          {!isLoading && !listErrorMessage && productions.length > 0 && (
            <>
              <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('production.table.animalTag')}</th>
                    <th>{t('production.table.date')}</th>
                    <th>{t('production.table.quantity')}</th>
                    <th>{t('production.table.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {productions.map((production) => (
                    <tr key={production.id}>
                      <td>{production.animal?.tag}</td>
                      <td>{production.date}</td>
                      <td>{production.quantity}</td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => void handleEdit(production.id)}
                          disabled={isSubmitting || isDeletingId === production.id}
                        >
                          {t('production.edit')}
                        </button>
                        {canDeleteResources && (
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--danger"
                            onClick={() => void handleDelete(production.id)}
                            disabled={isDeletingId === production.id}
                          >
                            {isDeletingId === production.id
                              ? t('production.deleting')
                              : t('production.delete')}
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

export default ProductionPage
