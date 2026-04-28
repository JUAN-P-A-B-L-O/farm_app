import { useEffect, useState } from 'react'
import axios from 'axios'
import AnimalForm from '../../components/animal/AnimalForm'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import ListingFiltersBar from '../../components/common/ListingFiltersBar'
import PaginationControls from '../../components/common/PaginationControls'
import { ANIMAL_ORIGINS, ANIMAL_STATUSES, getAnimalOriginLabel, getAnimalStatusLabel } from '../../i18n/domainLabels'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import {
  createAnimal,
  deleteAnimal,
  exportAnimalsCsv,
  getAnimalsPage,
  getAnimalById,
  sellAnimal,
  updateAnimal,
} from '../../services/animalService'
import type { Animal, AnimalFormData, AnimalListFilters, ApiErrorResponse } from '../../types/animal'
import { createEmptyPaginatedResponse, DEFAULT_PAGE_SIZE } from '../../utils/pagination'
import { isManager } from '../../utils/authorization'
import '../../App.css'

interface AnimalsPageProps {
  onOpenDetails: (animalId: string) => void
}

const emptyAnimalForm: AnimalFormData = {
  tag: '',
  breed: '',
  birthDate: '',
  origin: 'BORN',
  acquisitionCost: null,
  farmId: '',
}

const defaultFilters: AnimalListFilters = {
  search: '',
  status: '',
  origin: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 404) {
      return apiMessage ?? t('animals.errors.notFound')
    }

    if (status === 409) {
      return apiMessage ?? t('animals.errors.duplicateTag')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function AnimalsPage({ onOpenDetails }: AnimalsPageProps) {
  const { t } = useTranslation()
  const { user } = useAuth()
  const { selectedFarmId, selectedFarm } = useFarm()
  const canDeleteResources = isManager(user)
  const [animals, setAnimals] = useState<Animal[]>([])
  const [pagination, setPagination] = useState(createEmptyPaginatedResponse<Animal>())
  const [page, setPage] = useState(0)
  const [pageSize, setPageSize] = useState(DEFAULT_PAGE_SIZE)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [isSellingId, setIsSellingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [sellErrorMessage, setSellErrorMessage] = useState('')
  const [editingAnimalId, setEditingAnimalId] = useState<string | null>(null)
  const [sellingAnimal, setSellingAnimal] = useState<Animal | null>(null)
  const [isExporting, setIsExporting] = useState(false)
  const [formInitialValues, setFormInitialValues] = useState<AnimalFormData>(emptyAnimalForm)
  const [salePrice, setSalePrice] = useState('')
  const [saleDate, setSaleDate] = useState(() => new Date().toISOString().slice(0, 10))
  const [filters, setFilters] = useState<AnimalListFilters>(defaultFilters)
  const [appliedFilters, setAppliedFilters] = useState<AnimalListFilters>(defaultFilters)

  async function loadAnimals(
    nextFilters: AnimalListFilters = appliedFilters,
    targetPage = page,
    targetSize = pageSize,
  ) {
    if (!selectedFarmId) {
      setAnimals([])
      setPagination(createEmptyPaginatedResponse<Animal>(targetSize))
      setPage(0)
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAnimalsPage(selectedFarmId, { page: targetPage, size: targetSize }, nextFilters)

      if (data.content.length === 0 && data.totalElements > 0 && data.totalPages > 0 && targetPage >= data.totalPages) {
        await loadAnimals(nextFilters, data.totalPages - 1, targetSize)
        return
      }

      setAnimals(data.content)
      setPagination(data)
      setPage(data.page)
      setPageSize(data.size)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('animals.errors.loadList'), t))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    setPage(0)
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    void loadAnimals(defaultFilters, 0, pageSize)
  }, [selectedFarmId])

  function handlePageChange(nextPage: number) {
    if (nextPage === page) {
      return
    }

    void loadAnimals(appliedFilters, nextPage, pageSize)
  }

  function handlePageSizeChange(nextSize: number) {
    setPage(0)
    setPageSize(nextSize)
    void loadAnimals(appliedFilters, 0, nextSize)
  }

  async function handleCreateOrUpdate(data: AnimalFormData) {
    const payload: AnimalFormData = {
      ...data,
      farmId: selectedFarmId,
    }

    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingAnimalId) {
        await updateAnimal(editingAnimalId, payload, selectedFarmId)
      } else {
        await createAnimal(payload)
      }

      setEditingAnimalId(null)
      setFormInitialValues(emptyAnimalForm)
      await loadAnimals()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingAnimalId ? t('animals.errors.update') : t('animals.errors.create'),
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
      const animal = await getAnimalById(id, selectedFarmId)

      setEditingAnimalId(animal.id)
      setFormInitialValues({
        tag: animal.tag,
        breed: animal.breed,
        birthDate: animal.birthDate,
        origin: animal.origin,
        acquisitionCost: animal.acquisitionCost,
        status: animal.status,
        farmId: animal.farmId,
      })
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('animals.errors.loadDetails'), t))
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleCancelEdit() {
    setEditingAnimalId(null)
    setFormErrorMessage('')
    setFormInitialValues(emptyAnimalForm)
  }

  function handleStartSell(animal: Animal) {
    setSellingAnimal(animal)
    setSellErrorMessage('')
    setSalePrice(animal.salePrice?.toString() ?? '')
    setSaleDate(animal.saleDate ?? new Date().toISOString().slice(0, 10))
  }

  function handleCancelSell() {
    setSellingAnimal(null)
    setSellErrorMessage('')
    setSalePrice('')
    setSaleDate(new Date().toISOString().slice(0, 10))
  }

  async function handleSell() {
    if (!sellingAnimal) {
      return
    }

    const normalizedPrice = Number(salePrice)

    if (!Number.isFinite(normalizedPrice) || normalizedPrice <= 0) {
      setSellErrorMessage(t('animals.errors.invalidSalePrice'))
      return
    }

    setIsSellingId(sellingAnimal.id)
    setSellErrorMessage('')

    try {
      await sellAnimal(
        sellingAnimal.id,
        {
          salePrice: normalizedPrice,
          ...(saleDate ? { saleDate } : {}),
        },
        selectedFarmId,
      )

      if (editingAnimalId === sellingAnimal.id) {
        handleCancelEdit()
      }

      handleCancelSell()
      await loadAnimals()
    } catch (error) {
      setSellErrorMessage(getErrorMessage(error, t('animals.errors.sell'), t))
    } finally {
      setIsSellingId(null)
    }
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm(t('animals.confirmDelete'))

    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteAnimal(id, selectedFarmId)

      if (editingAnimalId === id) {
        handleCancelEdit()
      }

      await loadAnimals()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('animals.errors.delete'), t))
    } finally {
      setIsDeletingId(null)
    }
  }

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)

    try {
      await exportAnimalsCsv(selectedFarmId, undefined, appliedFilters)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError'), t))
    } finally {
      setIsExporting(false)
    }
  }

  function applyFilters() {
    setAppliedFilters(filters)
    setPage(0)
    void loadAnimals(filters, 0, pageSize)
  }

  function clearFilters() {
    setFilters(defaultFilters)
    setAppliedFilters(defaultFilters)
    setPage(0)
    void loadAnimals(defaultFilters, 0, pageSize)
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('animals.eyebrow')}</p>
        <h1>{t('animals.title')}</h1>
        <p className="animals-page__description">
          {t('animals.description')}
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingAnimalId ? t('animals.updateTitle') : t('animals.createTitle')}</h2>
              <p>
                {editingAnimalId
                  ? t('animals.updateDescription')
                  : t('animals.createDescription')}
              </p>
            </div>
          </div>

          <AnimalForm
            initialValues={formInitialValues}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingAnimalId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingAnimalId ? t('animals.submitUpdate') : t('animals.submitCreate')}
            errorMessage={formErrorMessage}
            selectedFarmName={selectedFarm?.name}
            showStatusField={editingAnimalId !== null}
          />
        </article>

        {sellingAnimal && (
          <article className="animals-panel">
            <div className="animals-panel__header">
              <div>
                <h2>{t('animals.sellTitle')}</h2>
                <p>{t('animals.sellDescription', { tag: sellingAnimal.tag })}</p>
              </div>
            </div>

            <div className="animal-form">
              <div className="animal-form__grid">
                <label className="animal-form__field">
                  <span>{t('animals.form.salePrice')}</span>
                  <input
                    type="number"
                    min="0.01"
                    step="0.01"
                    value={salePrice}
                    onChange={(event) => setSalePrice(event.target.value)}
                    required
                  />
                </label>

                <label className="animal-form__field">
                  <span>{t('animals.form.saleDate')}</span>
                  <input
                    type="date"
                    value={saleDate}
                    onChange={(event) => setSaleDate(event.target.value)}
                  />
                </label>
              </div>

              {sellErrorMessage && (
                <p className="animal-form__feedback animal-form__feedback--error">
                  {sellErrorMessage}
                </p>
              )}

              <div className="animal-form__actions">
                <button
                  type="button"
                  onClick={() => void handleSell()}
                  disabled={isSellingId === sellingAnimal.id}
                >
                  {isSellingId === sellingAnimal.id ? t('animals.selling') : t('animals.sellConfirm')}
                </button>

                <button
                  type="button"
                  className="animal-form__secondary-button"
                  onClick={handleCancelSell}
                  disabled={isSellingId === sellingAnimal.id}
                >
                  {t('common.cancel')}
                </button>
              </div>
            </div>
          </article>
        )}

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('animals.listTitle')}</h2>
              <p>{t('animals.listDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={!selectedFarmId || isLoading || animals.length === 0}
            />
          </div>

          <ListingFiltersBar
            searchId="animals-search"
            searchLabel={t('animals.filters.searchLabel')}
            searchPlaceholder={t('animals.filters.searchPlaceholder')}
            searchValue={filters.search}
            onSearchChange={(value) => setFilters((current) => ({ ...current, search: value }))}
            onApply={applyFilters}
            onClear={clearFilters}
            applyLabel={t('animals.filters.apply')}
            clearLabel={t('animals.filters.clear')}
            filters={[
              {
                id: 'animals-status-filter',
                label: t('animals.filters.statusLabel'),
                value: filters.status,
                onChange: (value) => setFilters((current) => ({ ...current, status: value as AnimalListFilters['status'] })),
                options: [
                  { value: '', label: t('animals.filters.allStatuses') },
                  ...ANIMAL_STATUSES.map((status) => ({
                    value: status,
                    label: getAnimalStatusLabel(t, status),
                  })),
                ],
              },
              {
                id: 'animals-origin-filter',
                label: t('animals.filters.originLabel'),
                value: filters.origin,
                onChange: (value) => setFilters((current) => ({ ...current, origin: value as AnimalListFilters['origin'] })),
                options: [
                  { value: '', label: t('animals.filters.allOrigins') },
                  ...ANIMAL_ORIGINS.map((origin) => ({
                    value: origin,
                    label: getAnimalOriginLabel(t, origin),
                  })),
                ],
              },
            ]}
          />

          {isLoading && <p className="animals-page__status">{t('animals.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && animals.length === 0 && (
            <p className="animals-page__status">{t('animals.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && animals.length > 0 && (
            <>
              <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('animals.table.tag')}</th>
                    <th>{t('animals.table.breed')}</th>
                    <th>{t('animals.table.birthDate')}</th>
                    <th>{t('animals.table.origin')}</th>
                    <th>{t('animals.table.status')}</th>
                    <th>{t('animals.table.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {animals.map((animal) => (
                    <tr key={animal.id}>
                      <td>{animal.tag}</td>
                      <td>{animal.breed}</td>
                      <td>{animal.birthDate}</td>
                      <td>{getAnimalOriginLabel(t, animal.origin)}</td>
                      <td>
                        <span
                          className={`animals-table__status animals-table__status--${animal.status.toLowerCase()}`}
                        >
                          {getAnimalStatusLabel(t, animal.status)}
                        </span>
                      </td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button"
                          onClick={() => onOpenDetails(animal.id)}
                          disabled={isSubmitting || isDeletingId === animal.id}
                        >
                          {t('animals.details')}
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => void handleEdit(animal.id)}
                          disabled={isSubmitting || isDeletingId === animal.id || isSellingId === animal.id}
                        >
                          {t('animals.edit')}
                        </button>
                        {animal.status === 'ACTIVE' && (
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--secondary"
                            onClick={() => handleStartSell(animal)}
                            disabled={isSubmitting || isDeletingId === animal.id || isSellingId === animal.id}
                          >
                            {t('animals.sell')}
                          </button>
                        )}
                        {canDeleteResources && (
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--danger"
                            onClick={() => void handleDelete(animal.id)}
                            disabled={isDeletingId === animal.id || isSellingId === animal.id}
                          >
                            {isDeletingId === animal.id ? t('animals.deleting') : t('animals.delete')}
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

export default AnimalsPage
