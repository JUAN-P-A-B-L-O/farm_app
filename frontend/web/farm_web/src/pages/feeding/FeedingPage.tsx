import { useEffect, useState } from 'react'
import axios from 'axios'
import FeedingForm from '../../components/feeding/FeedingForm'
import { getAllAnimals } from '../../services/animalService'
import { createFeeding, getAllFeedings, getAllFeedTypes } from '../../services/feedingService'
import type { Animal } from '../../types/animal'
import type {
  Feeding,
  FeedingAnimalOption,
  FeedingApiErrorResponse,
  FeedingFeedTypeOption,
  FeedingFormData,
} from '../../types/feeding'
import '../../App.css'

const emptyFeedingForm: FeedingFormData = {
  animalId: '',
  feedTypeId: '',
  date: '',
  quantity: 0,
}

const FEEDING_USER_ID = import.meta.env.VITE_USER_ID ?? 'string'

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<FeedingApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? 'Validation error while saving feeding.'
    }

    if (status === 404) {
      return apiMessage ?? 'Animal or feed type not found.'
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function mapAnimalsToOptions(animals: Animal[]): FeedingAnimalOption[] {
  return animals.map(({ id, tag }) => ({
    id,
    tag,
  }))
}

function FeedingPage() {
  const [feedings, setFeedings] = useState<Feeding[]>([])
  const [animals, setAnimals] = useState<FeedingAnimalOption[]>([])
  const [feedTypes, setFeedTypes] = useState<FeedingFeedTypeOption[]>([])
  const [formInitialValues, setFormInitialValues] = useState<FeedingFormData>(emptyFeedingForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isFormOptionsLoading, setIsFormOptionsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')

  async function loadFeedings() {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllFeedings()
      setFeedings(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, 'Unable to load feeding records.'))
    } finally {
      setIsLoading(false)
    }
  }

  async function loadFormOptions() {
    setIsFormOptionsLoading(true)
    setFormErrorMessage('')

    try {
      const [animalsData, feedTypesData] = await Promise.all([getAllAnimals(), getAllFeedTypes()])

      setAnimals(mapAnimalsToOptions(animalsData))
      setFeedTypes(feedTypesData)
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, 'Unable to load form options.'))
    } finally {
      setIsFormOptionsLoading(false)
    }
  }

  useEffect(() => {
    void Promise.all([loadFeedings(), loadFormOptions()])
  }, [])

  async function handleCreateFeeding(data: FeedingFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      await createFeeding({
        ...data,
        userId: FEEDING_USER_ID,
      })
      setFormInitialValues({ ...emptyFeedingForm })
      await loadFeedings()
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, 'Unable to create feeding record.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Feeding Control</p>
        <h1>Feeding Management</h1>
        <p className="animals-page__description">
          Register feeding entries and review animal feed consumption records.
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>Create Feeding</h2>
              <p>Select the animal, feed type, date, and quantity for the new feeding entry.</p>
            </div>
          </div>

          {isFormOptionsLoading && <p className="animals-page__status">Loading form options...</p>}

          {!isFormOptionsLoading && animals.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">No animals available for feeding records.</p>
          )}

          {!isFormOptionsLoading && feedTypes.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">No feed types available for feeding records.</p>
          )}

          {!isFormOptionsLoading && (
            <FeedingForm
              initialValues={formInitialValues}
              animals={animals}
              feedTypes={feedTypes}
              onSubmit={handleCreateFeeding}
              isSubmitting={isSubmitting}
              submitLabel="Create feeding"
              errorMessage={formErrorMessage}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>Feeding List</h2>
              <p>Review animal tag, feed type, date, and quantity for each feeding record.</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">Loading feeding records...</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && feedings.length === 0 && (
            <p className="animals-page__status">No feeding records found.</p>
          )}

          {!isLoading && !listErrorMessage && feedings.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>Animal tag</th>
                    <th>Feed type</th>
                    <th>Date</th>
                    <th>Quantity</th>
                  </tr>
                </thead>
                <tbody>
                  {feedings.map((feeding) => (
                    <tr key={feeding.id}>
                      <td>{feeding.animal.tag}</td>
                      <td>{feeding.feedType.name}</td>
                      <td>{feeding.date}</td>
                      <td>{feeding.quantity}</td>
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
