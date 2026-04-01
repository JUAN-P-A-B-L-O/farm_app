import { useEffect, useState } from 'react'
import axios from 'axios'
import FeedingForm from '../../components/feeding/FeedingForm'
import { useTranslation } from '../../hooks/useTranslation'
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
  return animals.map(({ id, tag }) => ({
    id,
    tag,
  }))
}

function FeedingPage() {
  const { t, language } = useTranslation()
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
      setListErrorMessage(getErrorMessage(error, t('feeding.errors.loadRecords'), t))
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
      setFormErrorMessage(getErrorMessage(error, t('feeding.errors.loadOptions'), t))
    } finally {
      setIsFormOptionsLoading(false)
    }
  }

  useEffect(() => {
    void Promise.all([loadFeedings(), loadFormOptions()])
  }, [language])

  async function handleCreateFeeding(data: FeedingFormData) {
    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      await createFeeding(data)
      setFormInitialValues({ ...emptyFeedingForm })
      await loadFeedings()
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('feeding.errors.create'), t))
    } finally {
      setIsSubmitting(false)
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
              <h2>{t('feeding.createTitle')}</h2>
              <p>{t('feeding.createDescription')}</p>
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
              onSubmit={handleCreateFeeding}
              isSubmitting={isSubmitting}
              submitLabel={t('feeding.submit')}
              errorMessage={formErrorMessage}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>{t('feeding.listTitle')}</h2>
              <p>{t('feeding.listDescription')}</p>
            </div>
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
