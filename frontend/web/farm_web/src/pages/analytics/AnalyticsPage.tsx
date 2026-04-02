import { useEffect, useState } from 'react'
import axios from 'axios'
import LineChart, { type LineChartPoint } from '../../components/analytics/LineChart'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllAnimals } from '../../services/animalService'
import { getFeedingsByAnimalId } from '../../services/feedingService'
import { getProductionsByAnimalId } from '../../services/productionService'
import type { Animal, ApiErrorResponse } from '../../types/animal'
import type { FeedingTrendPoint } from '../../types/feeding'
import type { ProductionTrendPoint } from '../../types/production'
import '../../App.css'

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<ApiErrorResponse>(error)) {
    const apiMessage = error.response?.data?.error

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function mapAnimalsToOptions(animals: Animal[]) {
  return animals.map(({ id, tag }) => ({
    id,
    tag,
  }))
}

function mapTrendData(data: ProductionTrendPoint[] | FeedingTrendPoint[]): LineChartPoint[] {
  return [...data]
    .sort((firstItem, secondItem) => firstItem.date.localeCompare(secondItem.date))
    .map(({ date, quantity }) => ({
      date,
      quantity,
    }))
}

function AnalyticsPage() {
  const { t, language } = useTranslation()
  const [animals, setAnimals] = useState<Array<{ id: string; tag: string }>>([])
  const [selectedAnimalId, setSelectedAnimalId] = useState('')
  const [productionData, setProductionData] = useState<LineChartPoint[]>([])
  const [feedingData, setFeedingData] = useState<LineChartPoint[]>([])
  const [isAnimalsLoading, setIsAnimalsLoading] = useState(true)
  const [isChartsLoading, setIsChartsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  useEffect(() => {
    async function loadAnimals() {
      setIsAnimalsLoading(true)
      setErrorMessage('')

      try {
        const data = await getAllAnimals()
        setAnimals(mapAnimalsToOptions(data))
      } catch (error) {
        setErrorMessage(getErrorMessage(error, t('analytics.loadAnimalsError')))
      } finally {
        setIsAnimalsLoading(false)
      }
    }

    void loadAnimals()
  }, [language])

  useEffect(() => {
    async function loadAnalytics(animalId: string) {
      setIsChartsLoading(true)
      setErrorMessage('')

      try {
        const [productions, feedings] = await Promise.all([
          getProductionsByAnimalId(animalId),
          getFeedingsByAnimalId(animalId),
        ])

        setProductionData(mapTrendData(productions))
        setFeedingData(mapTrendData(feedings))
      } catch (error) {
        setErrorMessage(getErrorMessage(error, t('analytics.loadChartsError')))
        setProductionData([])
        setFeedingData([])
      } finally {
        setIsChartsLoading(false)
      }
    }

    if (!selectedAnimalId) {
      setProductionData([])
      setFeedingData([])
      setIsChartsLoading(false)
      return
    }

    void loadAnalytics(selectedAnimalId)
  }, [selectedAnimalId, language])

  const showCharts = !isChartsLoading && !errorMessage && selectedAnimalId

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('analytics.eyebrow')}</p>
        <h1>{t('analytics.title')}</h1>
        <p className="animals-page__description">
          {t('analytics.description')}
        </p>
      </section>

      <section className="analytics-layout">
        <article className="analytics-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('analytics.selectorTitle')}</h2>
              <p>{t('analytics.selectorDescription')}</p>
            </div>
          </div>

          <div className="analytics-controls">
            <label htmlFor="analytics-animal-select">
              {t('analytics.animalLabel')}
              <select
                id="analytics-animal-select"
                value={selectedAnimalId}
                onChange={(event) => setSelectedAnimalId(event.target.value)}
                disabled={isAnimalsLoading}
              >
                <option value="">{t('analytics.selectAnimal')}</option>
                {animals.map((animal) => (
                  <option key={animal.id} value={animal.id}>
                    {animal.tag}
                  </option>
                ))}
              </select>
            </label>
          </div>

          {isAnimalsLoading && <p className="animals-page__status">{t('analytics.loadingAnimals')}</p>}

          {!isAnimalsLoading && !errorMessage && animals.length === 0 && (
            <p className="animals-page__status">{t('analytics.emptyAnimals')}</p>
          )}

          {!selectedAnimalId && !isAnimalsLoading && animals.length > 0 && !errorMessage && (
            <p className="animals-page__status">{t('analytics.promptSelect')}</p>
          )}

          {isChartsLoading && <p className="animals-page__status">{t('analytics.loadingCharts')}</p>}

          {errorMessage && (
            <p className="animals-page__status animals-page__status--error">{errorMessage}</p>
          )}
        </article>

        <section className="analytics-grid">
          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>{t('analytics.productionTitle')}</h2>
              <p>{t('analytics.productionDescription')}</p>
            </div>

            {showCharts && productionData.length > 0 && (
              <LineChart data={productionData} color="#2e6a46" />
            )}

            {showCharts && productionData.length === 0 && (
              <p className="analytics-chart__empty">{t('analytics.productionEmpty')}</p>
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>{t('analytics.feedingTitle')}</h2>
              <p>{t('analytics.feedingDescription')}</p>
            </div>

            {showCharts && feedingData.length > 0 && (
              <LineChart data={feedingData} color="#c26b2c" />
            )}

            {showCharts && feedingData.length === 0 && (
              <p className="analytics-chart__empty">{t('analytics.feedingEmpty')}</p>
            )}
          </article>
        </section>
      </section>
    </main>
  )
}

export default AnalyticsPage
