import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import type { FeedTypeFormData } from '../../types/feedType'

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
  const [formData, setFormData] = useState<FeedTypeFormData>(initialValues)

  useEffect(() => {
    setFormData(initialValues)
  }, [initialValues])

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: name === 'costPerKg' ? Number(value) : value,
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    await onSubmit(formData)
  }

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
            placeholder="Corn silage"
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('feedType.form.costPerKg')}</span>
          <input
            name="costPerKg"
            type="number"
            min="0"
            step="any"
            value={formData.costPerKg}
            onChange={handleChange}
            placeholder="0"
            required
          />
        </label>
      </div>

      {errorMessage && (
        <p className="animal-form__feedback animal-form__feedback--error">
          {errorMessage}
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
