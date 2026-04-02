import { useEffect, useState } from 'react'
import axios from 'axios'
import AnimalForm from '../../components/animal/AnimalForm'
import { useTranslation } from '../../hooks/useTranslation'
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
      setListErrorMessage(getErrorMessage(error, t('animals.errors.loadList'), t))
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
      const animal = await getAnimalById(id)

      setEditingAnimalId(animal.id)
      setFormInitialValues({
        tag: animal.tag,
        breed: animal.breed,
        birthDate: animal.birthDate,
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

  async function handleDelete(id: string) {
    const shouldDelete = window.confirm(t('animals.confirmDelete'))

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
      setListErrorMessage(getErrorMessage(error, t('animals.errors.delete'), t))
    } finally {
      setIsDeletingId(null)
    }
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
          />
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>{t('animals.listTitle')}</h2>
              <p>{t('animals.listDescription')}</p>
            </div>
          </div>

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
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('animals.table.tag')}</th>
                    <th>{t('animals.table.breed')}</th>
                    <th>{t('animals.table.birthDate')}</th>
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
                          {t('animals.details')}
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--secondary"
                          onClick={() => void handleEdit(animal.id)}
                          disabled={isSubmitting || isDeletingId === animal.id}
                        >
                          {t('animals.edit')}
                        </button>
                        <button
                          type="button"
                          className="animals-table__action-button animals-table__action-button--danger"
                          onClick={() => void handleDelete(animal.id)}
                          disabled={isDeletingId === animal.id}
                        >
                          {isDeletingId === animal.id ? t('animals.deleting') : t('animals.delete')}
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
