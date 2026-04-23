import { useEffect, useState } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import FeedingForm from '../../components/feeding/FeedingForm'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import {
  createFeeding,
  deleteFeeding,
  exportFeedingsCsv,
  getAllFeedings,
  getAllFeedTypes,
  getFeedingById,
  updateFeeding,
} from '../../services/feedingService'
import type { Animal } from '../../types/animal'
import type {
  Feeding,
  FeedingAnimalOption,
  FeedingApiErrorResponse,
  FeedingFeedTypeOption,
  FeedingFormData,
} from '../../types/feeding'
import { isManager } from '../../utils/authorization'
import '../../App.css'

const emptyFeedingForm: FeedingFormData = {
  animalId: '',
  feedTypeId: '',
  date: '',
  quantity: 0,
  userId: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<FeedingApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? t('feeding.errors.validationSave')
    }

    if (status === 404) {
      return apiMessage ?? t('feeding.errors.notFound')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function mapAnimalsToOptions(animals: Animal[]): FeedingAnimalOption[] {
  return animals
    .filter((animal) => animal.status === 'ACTIVE')
    .map(({ id, tag }) => ({
      id,
      tag,
    }))
}

function FeedingPage() {
  const { t } = useTranslation()
  const { user } = useAuth()
  const { selectedFarmId } = useFarm()
  const canSelectCreateDate = isManager(user)
  const canDeleteResources = isManager(user)
  const [feedings, setFeedings] = useState<Feeding[]>([])
  const [animals, setAnimals] = useState<FeedingAnimalOption[]>([])
  const [feedTypes, setFeedTypes] = useState<FeedingFeedTypeOption[]>([])
  const [formInitialValues, setFormInitialValues] = useState<FeedingFormData>(emptyFeedingForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isFormOptionsLoading, setIsFormOptionsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingFeedingId, setEditingFeedingId] = useState<string | null>(null)
  const [isExporting, setIsExporting] = useState(false)

  async function loadFeedings() {
    if (!selectedFarmId) {
      setFeedings([])
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllFeedings(selectedFarmId)
      setFeedings(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('feeding.errors.loadRecords'), t))
    } finally {
      setIsLoading(false)
    }
  }

  async function loadFormOptions() {
    if (!selectedFarmId) {
      setAnimals([])
      setFeedTypes([])
      setFormErrorMessage('')
      setIsFormOptionsLoading(false)
      return
    }

    setIsFormOptionsLoading(true)
    setFormErrorMessage('')

    try {
      const [animalsData, feedTypesData] = await Promise.all([
        getAllAnimals(selectedFarmId),
        getAllFeedTypes(selectedFarmId),
      ])

      setAnimals(mapAnimalsToOptions(animalsData))
      setFeedTypes(feedTypesData)
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('feeding.errors.loadOptions'), t))
    } finally {
      setIsFormOptionsLoading(false)
    }
  }

  useEffect(() => {
    void Promise.all([loadFeedings(), loadFormOptions()])
  }, [selectedFarmId])

  async function handleCreateOrUpdateFeeding(data: FeedingFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingFeedingId) {
        await updateFeeding(editingFeedingId, data, selectedFarmId)
      } else {
        await createFeeding(data, selectedFarmId)
      }

      setEditingFeedingId(null)
      setFormInitialValues({ ...emptyFeedingForm })
      await loadFeedings()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingFeedingId ? t('feeding.errors.update') : t('feeding.errors.create'),
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
      const feeding = await getFeedingById(id, selectedFarmId)

      setEditingFeedingId(feeding.id)
      setFormInitialValues({
        animalId: feeding.animalId,
        feedTypeId: feeding.feedTypeId,
        date: feeding.date,
        quantity: feeding.quantity,
        userId: '',
      })
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('feeding.errors.loadDetails'), t))
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleCancelEdit() {
    setEditingFeedingId(null)
    setFormErrorMessage('')
    setFormInitialValues({ ...emptyFeedingForm })
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm(t('feeding.confirmDelete'))

    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteFeeding(id, selectedFarmId)

      if (editingFeedingId === id) {
        handleCancelEdit()
      }

      await loadFeedings()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('feeding.errors.delete'), t))
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
      await exportFeedingsCsv(selectedFarmId)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError'), t))
    } finally {
      setIsExporting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('feeding.eyebrow')}</p>
        <h1>{t('feeding.title')}</h1>
        <p className="animals-page__description">
          {t('feeding.description')}
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingFeedingId ? t('feeding.updateTitle') : t('feeding.createTitle')}</h2>
              <p>
                {editingFeedingId
                  ? t('feeding.updateDescription')
                  : t('feeding.createDescription')}
              </p>
            </div>
          </div>

          {isFormOptionsLoading && <p className="animals-page__status">{t('feeding.loadingOptions')}</p>}

          {!isFormOptionsLoading && animals.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">{t('feeding.emptyAnimals')}</p>
          )}

          {!isFormOptionsLoading && feedTypes.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">{t('feeding.emptyFeedTypes')}</p>
          )}

          {!isFormOptionsLoading && (
            <FeedingForm
              initialValues={formInitialValues}
              animals={animals}
              feedTypes={feedTypes}
              onSubmit={handleCreateOrUpdateFeeding}
              onCancel={editingFeedingId ? handleCancelEdit : undefined}
              isSubmitting={isSubmitting}
              submitLabel={editingFeedingId ? t('feeding.submitUpdate') : t('feeding.submitCreate')}
              errorMessage={formErrorMessage}
              requireUserSelection={!editingFeedingId}
              allowDateSelection={editingFeedingId !== null || canSelectCreateDate}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('feeding.listTitle')}</h2>
              <p>{t('feeding.listDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={!selectedFarmId || isLoading || feedings.length === 0}
            />
          </div>

          {isLoading && <p className="animals-page__status">{t('feeding.loadingRecords')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && feedings.length === 0 && (
            <p className="animals-page__status">{t('feeding.emptyRecords')}</p>
          )}

          {!isLoading && !listErrorMessage && feedings.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('feeding.table.animalTag')}</th>
                    <th>{t('feeding.table.feedType')}</th>
                    <th>{t('feeding.table.date')}</th>
                    <th>{t('feeding.table.quantity')}</th>
                    <th>{t('feeding.table.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {feedings.map((feeding) => (
                    <tr key={feeding.id}>
                      <td>{feeding.animal?.tag}</td>
                      <td>{feeding.feedType?.name}</td>
                      <td>{feeding.date}</td>
                      <td>{feeding.quantity}</td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => void handleEdit(feeding.id)}
                          disabled={isSubmitting || isDeletingId === feeding.id}
                        >
                          {t('feeding.edit')}
                        </button>
                        {canDeleteResources && (
                          <button
                            type="button"
                            className="animals-table__action-button animals-table__action-button--danger"
                            onClick={() => void handleDelete(feeding.id)}
                            disabled={isDeletingId === feeding.id}
                          >
                            {isDeletingId === feeding.id ? t('feeding.deleting') : t('feeding.delete')}
                          </button>
                        )}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </article>
      </section>
    </main>
  )
}

export default FeedingPage
