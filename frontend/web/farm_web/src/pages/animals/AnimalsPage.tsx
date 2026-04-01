import { useEffect, useState } from 'react'
import axios from 'axios'
import AnimalForm from '../../components/animal/AnimalForm'
import {
  createAnimal,
  deleteAnimal,
  getAllAnimals,
  getAnimalById,
  updateAnimal,
} from '../../services/animalService'
import type { Animal, AnimalFormData, ApiErrorResponse } from '../../types/animal'
import '../../App.css'

interface AnimalsPageProps {
  onOpenDetails: (animalId: string) => void
}

const emptyAnimalForm: AnimalFormData = {
  tag: '',
  breed: '',
  birthDate: '',
  farmId: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 404) {
      return apiMessage ?? 'Animal not found.'
    }

    if (status === 409) {
      return apiMessage ?? 'Animal with this tag already exists.'
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function AnimalsPage({ onOpenDetails }: AnimalsPageProps) {
  const [animals, setAnimals] = useState<Animal[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isDeletingId, setIsDeletingId] = useState<string | null>(null)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')
  const [editingAnimalId, setEditingAnimalId] = useState<string | null>(null)
  const [formInitialValues, setFormInitialValues] = useState<AnimalFormData>(emptyAnimalForm)

  async function loadAnimals() {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllAnimals()
      setAnimals(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, 'Unable to load animals.'))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadAnimals()
  }, [])

  async function handleCreateOrUpdate(data: AnimalFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      if (editingAnimalId) {
        await updateAnimal(editingAnimalId, data)
      } else {
        await createAnimal(data)
      }

      setEditingAnimalId(null)
      setFormInitialValues(emptyAnimalForm)
      await loadAnimals()
    } catch (error) {
      setFormErrorMessage(
        getErrorMessage(
          error,
          editingAnimalId ? 'Unable to update animal.' : 'Unable to create animal.',
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
      const animal = await getAnimalById(id)

      setEditingAnimalId(animal.id)
      setFormInitialValues({
        tag: animal.tag,
        breed: animal.breed,
        birthDate: animal.birthDate,
        farmId: animal.farmId,
      })
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, 'Unable to load animal details.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  function handleCancelEdit() {
    setEditingAnimalId(null)
    setFormErrorMessage('')
    setFormInitialValues(emptyAnimalForm)
  }

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm('Are you sure you want to delete this animal?')

    if (!shouldDelete) {
      return
    }

    setIsDeletingId(id)
    setListErrorMessage('')

    try {
      await deleteAnimal(id)

      if (editingAnimalId === id) {
        handleCancelEdit()
      }

      await loadAnimals()
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, 'Unable to delete animal.'))
    } finally {
      setIsDeletingId(null)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Livestock Control</p>
        <h1>Animals Management</h1>
        <p className="animals-page__description">
          Register animals, update records, and keep the herd inventory organized.
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{editingAnimalId ? 'Update Animal' : 'Create Animal'}</h2>
              <p>
                {editingAnimalId
                  ? 'Edit the selected animal using the existing data.'
                  : 'Fill in the animal information to create a new record.'}
              </p>
            </div>
          </div>

          <AnimalForm
            initialValues={formInitialValues}
            onSubmit={handleCreateOrUpdate}
            onCancel={editingAnimalId ? handleCancelEdit : undefined}
            isSubmitting={isSubmitting}
            submitLabel={editingAnimalId ? 'Update animal' : 'Create animal'}
            errorMessage={formErrorMessage}
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>Animals List</h2>
              <p>Review current records and manage updates or deletions.</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">Loading animals...</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && animals.length === 0 && (
            <p className="animals-page__status">No animals found.</p>
          )}

          {!isLoading && !listErrorMessage && animals.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>Tag</th>
                    <th>Breed</th>
                    <th>Birth date</th>
                    <th>Status</th>
                    <th>Actions</th>
                  </tr>
                </thead>
                <tbody>
                  {animals.map((animal) => (
                    <tr key={animal.id}>
                      <td>{animal.tag}</td>
                      <td>{animal.breed}</td>
                      <td>{animal.birthDate}</td>
                      <td>
                        <span
                          className={`animals-table__status animals-table__status--${animal.status.toLowerCase()}`}
                        >
                          {animal.status}
                        </span>
                      </td>
                      <td className="animals-table__actions">
                        <button
                          type="button"
                          className="animals-table__action-button"
                          onClick={() => onOpenDetails(animal.id)}
                          disabled={isSubmitting || isDeletingId === animal.id}
                        >
                          Details
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => void handleEdit(animal.id)}
                          disabled={isSubmitting || isDeletingId === animal.id}
                        >
                          Edit
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--danger"
                          onClick={() => void handleDelete(animal.id)}
                          disabled={isDeletingId === animal.id}
                        >
                          {isDeletingId === animal.id ? 'Deleting...' : 'Delete'}
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

export default AnimalsPage
