import { useEffect, useState } from 'react'
import axios from 'axios'
import ProductionForm from '../../components/production/ProductionForm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import { createProduction, getAllProductions } from '../../services/productionService'
import type { Animal } from '../../types/animal'
import type {
  Production,
  ProductionApiErrorResponse,
  ProductionAnimalOption,
  ProductionFormData,
} from '../../types/production'
import '../../App.css'

const emptyProductionForm: ProductionFormData = {
  animalId: '',
  date: '',
  quantity: 0,
  userId: '',
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
  return animals.map(({ id, tag }) => ({
    id,
    tag,
  }))
}

function ProductionPage() {
  const { t, language } = useTranslation()
  const [productions, setProductions] = useState<Production[]>([])
  const [animals, setAnimals] = useState<ProductionAnimalOption[]>([])
  const [formInitialValues, setFormInitialValues] = useState<ProductionFormData>(emptyProductionForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')

  async function loadProductions() {
    setIsLoading(true)
    setListErrorMessage('')

    try {
      const data = await getAllProductions()
      setProductions(data)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('production.errors.loadRecords'), t))
    } finally {
      setIsLoading(false)
    }
  }

  async function loadAnimals() {
    setIsAnimalsLoading(true)
    setFormErrorMessage('')

    try {
      const data = await getAllAnimals()
      setAnimals(mapAnimalsToOptions(data))
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('production.errors.loadAnimals'), t))
    } finally {
      setIsAnimalsLoading(false)
    }
  }

  useEffect(() => {
    void Promise.all([loadProductions(), loadAnimals()])
  }, [language])

  async function handleCreateProduction(data: ProductionFormData) {
    const payload: ProductionFormData = {
      animalId: data.animalId.trim(),
      date: data.date,
      quantity: Number(data.quantity),
      userId: data.userId.trim(),
    }

    if (
      !payload.animalId ||
      !payload.date ||
      !payload.userId ||
      !Number.isFinite(payload.quantity) ||
      payload.quantity <= 0
    ) {
      setFormErrorMessage(t('production.errors.missingFields'))
      return
    }

    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      console.log('Production payload:', payload)
      await createProduction(payload)
      setFormInitialValues({ ...emptyProductionForm })
      await loadProductions()
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('production.errors.create'), t))
    } finally {
      setIsSubmitting(false)
    }
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
              <h2>{t('production.createTitle')}</h2>
              <p>{t('production.createDescription')}</p>
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
              onSubmit={handleCreateProduction}
              isSubmitting={isSubmitting}
              submitLabel={t('production.submit')}
              errorMessage={formErrorMessage}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>{t('production.listTitle')}</h2>
              <p>{t('production.listDescription')}</p>
            </div>
          </div>

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
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('production.table.animalTag')}</th>
                    <th>{t('production.table.date')}</th>
                    <th>{t('production.table.quantity')}</th>
                  </tr>
                </thead>
                <tbody>
                  {productions.map((production) => (
                    <tr key={production.id}>
                      <td>{production.animal?.tag}</td>
                      <td>{production.date}</td>
                      <td>{production.quantity}</td>
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

export default ProductionPage
