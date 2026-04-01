import { useEffect, useState } from 'react'
import axios from 'axios'
import ProductionForm from '../../components/production/ProductionForm'
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

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<ProductionApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? 'Validation error while saving production.'
    }

    if (status === 404) {
      return apiMessage ?? 'Animal not found.'
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
      setListErrorMessage(getErrorMessage(error, 'Unable to load production records.'))
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
      setFormErrorMessage(getErrorMessage(error, 'Unable to load animals.'))
    } finally {
      setIsAnimalsLoading(false)
    }
  }

  useEffect(() => {
    void Promise.all([loadProductions(), loadAnimals()])
  }, [])

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
      setFormErrorMessage('Fill in animal, user, date, and a quantity greater than zero.')
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
      setFormErrorMessage(getErrorMessage(error, 'Unable to create production record.'))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Production Control</p>
        <h1>Production Management</h1>
        <p className="animals-page__description">
          Record new production entries and review the current production history.
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>Create Production</h2>
              <p>Register a production entry for an animal using the available herd list.</p>
            </div>
          </div>

          {isAnimalsLoading && <p className="animals-page__status">Loading animals...</p>}

          {!isAnimalsLoading && animals.length === 0 && !formErrorMessage && (
            <p className="animals-page__status">No animals available for production records.</p>
          )}

          {!isAnimalsLoading && (
            <ProductionForm
              initialValues={formInitialValues}
              animals={animals}
              onSubmit={handleCreateProduction}
              isSubmitting={isSubmitting}
              submitLabel="Create production"
              errorMessage={formErrorMessage}
            />
          )}
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header">
            <div>
              <h2>Production List</h2>
              <p>Track animal tag, production date, and quantity for each record.</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">Loading production records...</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">
              {listErrorMessage}
            </p>
          )}

          {!isLoading && !listErrorMessage && productions.length === 0 && (
            <p className="animals-page__status">No production records found.</p>
          )}

          {!isLoading && !listErrorMessage && productions.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>Animal tag</th>
                    <th>Date</th>
                    <th>Quantity</th>
                  </tr>
                </thead>
                <tbody>
                  {productions.map((production) => (
                    <tr key={production.id}>
                      <td>{production.animal.tag}</td>
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
