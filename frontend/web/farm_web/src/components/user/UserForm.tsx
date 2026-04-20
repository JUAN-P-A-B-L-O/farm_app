import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import type { Farm } from '../../types/farm'
import type { UserFormData } from '../../types/user'

const roleOptions = ['MANAGER', 'WORKER']

interface UserFormProps {
  initialValues: UserFormData
  farms: Farm[]
  isLoadingFarms: boolean
  onSubmit: (data: UserFormData) => Promise<void>
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function UserForm({
  initialValues,
  farms,
  isLoadingFarms,
  onSubmit,
  isSubmitting,
  submitLabel,
  errorMessage,
}: UserFormProps) {
  const { t } = useTranslation()
  const [formData, setFormData] = useState<UserFormData>(initialValues)
  const [validationMessage, setValidationMessage] = useState('')

  useEffect(() => {
    setFormData(initialValues)
    setValidationMessage('')
  }, [initialValues])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name } = event.target
    const value =
      event.target instanceof HTMLInputElement && event.target.type === 'checkbox'
        ? event.target.checked
        : event.target instanceof HTMLSelectElement && event.target.multiple
          ? Array.from(event.target.selectedOptions, (option) => option.value)
          : event.target.value

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
      ...(name === 'active' && value === false ? { password: '' } : {}),
    }))

    if (validationMessage) {
      setValidationMessage('')
    }
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    const payload = {
      name: formData.name.trim(),
      email: formData.email.trim(),
      role: formData.role.trim(),
      password: formData.password.trim(),
      active: formData.active,
      farmIds: formData.farmIds,
    }

    if (!payload.name) {
      setValidationMessage(t('accessControl.errors.nameRequired'))
      return
    }

    if (!payload.email) {
      setValidationMessage(t('accessControl.errors.emailRequired'))
      return
    }

    if (!payload.role) {
      setValidationMessage(t('accessControl.errors.roleRequired'))
      return
    }

    if (payload.active && !payload.password) {
      setValidationMessage(t('accessControl.errors.passwordRequired'))
      return
    }

    if (payload.farmIds.length === 0) {
      setValidationMessage(t('accessControl.errors.farmsRequired'))
      return
    }

    setValidationMessage('')
    await onSubmit(payload)
  }

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>{t('accessControl.form.name')}</span>
          <input
            name="name"
            type="text"
            value={formData.name}
            onChange={handleChange}
            placeholder="Maria Silva"
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('accessControl.form.email')}</span>
          <input
            name="email"
            type="email"
            value={formData.email}
            onChange={handleChange}
            placeholder="maria.silva@example.com"
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('accessControl.form.role')}</span>
          <select name="role" value={formData.role} onChange={handleChange} required>
            <option value="">{t('accessControl.form.selectRole')}</option>
            {roleOptions.map((role) => (
              <option key={role} value={role}>
                {role}
              </option>
            ))}
          </select>
        </label>

        <label className="animal-form__field animal-form__field--checkbox">
          <span>{t('accessControl.form.active')}</span>
          <input
            name="active"
            type="checkbox"
            checked={formData.active}
            onChange={handleChange}
          />
        </label>

        {formData.active && (
          <label className="animal-form__field">
            <span>{t('accessControl.form.password')}</span>
            <input
              name="password"
              type="password"
              value={formData.password}
              onChange={handleChange}
              placeholder="farmapp@123"
              required
            />
          </label>
        )}

        <label className="animal-form__field">
          <span>{t('accessControl.form.farms')}</span>
          <select
            name="farmIds"
            value={formData.farmIds}
            onChange={handleChange}
            multiple
            required
            disabled={isLoadingFarms || farms.length === 0}
          >
            {farms.map((farm) => (
              <option key={farm.id} value={farm.id}>
                {farm.name}
              </option>
            ))}
          </select>
          <small>
            {isLoadingFarms
              ? t('accessControl.form.loadingFarms')
              : farms.length === 0
                ? t('accessControl.form.noFarms')
                : t('accessControl.form.farmsHint')}
          </small>
        </label>
      </div>

      {validationMessage && (
        <p className="animal-form__feedback animal-form__feedback--error">
          {validationMessage}
        </p>
      )}

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

export default UserForm
