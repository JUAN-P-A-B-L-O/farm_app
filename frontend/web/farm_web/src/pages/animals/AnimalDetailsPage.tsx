import { useEffect, useState } from 'react'
import axios from 'axios'
import { useCurrency } from '../../hooks/useCurrency'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { getAnimalById } from '../../services/animalService'
import { getFeedingsByAnimalId } from '../../services/feedingService'
import { getProductionsByAnimalId } from '../../services/productionService'
import type { Animal, ApiErrorResponse } from '../../types/animal'
import type { FeedingTrendPoint } from '../../types/feeding'
import type { ProductionTrendPoint } from '../../types/production'
import { appendCurrencyCode, formatDisplayMoney } from '../../utils/currency'
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
      return apiMessage ?? fallbackMessage
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
  const { t, language } = useTranslation()
  const { currency } = useCurrency()
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
        setErrorMessage(getErrorMessage(error, t('animals.errors.loadDetails')))
      } finally {
        setIsLoading(false)
      }
    }

    void loadAnimalDetails()
  }, [animalId, selectedFarmId, t])

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('animals.details')}</p>
        <h1>{t('animals.detailsTitle')}</h1>
        <p className="animals-page__description">
          {t('animals.detailsDescription')}
        </p>
        <button
          type="button"
          className="animals-table__action-button animals-table__action-button--secondary"
          onClick={onBackToAnimals}
        >
          {t('animals.back')}
        </button>
      </section>

      {isLoading && <p className="animals-page__status animals-page__status--standalone">{t('animals.loadingDetails')}</p>}

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
                <h2>{t('animals.detailsSections.infoTitle')}</h2>
                <p>{t('animals.detailsSections.infoDescription')}</p>
              </div>
            </div>

            <dl className="animal-details-grid">
              <div className="animal-details-grid__item">
                <dt>{t('animals.table.tag')}</dt>
                <dd>{animal.tag}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{t('animals.table.breed')}</dt>
                <dd>{animal.breed}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{t('animals.table.birthDate')}</dt>
                <dd>{animal.birthDate}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{t('animals.table.origin')}</dt>
                <dd>{t(`animals.origins.${animal.origin}`)}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{appendCurrencyCode(t('animals.form.acquisitionCost'), currency)}</dt>
                <dd>{formatDisplayMoney(animal.acquisitionCost, language, currency) ?? t('animals.noAcquisitionCost')}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{appendCurrencyCode(t('animals.form.salePrice'), currency)}</dt>
                <dd>{formatDisplayMoney(animal.salePrice, language, currency) ?? t('animals.noSaleData')}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{t('animals.form.saleDate')}</dt>
                <dd>{animal.saleDate ?? t('animals.noSaleData')}</dd>
              </div>
              <div className="animal-details-grid__item">
                <dt>{t('animals.table.status')}</dt>
                <dd>
                  <span
                    className={`animals-table__status animals-table__status--${animal.status.toLowerCase()}`}
                  >
                    {t(`animals.statuses.${animal.status}`)}
                  </span>
                </dd>
              </div>
            </dl>
          </article>

          <article className="animals-panel animals-panel--table">
            <div className="animals-panel__header">
              <div>
                <h2>{t('animals.detailsSections.productionTitle')}</h2>
                <p>{t('animals.detailsSections.productionDescription')}</p>
              </div>
            </div>

            {productions.length === 0 && (
              <p className="animals-page__status">{t('animals.detailsSections.productionEmpty')}</p>
            )}

            {productions.length > 0 && (
              <div className="animals-table-wrapper">
                <table className="animals-table">
                  <thead>
                    <tr>
                      <th>{t('production.table.date')}</th>
                      <th>{t('production.table.quantity')}</th>
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
                <h2>{t('animals.detailsSections.feedingTitle')}</h2>
                <p>{t('animals.detailsSections.feedingDescription')}</p>
              </div>
            </div>

            {feedings.length === 0 && (
              <p className="animals-page__status">{t('animals.detailsSections.feedingEmpty')}</p>
            )}

            {feedings.length > 0 && (
              <div className="animals-table-wrapper">
                <table className="animals-table">
                  <thead>
                    <tr>
                      <th>{t('production.table.date')}</th>
                      <th>{t('production.table.quantity')}</th>
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
