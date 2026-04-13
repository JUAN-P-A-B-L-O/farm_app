import { useEffect, useState } from 'react'
import axios from 'axios'
import { useFarm } from '../../hooks/useFarm'
import { getAnimalById } from '../../services/animalService'
import { getFeedingsByAnimalId } from '../../services/feedingService'
import { getProductionsByAnimalId } from '../../services/productionService'
import type { Animal, ApiErrorResponse } from '../../types/animal'
import type { FeedingTrendPoint } from '../../types/feeding'
import type { ProductionTrendPoint } from '../../types/production'
import '../../App.css'

interface AnimalDetailsPageProps {
  animalId: string
  onBackToAnimals: () => void
}

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 404) {
      return apiMessage ?? 'Animal not found.'
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function sortByDateDescending<T extends { date: string }>(records: T[]): T[] {
  return [...records].sort((left, right) => right.date.localeCompare(left.date))
}

function AnimalDetailsPage({ animalId, onBackToAnimals }: AnimalDetailsPageProps) {
  const { selectedFarmId } = useFarm()
  const [animal, setAnimal] = useState<Animal | null>(null)
  const [productions, setProductions] = useState<ProductionTrendPoint[]>([])
  const [feedings, setFeedings] = useState<FeedingTrendPoint[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    async function loadAnimalDetails() {
      if (!selectedFarmId) {
        setAnimal(null)
        setProductions([])
        setFeedings([])
        setIsLoading(false)
        return
      }

      setIsLoading(true)
      setErrorMessage('')

      try {
        const [animalData, productionsData, feedingsData] = await Promise.all([
          getAnimalById(animalId, selectedFarmId),
          getProductionsByAnimalId(animalId, selectedFarmId),
          getFeedingsByAnimalId(animalId, selectedFarmId),
        ])

        setAnimal(animalData)
        setProductions(sortByDateDescending(productionsData))
        setFeedings(sortByDateDescending(feedingsData))
      } catch (error) {
        setErrorMessage(getErrorMessage(error, 'Unable to load animal details.'))
      } finally {
        setIsLoading(false)
      }
    }

    void loadAnimalDetails()
  }, [animalId, selectedFarmId])

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Animal Details</p>
        <h1>Animal Overview</h1>
        <p className="animals-page__description">
          Review the animal profile together with its production and feeding history.
        </p>
        <button
          type="button"
          className="animals-table__action-button animals-table__action-button--secondary"
          onClick={onBackToAnimals}
        >
          Back to animals
        </button>
      </section>

      {isLoading && <p className="animals-page__status animals-page__status--standalone">Loading animal details...</p>}

      {!isLoading && errorMessage && (
        <p className="animals-page__status animals-page__status--error animals-page__status--standalone">
          {errorMessage}
        </p>
      )}

      {!isLoading && !errorMessage && animal && (
        <section className="animal-details-layout">
          <article className="animals-panel">
            <div className="animals-panel__header">
              <div>
                <h2>Animal Information</h2>
                <p>Core data for the selected animal.</p>
              </div>
            </div>

            <dl className="animal-details-grid">
              <div className="animal-details-grid__item">
                <dt>Tag</dt>
                <dd>{animal.tag}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>Breed</dt>
                <dd>{animal.breed}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>Birth date</dt>
                <dd>{animal.birthDate}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>Status</dt>
                <dd>
                  <span
                    className={`animals-table__status animals-table__status--${animal.status.toLowerCase()}`}
                  >
                    {animal.status}
                  </span>
                </dd>
              </div>
            </dl>
          </article>

          <article className="animals-panel animals-panel--table">
            <div className="animals-panel__header">
              <div>
                <h2>Production History</h2>
                <p>Production records sorted from most recent to oldest.</p>
              </div>
            </div>

            {productions.length === 0 && (
              <p className="animals-page__status">No production records found for this animal.</p>
            )}

            {productions.length > 0 && (
              <div className="animals-table-wrapper">
                <table className="animals-table">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Quantity</th>
                    </tr>
                  </thead>
                  <tbody>
                    {productions.map((production) => (
                      <tr key={`${production.date}-${production.quantity}`}>
                        <td>{production.date}</td>
                        <td>{production.quantity}</td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
            )}
          </article>

          <article className="animals-panel animals-panel--table">
            <div className="animals-panel__header">
              <div>
                <h2>Feeding History</h2>
                <p>Feeding records sorted from most recent to oldest.</p>
              </div>
            </div>

            {feedings.length === 0 && (
              <p className="animals-page__status">No feeding records found for this animal.</p>
            )}

            {feedings.length > 0 && (
              <div className="animals-table-wrapper">
                <table className="animals-table">
                  <thead>
                    <tr>
                      <th>Date</th>
                      <th>Quantity</th>
                    </tr>
                  </thead>
                  <tbody>
                    {feedings.map((feeding) => (
                      <tr key={`${feeding.date}-${feeding.quantity}`}>
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
      )}
    </main>
  )
}

export default AnimalDetailsPage
