import { useEffect, useState } from 'react'
import axios from 'axios'
import LineChart, { type LineChartPoint } from '../../components/analytics/LineChart'
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
        setErrorMessage(getErrorMessage(error, 'Unable to load animals.'))
      } finally {
        setIsAnimalsLoading(false)
      }
    }

    void loadAnimals()
  }, [])

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
        setErrorMessage(getErrorMessage(error, 'Unable to load analytics data.'))
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
  }, [selectedAnimalId])

  const showCharts = !isChartsLoading && !errorMessage && selectedAnimalId

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">Analytics</p>
        <h1>Animal Trends</h1>
        <p className="animals-page__description">
          Select an animal to compare production and feeding quantities over time.
        </p>
      </section>

      <section className="analytics-layout">
        <article className="analytics-panel">
          <div className="animals-panel__header">
            <div>
              <h2>Animal Selector</h2>
              <p>Choose an animal by tag to load production and feeding trends.</p>
            </div>
          </div>

          <div className="analytics-controls">
            <label htmlFor="analytics-animal-select">
              Animal
              <select
                id="analytics-animal-select"
                value={selectedAnimalId}
                onChange={(event) => setSelectedAnimalId(event.target.value)}
                disabled={isAnimalsLoading}
              >
                <option value="">Select an animal</option>
                {animals.map((animal) => (
                  <option key={animal.id} value={animal.id}>
                    {animal.tag}
                  </option>
                ))}
              </select>
            </label>
          </div>

          {isAnimalsLoading && <p className="animals-page__status">Loading animals...</p>}

          {!isAnimalsLoading && !errorMessage && animals.length === 0 && (
            <p className="animals-page__status">No animals available.</p>
          )}

          {!selectedAnimalId && !isAnimalsLoading && animals.length > 0 && !errorMessage && (
            <p className="animals-page__status">Select an animal to view analytics.</p>
          )}

          {isChartsLoading && <p className="animals-page__status">Loading analytics...</p>}

          {errorMessage && (
            <p className="animals-page__status animals-page__status--error">{errorMessage}</p>
          )}
        </article>

        <section className="analytics-grid">
          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>Production Over Time</h2>
              <p>Daily production quantity for the selected animal.</p>
            </div>

            {showCharts && productionData.length > 0 && (
              <LineChart data={productionData} color="#2e6a46" />
            )}

            {showCharts && productionData.length === 0 && (
              <p className="analytics-chart__empty">No production data found for this animal.</p>
            )}
          </article>

          <article className="analytics-panel analytics-chart">
            <div className="analytics-chart__header">
              <h2>Feeding Over Time</h2>
              <p>Daily feeding quantity for the selected animal.</p>
            </div>

            {showCharts && feedingData.length > 0 && (
              <LineChart data={feedingData} color="#c26b2c" />
            )}

            {showCharts && feedingData.length === 0 && (
              <p className="analytics-chart__empty">No feeding data found for this animal.</p>
            )}
          </article>
        </section>
      </section>
    </main>
  )
}

export default AnalyticsPage
