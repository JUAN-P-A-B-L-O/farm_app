import { useEffect, useState } from 'react'
import axios from 'axios'
import FeedTypeForm from '../../components/feed-type/FeedTypeForm'
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

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<FeedTypeApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? 'Validation error while saving feed type.'
    }

    if (status === 404) {
      return apiMessage ?? 'Feed type not found.'
    }

    if (status === 409) {
      return apiMessage ?? 'Feed type with this name already exists.'
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function FeedTypePage() {
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
      setListErrorMessage(getErrorMessage(error, 'Unable to load feed types.'))
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
          editingFeedTypeId ? 'Unable to update feed type.' : 'Unable to create feed type.',
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
    const shouldDelete = window.confirm('Are you sure you want to delete this feed type?')

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
      setListErrorMessage(getErrorMessage(error, 'Unable to delete feed type.'))
    } finally {
      setIsDeletingId(null)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Feed Control</p>
        <h1>Feed Type Management</h1>
        <p className="animals-page__description">
          Manage available feed types and keep feeding records aligned with current options.
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingFeedTypeId ? 'Update Feed Type' : 'Create Feed Type'}</h2>
              <p>
                {editingFeedTypeId
                  ? 'Edit the selected feed type using the existing data.'
                  : 'Fill in the feed type information to create a new record.'}
              </p>
            </div>
          </div>

          <FeedTypeForm
            initialValues={formInitialValues}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingFeedTypeId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingFeedTypeId ? 'Update feed type' : 'Create feed type'}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>Feed Type List</h2>
              <p>Review current feed types and manage updates or deletions.</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">Loading feed types...</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && feedTypes.length === 0 && (
            <p className="animals-page__status">No feed types found.</p>
          )}

          {!isLoading && !listErrorMessage && feedTypes.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>Name</th>
                    <th>Cost per kg</th>
                    <th>Actions</th>
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
                          Edit
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--danger"
                          onClick={() => handleDelete(feedType.id)}
                          disabled={isSubmitting || isDeletingId === feedType.id}
                        >
                          {isDeletingId === feedType.id ? 'Deleting...' : 'Delete'}
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
