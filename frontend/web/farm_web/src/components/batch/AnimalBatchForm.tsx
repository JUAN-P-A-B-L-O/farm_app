import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import type { AnimalBatchFormData } from '../../types/animalBatch'
import type { Animal } from '../../types/animal'

interface AnimalBatchFormProps {
  initialValues: AnimalBatchFormData
  animals: Animal[]
  onSubmit: (data: AnimalBatchFormData) => Promise<void>
  onCancel?: () => void
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function AnimalBatchForm({
  initialValues,
  animals,
  onSubmit,
  onCancel,
  isSubmitting,
  submitLabel,
  errorMessage,
}: AnimalBatchFormProps) {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<AnimalBatchFormData>(initialValues)
  const [validationMessage, setValidationMessage] = useState('')

  useEffect(() => {
    setFormData(initialValues)
    setValidationMessage('')
  }, [initialValues])

  function handleNameChange(event: ChangeEvent<HTMLInputElement>) {
    setValidationMessage('')
    setFormData((current) => ({
      ...current,
      name: event.target.value,
    }))
  }

  function handleAnimalIdsChange(event: ChangeEvent<HTMLSelectElement>) {
    setValidationMessage('')
    setFormData((current) => ({
      ...current,
      animalIds: Array.from(event.target.selectedOptions, (option) => option.value),
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!formData.name.trim()) {
      setValidationMessage(t('batches.errors.nameRequired'))
      return
    }

    if (formData.animalIds.length === 0) {
      setValidationMessage(t('batches.errors.animalsRequired'))
      return
    }

    await onSubmit({
      name: formData.name.trim(),
      animalIds: [...formData.animalIds],
    })
  }

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>{t('batches.form.name')}</span>
          <input
            name="name"
            type="text"
            value={formData.name}
            onChange={handleNameChange}
            placeholder={t('batches.form.placeholders.name')}
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('batches.form.animals')}</span>
          <select
            name="animalIds"
            multiple
            size={Math.min(Math.max(animals.length, 3), 8)}
            value={formData.animalIds}
            onChange={handleAnimalIdsChange}
            disabled={isSubmitting || animals.length === 0}
            required
          >
            {animals.map((animal) => (
              <option key={animal.id} value={animal.id}>
                {animal.tag}
              </option>
            ))}
          </select>
          <small className="listing-filters__help">{t('batches.form.animalsHelp')}</small>
        </label>
      </div>

      {(validationMessage || errorMessage) && (
        <p className="animal-form__feedback animal-form__feedback--error">
          {validationMessage || errorMessage}
        </p>
      )}

      <div className="animal-form__actions">
        <button type="submit" disabled={isSubmitting || animals.length === 0}>
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

export default AnimalBatchForm
