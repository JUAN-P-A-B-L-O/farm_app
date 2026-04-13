import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import type { FarmFormData } from '../../types/farm'

interface FarmFormProps {
  initialValues: FarmFormData
  onSubmit: (data: FarmFormData) => Promise<void>
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function FarmForm({
  initialValues,
  onSubmit,
  isSubmitting,
  submitLabel,
  errorMessage,
}: FarmFormProps) {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<FarmFormData>(initialValues)

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
    await onSubmit({
      name: formData.name.trim(),
    })
  }

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>{t('farm.form.name')}</span>
          <input
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            placeholder={t('farm.form.placeholder')}
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
      </div>
    </form>
  )
}

export default FarmForm
