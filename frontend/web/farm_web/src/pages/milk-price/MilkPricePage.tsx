import { useEffect, useState, type FormEvent } from 'react'
import axios from 'axios'
import ExportCsvButton from '../../components/common/ExportCsvButton'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import {
  createMilkPrice,
  exportMilkPriceHistoryCsv,
  getCurrentMilkPrice,
  getMilkPriceHistory,
} from '../../services/milkPriceService'
import type {
  CreateMilkPricePayload,
  MilkPrice,
  MilkPriceApiErrorResponse,
} from '../../types/milkPrice'
import '../../App.css'

const emptyForm: CreateMilkPricePayload = {
  price: 0,
  effectiveDate: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<MilkPriceApiErrorResponse>(error)) {
    return error.response?.data?.error ?? fallbackMessage
  }

  return fallbackMessage
}

function formatCurrency(value: number, language: string) {
  return new Intl.NumberFormat(language === 'pt-BR' ? 'pt-BR' : 'en-US', {
    style: 'currency',
    currency: language === 'pt-BR' ? 'BRL' : 'USD',
    minimumFractionDigits: 2,
    maximumFractionDigits: 2,
  }).format(value)
}

function MilkPricePage() {
  const { selectedFarmId } = useFarm()
  const { t, language } = useTranslation()
  const [currentPrice, setCurrentPrice] = useState<MilkPrice | null>(null)
  const [history, setHistory] = useState<MilkPrice[]>([])
  const [formData, setFormData] = useState<CreateMilkPricePayload>(emptyForm)
  const [isLoading, setIsLoading] = useState(true)
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isExporting, setIsExporting] = useState(false)
  const [listErrorMessage, setListErrorMessage] = useState('')
  const [formErrorMessage, setFormErrorMessage] = useState('')

  async function loadMilkPrices() {
    if (!selectedFarmId) {
      setCurrentPrice(null)
      setHistory([])
      setListErrorMessage('')
      setIsLoading(false)
      return
    }

    setIsLoading(true)
    setListErrorMessage('')

    try {
      const [current, priceHistory] = await Promise.all([
        getCurrentMilkPrice(selectedFarmId),
        getMilkPriceHistory(selectedFarmId),
      ])

      setCurrentPrice(current)
      setHistory(priceHistory)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('milkPrice.errors.load')))
    } finally {
      setIsLoading(false)
    }
  }

  useEffect(() => {
    void loadMilkPrices()
  }, [selectedFarmId])

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!selectedFarmId) {
      setFormErrorMessage(t('milkPrice.errors.selectFarm'))
      return
    }

    if (!formData.effectiveDate || !Number.isFinite(formData.price) || formData.price <= 0) {
      setFormErrorMessage(t('milkPrice.errors.invalid'))
      return
    }

    setIsSubmitting(true)
    setFormErrorMessage('')

    try {
      await createMilkPrice(formData, selectedFarmId)
      setFormData(emptyForm)
      await loadMilkPrices()
    } catch (error) {
      setFormErrorMessage(getErrorMessage(error, t('milkPrice.errors.create')))
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleExport() {
    if (!selectedFarmId) {
      return
    }

    setIsExporting(true)
    setListErrorMessage('')

    try {
      await exportMilkPriceHistoryCsv(selectedFarmId)
    } catch (error) {
      setListErrorMessage(getErrorMessage(error, t('common.exportError')))
    } finally {
      setIsExporting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('milkPrice.eyebrow')}</p>
        <h1>{t('milkPrice.title')}</h1>
        <p className="animals-page__description">{t('milkPrice.description')}</p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('milkPrice.currentTitle')}</h2>
              <p>{t('milkPrice.currentDescription')}</p>
            </div>
          </div>

          {isLoading && <p className="animals-page__status">{t('milkPrice.loading')}</p>}

          {listErrorMessage && (
            <p className="animals-page__status animals-page__status--error">{listErrorMessage}</p>
          )}

          {!isLoading && !listErrorMessage && currentPrice && (
            <>
              <dl className="animal-details-grid">
                <div className="animal-details-grid__item">
                  <dt>{t('milkPrice.summary.currentPrice')}</dt>
                  <dd>{formatCurrency(currentPrice.price, language)}</dd>
                </div>
                <div className="animal-details-grid__item">
                  <dt>{t('milkPrice.summary.effectiveDate')}</dt>
                  <dd>{currentPrice.effectiveDate ?? t('milkPrice.summary.defaultFallback')}</dd>
                </div>
              </dl>

              {currentPrice.fallbackDefault && (
                <p className="animals-page__status">{t('milkPrice.fallbackNotice')}</p>
              )}
            </>
          )}

          <div className="animals-panel__header" style={{ marginTop: 24 }}>
            <div>
              <h2>{t('milkPrice.createTitle')}</h2>
              <p>{t('milkPrice.createDescription')}</p>
            </div>
          </div>

          <form className="animal-form" onSubmit={handleSubmit}>
            <div className="animal-form__grid">
              <label className="animal-form__field" htmlFor="milk-price-value">
                <span>{t('milkPrice.form.price')}</span>
                <input
                  id="milk-price-value"
                  type="number"
                  min="0.01"
                  step="0.01"
                  value={formData.price || ''}
                  onChange={(event) =>
                    setFormData((current) => ({ ...current, price: Number(event.target.value) }))
                  }
                />
              </label>

              <label className="animal-form__field" htmlFor="milk-price-effective-date">
                <span>{t('milkPrice.form.effectiveDate')}</span>
                <input
                  id="milk-price-effective-date"
                  type="date"
                  value={formData.effectiveDate}
                  onChange={(event) =>
                    setFormData((current) => ({ ...current, effectiveDate: event.target.value }))
                  }
                />
              </label>
            </div>

            {formErrorMessage && (
              <p className="animal-form__feedback animal-form__feedback--error">{formErrorMessage}</p>
            )}

            <div className="animal-form__actions">
              <button type="submit" disabled={isSubmitting}>
                {isSubmitting ? t('milkPrice.submitting') : t('milkPrice.submit')}
              </button>
            </div>
          </form>
        </article>

        <article className="animals-panel animals-panel--table">
          <div className="animals-panel__header animals-panel__header--actions">
            <div>
              <h2>{t('milkPrice.historyTitle')}</h2>
              <p>{t('milkPrice.historyDescription')}</p>
            </div>
            <ExportCsvButton
              onClick={() => void handleExport()}
              label={t('common.exportCsv')}
              loadingLabel={t('common.exportingCsv')}
              isLoading={isExporting}
              disabled={!selectedFarmId || isLoading || history.length === 0}
            />
          </div>

          {isLoading && <p className="animals-page__status">{t('milkPrice.loading')}</p>}

          {!isLoading && !listErrorMessage && history.length === 0 && (
            <p className="animals-page__status">{t('milkPrice.empty')}</p>
          )}

          {!isLoading && !listErrorMessage && history.length > 0 && (
            <div className="animals-table-wrapper">
              <table className="animals-table">
                <thead>
                  <tr>
                    <th>{t('milkPrice.table.price')}</th>
                    <th>{t('milkPrice.table.effectiveDate')}</th>
                    <th>{t('milkPrice.table.createdAt')}</th>
                  </tr>
                </thead>
                <tbody>
                  {history.map((entry) => (
                    <tr key={entry.id ?? `${entry.effectiveDate}-${entry.price}`}>
                      <td>{formatCurrency(entry.price, language)}</td>
                      <td>{entry.effectiveDate ?? '-'}</td>
                      <td>{entry.createdAt ? entry.createdAt.replace('T', ' ') : '-'}</td>
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

export default MilkPricePage
