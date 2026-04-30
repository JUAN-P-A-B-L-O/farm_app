import { useState, type ChangeEvent, type FormEvent } from 'react'
import { useCurrency } from '../../hooks/useCurrency'
import { useMeasurementUnits } from '../../hooks/useMeasurementUnits'
import { useTranslation } from '../../hooks/useTranslation'
import type { FeedTypeFormData } from '../../types/feedType'
import { hasAtMostTwoDecimals, parseTwoDecimalInput } from '../../utils/decimal'
import { appendCurrencyCode } from '../../utils/currency'
import {
  convertFeedCostFromBase,
  convertFeedCostToBase,
  getFeedCostInputStep,
  getMeasurementUnitShortLabelKey,
} from '../../utils/measurementUnits'

interface FeedTypeFormProps {
  initialValues: FeedTypeFormData
  onSubmit: (data: FeedTypeFormData) => Promise<void>
  onCancel?: () => void
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function FeedTypeForm({
  initialValues,
  onSubmit,
  onCancel,
  isSubmitting,
  submitLabel,
  errorMessage,
}: FeedTypeFormProps) {
  const { t } = useTranslation()
  const { currency } = useCurrency()
  const { feedingUnit } = useMeasurementUnits()
  const [formData, setFormData] = useState<FeedTypeFormData>(() => initialValues)
  const [validationMessage, setValidationMessage] = useState('')

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target
    setValidationMessage('')

    setFormData((currentData) => {
      if (name !== 'costPerKg') {
        return {
          ...currentData,
          [name]: value,
        }
      }

      const currentDisplayCost = convertFeedCostFromBase(currentData.costPerKg, feedingUnit)
      const nextDisplayCost = feedingUnit === 'GRAM'
        ? value === ''
          ? 0
          : Number.isFinite(Number(value))
            ? Number(value)
            : currentDisplayCost
        : parseTwoDecimalInput(value, currentDisplayCost)

      return {
        ...currentData,
        costPerKg: convertFeedCostToBase(nextDisplayCost, feedingUnit),
      }
    })
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!Number.isFinite(formData.costPerKg) || formData.costPerKg <= 0) {
      setValidationMessage(t('feedType.errors.costPerKg'))
      return
    }

    if (!hasAtMostTwoDecimals(formData.costPerKg)) {
      setValidationMessage(
        feedingUnit === 'GRAM'
          ? t('measurementUnits.errors.feedCostStep')
          : t('feedType.errors.costPrecision'),
      )
      return
    }

    await onSubmit(formData)
  }

  const unitLabel = t(getMeasurementUnitShortLabelKey(feedingUnit))
  const costLabel = `${appendCurrencyCode(t('feedType.form.costPerKg'), currency)} / ${unitLabel}`
  const displayCost = convertFeedCostFromBase(formData.costPerKg, feedingUnit)

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>{t('feedType.form.name')}</span>
          <input
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            placeholder={t('feedType.form.placeholders.name')}
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{costLabel}</span>
          <input
            name="costPerKg"
            type="number"
            min="0"
            step={getFeedCostInputStep(feedingUnit)}
            value={displayCost}
            onChange={handleChange}
            placeholder={t('common.placeholders.numericValue')}
            required
          />
        </label>
      </div>

      {(validationMessage || errorMessage) && (
        <p className="animal-form__feedback animal-form__feedback--error">
          {validationMessage || errorMessage}
        </p>
      )}

      <div className="animal-form__actions">
        <button type="submit" disabled={isSubmitting}>
          {isSubmitting ? t('common.saving') : submitLabel}
        </button>

        {onCancel && (
          <button
            type="button"
            className="animal-form__secondary-button"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            {t('common.cancel')}
          </button>
        )}
      </div>
    </form>
  )
}

export default FeedTypeForm
