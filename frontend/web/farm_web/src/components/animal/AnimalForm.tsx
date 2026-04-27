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
  selectedFarmName?: string
  showStatusField?: boolean
}

function AnimalForm({
  initialValues,
  onSubmit,
  onCancel,
  isSubmitting,
  submitLabel,
  errorMessage,
  selectedFarmName,
  showStatusField = false,
}: AnimalFormProps) {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<AnimalFormData>(initialValues)
  const availableStatuses = formData.status === 'SOLD'
    ? ['ACTIVE', 'SOLD', 'DEAD', 'INACTIVE'] as const
    : ['ACTIVE', 'DEAD', 'INACTIVE'] as const

  useEffect(() => {
    setFormData(initialValues)
  }, [initialValues])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: name === 'acquisitionCost'
        ? (value === '' ? null : Number(value))
        : value,
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
            placeholder={t('animals.form.placeholders.breed')}
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
          <span>{t('animals.form.origin')}</span>
          <select name="origin" value={formData.origin} onChange={handleChange} required>
            <option value="BORN">{t('animals.origins.BORN')}</option>
            <option value="PURCHASED">{t('animals.origins.PURCHASED')}</option>
          </select>
        </label>

        {formData.origin === 'PURCHASED' && (
          <label className="animal-form__field">
            <span>{t('animals.form.acquisitionCost')}</span>
            <input
              name="acquisitionCost"
              type="number"
              min="0.01"
              step="0.01"
              value={formData.acquisitionCost ?? ''}
              onChange={handleChange}
              required
            />
          </label>
        )}

        {showStatusField && (
          <label className="animal-form__field">
            <span>{t('animals.form.status')}</span>
            <select name="status" value={formData.status ?? 'ACTIVE'} onChange={handleChange}>
              {availableStatuses.map((status) => (
                <option key={status} value={status}>{t(`animals.statuses.${status}`)}</option>
              ))}
            </select>
          </label>
        )}

        {selectedFarmName ? (
          <label className="animal-form__field">
            <span>{t('animals.form.farm')}</span>
            <input name="farmName" type="text" value={selectedFarmName} readOnly />
          </label>
        ) : (
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
        )}
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
