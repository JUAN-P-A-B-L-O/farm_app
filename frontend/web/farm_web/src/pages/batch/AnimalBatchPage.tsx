import { useEffect, useRef, useState } from 'react'
import axios from 'axios'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import { useAutoAppliedFilters } from '../../hooks/useAutoAppliedFilters'
import PaginationControls from '../../components/common/PaginationControls'
import AnimalBatchForm from '../../components/batch/AnimalBatchForm'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import {
  createAnimalBatch,
  deleteAnimalBatch,
  getAnimalBatchesPage,
  updateAnimalBatch,
} from '../../services/animalBatchService'
import type { Animal } from '../../types/animal'
import type {
  AnimalBatch,
  AnimalBatchApiErrorResponse,
  AnimalBatchFormData,
  AnimalBatchListFilters,
} from '../../types/animalBatch'
import { createEmptyPaginatedResponse, DEFAULT_PAGE_SIZE } from '../../utils/pagination'
import { isManager } from '../../utils/authorization'
import '../../App.css'

const emptyBatchForm: AnimalBatchFormData = {
  name: '',
  animalIds: [],
}

const defaultFilters: AnimalBatchListFilters = {
  search: '',
}

const debouncedBatchFilterKeys: Array<keyof AnimalBatchListFilters> = ['search']

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<AnimalBatchApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? t('batches.errors.validationSave')
    }

    if (status === 404) {
      return apiMessage ?? t('batches.errors.notFound')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function AnimalBatchPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const { selectedFarmId } = useFarm()
  const canDeleteResources = isManager(user)
  const [batches, setBatches] = useState<AnimalBatch[]>([])
  const [animals, setAnimals] = useState<Animal[]>([])
  const [pagination, setPagination] = useState(createEmptyPaginatedResponse<AnimalBatch>())
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [isLoading, setIsLoading] = useState(true)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingBatchId, setEditingBatchId] = useState<string | null>(null)
  const [formInitialValues, setFormInitialValues] = useState<AnimalBatchFormData>(emptyBatchForm)
  const previousSelectedFarmIdRef = useRef(selectedFarmId)
  const { filters, appliedFilters, setFilters, resetFilters } = useAutoAppliedFilters(defaultFilters, {
    debounceKeys: debouncedBatchFilterKeys,
    onAppliedChange: (nextFilters) => {
      setPage(0)
      void loadBatches(nextFilters, 0, pageSize)
    },
  })

  async function loadBatches(
    nextFilters: AnimalBatchListFilters = appliedFilters,
    targetPage = page,
    targetSize = pageSize,
  ) {
    if (!selectedFarmId) {
      setBatches([])
      setPagination(createEmptyPaginatedResponse<AnimalBatch>(targetSize))
      setPage(0)
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAnimalBatchesPage(selectedFarmId, { page: targetPage, size: targetSize }, nextFilters)

      if (data.content.length === 0 && data.totalElements > 0 && data.totalPages > 0 && targetPage >= data.totalPages) {
        await loadBatches(nextFilters, data.totalPages - 1, targetSize)
        return
      }

      setBatches(data.content)
      setPagination(data)
      setPage(data.page)
      setPageSize(data.size)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('batches.errors.loadList'), t))
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
      setAnimals(data.filter((animal) => animal.status === 'ACTIVE'))
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('batches.errors.loadAnimals'), t))
    } finally {
      setIsAnimalsLoading(false)
    }
  }

  useEffect(() => {
    void loadAnimals()
  }, [selectedFarmId])

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

    void loadBatches(appliedFilters, nextPage, pageSize)
  }

  function handlePageSizeChange(nextSize: number) {
    setPage(0)
    setPageSize(nextSize)
    void loadBatches(appliedFilters, 0, nextSize)
  }

  async function handleCreateOrUpdate(data: AnimalBatchFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingBatchId) {
        await updateAnimalBatch(editingBatchId, data, selectedFarmId)
      } else {
        await createAnimalBatch(data, selectedFarmId)
      }

      setEditingBatchId(null)
      setFormInitialValues(emptyBatchForm)
      await loadBatches()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingBatchId ? t('batches.errors.update') : t('batches.errors.create'),
          t,
        ),
      )
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleEdit(batch: AnimalBatch) {
    setEditingBatchId(batch.id)
    setFormErrorMessage('')
    setFormInitialValues({
      name: batch.name,
      animalIds: batch.animals.map((animal) => animal.id),
    })
  }

  function handleCancelEdit() {
    setEditingBatchId(null)
    setFormErrorMessage('')
    setFormInitialValues(emptyBatchForm)
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm(t('batches.confirmDelete'))
    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteAnimalBatch(id, selectedFarmId)

      if (editingBatchId === id) {
        handleCancelEdit()
      }

      await loadBatches()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('batches.errors.delete'), t))
    } finally {
      setIsDeletingId(null)
    }
  }

  function clearFilters() {
    setPage(0)
    resetFilters()
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('batches.eyebrow')}</p>
        <h1>{t('batches.title')}</h1>
        <p className="animals-page__description">{t('batches.description')}</p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingBatchId ? t('batches.updateTitle') : t('batches.createTitle')}</h2>
              <p>{editingBatchId ? t('batches.updateDescription') : t('batches.createDescription')}</p>
            </div>
          </div>

          {isAnimalsLoading && <p className="animals-page__status">{t('batches.loadingAnimals')}</p>}

          {!isAnimalsLoading && animals.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">{t('batches.emptyAnimals')}</p>
          )}

          {!isAnimalsLoading && (
            <AnimalBatchForm
              initialValues={formInitialValues}
              animals={animals}
              onSubmit={handleCreateOrUpdate}
              onCancel={editingBatchId ? handleCancelEdit : undefined}
              isSubmitting={isSubmitting}
              submitLabel={editingBatchId ? t('batches.submitUpdate') : t('batches.submitCreate')}
              errorMessage={formErrorMessage}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>{t('batches.listTitle')}</h2>
              <p>{t('batches.listDescription')}</p>
            </div>
          </div>

          <ListingFiltersBar
            search={{
              id: 'batch-search',
              label: t('batches.filters.searchLabel'),
              placeholder: t('batches.filters.searchPlaceholder'),
              value: filters.search,
              onChange: (value) => setFilters((current) => ({ ...current, search: value })),
            }}
            onClear={clearFilters}
            clearLabel={t('batches.filters.clear')}
          />

          {isLoading && <p className="animals-page__status">{t('batches.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && batches.length === 0 && (
            <p className="animals-page__status">{t('batches.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && batches.length > 0 && (
            <>
              <div className="animals-table-wrapper">
                <table className="animals-table">
                  <thead>
                    <tr>
                      <th>{t('batches.table.name')}</th>
                      <th>{t('batches.table.animals')}</th>
                      <th>{t('batches.table.count')}</th>
                      <th>{t('batches.table.actions')}</th>
                    </tr>
                  </thead>
                  <tbody>
                    {batches.map((batch) => (
                      <tr key={batch.id}>
                        <td>{batch.name}</td>
                        <td>{batch.animals.map((animal) => animal.tag).join(', ')}</td>
                        <td>{batch.animals.length}</td>
                        <td className="animals-table__actions">
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--secondary"
                            onClick={() => handleEdit(batch)}
                            disabled={isSubmitting || isDeletingId === batch.id}
                          >
                            {t('batches.edit')}
                          </button>
                          {canDeleteResources && (
                            <button
                              type="button"
                              className="animals-table__action-button animals-table__action-button--danger"
                              onClick={() => void handleDelete(batch.id)}
                              disabled={isDeletingId === batch.id}
                            >
                              {isDeletingId === batch.id ? t('batches.deleting') : t('batches.delete')}
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

export default AnimalBatchPage
