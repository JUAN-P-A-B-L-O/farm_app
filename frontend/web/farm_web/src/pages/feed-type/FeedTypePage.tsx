import { useEffect, useState } from 'react'
import axios from 'axios'
import FeedTypeForm from '../../components/feed-type/FeedTypeForm'
import { useTranslation } from '../../hooks/useTranslation'
import {
  createFeedType,
  deleteFeedType,
  getAllFeedTypes,
  updateFeedType,
} from '../../services/feedTypeService'
import type { FeedType, FeedTypeApiErrorResponse, FeedTypeFormData } from '../../types/feedType'
import '../../App.css'

const emptyFeedTypeForm: FeedTypeFormData = {
  name: '',
  costPerKg: 0,
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
  const { t } = useTranslation()
  const [feedTypes, setFeedTypes] = useState<FeedType[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingFeedTypeId, setEditingFeedTypeId] = useState<string | null>(null)
  const [formInitialValues, setFormInitialValues] = useState<FeedTypeFormData>(emptyFeedTypeForm)

  async function loadFeedTypes() {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllFeedTypes()
      setFeedTypes(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('feedType.errors.loadList'), t))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadFeedTypes()
  }, [])

  async function handleCreateOrUpdate(data: FeedTypeFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingFeedTypeId) {
        await updateFeedType(editingFeedTypeId, data)
      } else {
        await createFeedType(data)
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
      await deleteFeedType(id)

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
            initialValues={formInitialValues}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingFeedTypeId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingFeedTypeId ? t('feedType.submitUpdate') : t('feedType.submitCreate')}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>{t('feedType.listTitle')}</h2>
              <p>{t('feedType.listDescription')}</p>
            </div>
          </div>

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
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('feedType.table.name')}</th>
                    <th>{t('feedType.table.costPerKg')}</th>
                    <th>{t('feedType.table.actions')}</th>
                  </tr>
                </thead>
                <tbody>
                  {feedTypes.map((feedType) => (
                    <tr key={feedType.id}>
                      <td>{feedType.name}</td>
                      <td>{feedType.costPerKg}</td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => handleEdit(feedType)}
                          disabled={isSubmitting || isDeletingId === feedType.id}
                        >
                          {t('feedType.edit')}
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--danger"
                          onClick={() => handleDelete(feedType.id)}
                          disabled={isSubmitting || isDeletingId === feedType.id}
                        >
                          {isDeletingId === feedType.id ? t('feedType.deleting') : t('feedType.delete')}
                        </button>
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

export default FeedTypePage
