import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import type { AnimalFormData } from '../../types/animal'

interface AnimalFormProps {
  initialValues: AnimalFormData
  onSubmit: (data: AnimalFormData) => Promise<void>
  onCancel?: () => void
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function AnimalForm({
  initialValues,
  onSubmit,
  onCancel,
  isSubmitting,
  submitLabel,
  errorMessage,
}: AnimalFormProps) {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<AnimalFormData>(initialValues)

  useEffect(() => {
    setFormData(initialValues)
  }, [initialValues])

  function handleChange(event: ChangeEvent<HTMLInputElement>) {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
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
          <span>{t('animals.form.tag')}</span>
          <input
            name="tag"
            type="text"
            value={formData.tag}
            onChange={handleChange}
            placeholder="A-102"
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('animals.form.breed')}</span>
          <input
            name="breed"
            type="text"
            value={formData.breed}
            onChange={handleChange}
            placeholder="Holstein"
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('animals.form.birthDate')}</span>
          <input
            name="birthDate"
            type="date"
            value={formData.birthDate}
            onChange={handleChange}
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('animals.form.farmId')}</span>
          <input
            name="farmId"
            type="text"
            value={formData.farmId}
            onChange={handleChange}
            placeholder="farm-001"
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

export default AnimalForm
