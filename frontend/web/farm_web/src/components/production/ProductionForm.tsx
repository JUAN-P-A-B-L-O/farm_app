import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllUsers } from '../../services/userService'
import { hasAtMostTwoDecimals, parseTwoDecimalInput } from '../../utils/decimal'
import type { ProductionAnimalOption, ProductionFormData } from '../../types/production'
import type { User } from '../../types/user'

interface ProductionFormProps {
  initialValues: ProductionFormData
  animals: ProductionAnimalOption[]
  onSubmit: (data: ProductionFormData) => Promise<void>
  onCancel?: () => void
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
  requireUserSelection?: boolean
  allowDateSelection?: boolean
}

function getCurrentDateInputValue() {
  const today = new Date()
  const year = today.getFullYear()
  const month = String(today.getMonth() + 1).padStart(2, '0')
  const day = String(today.getDate()).padStart(2, '0')

  return `${year}-${month}-${day}`
}

function ProductionForm({
  initialValues,
  animals,
  onSubmit,
  onCancel,
  isSubmitting,
  submitLabel,
  errorMessage,
  requireUserSelection = true,
  allowDateSelection = true,
}: ProductionFormProps) {
  const { t, language } = useTranslation()
  const [formData, setFormData] = useState<ProductionFormData>(initialValues)
  const [users, setUsers] = useState<User[]>([])
  const [isUsersLoading, setIsUsersLoading] = useState(true)
  const [usersErrorMessage, setUsersErrorMessage] = useState('')
  const [validationMessage, setValidationMessage] = useState('')

  useEffect(() => {
    setFormData(initialValues)
    setValidationMessage('')
  }, [initialValues])

  useEffect(() => {
    if (!requireUserSelection) {
      setIsUsersLoading(false)
      setUsersErrorMessage('')
      return
    }

    async function loadUsers() {
      setIsUsersLoading(true)
      setUsersErrorMessage('')

      try {
        const usersData = await getAllUsers()
        setUsers(usersData)
      } catch {
        setUsersErrorMessage(t('production.errors.loadUsers'))
      } finally {
        setIsUsersLoading(false)
      }
    }

    void loadUsers()
  }, [language, requireUserSelection])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target

    setValidationMessage('')
    setFormData((currentData) => ({
      ...currentData,
      [name]: name === 'quantity' ? parseTwoDecimalInput(value, currentData.quantity) : value,
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    const submissionDate = allowDateSelection ? formData.date : getCurrentDateInputValue()

    if (!formData.animalId) {
      setValidationMessage(t('production.errors.selectAnimal'))
      return
    }

    if (allowDateSelection && !submissionDate) {
      setValidationMessage(t('production.errors.selectDate'))
      return
    }

    if (!Number.isFinite(formData.quantity) || formData.quantity <= 0) {
      setValidationMessage(t('production.errors.quantity'))
      return
    }

    if (!hasAtMostTwoDecimals(formData.quantity)) {
      setValidationMessage(t('production.errors.quantityPrecision'))
      return
    }

    if (requireUserSelection && !formData.userId) {
      setValidationMessage(t('production.errors.selectUser'))
      return
    }

    await onSubmit({
      ...formData,
      date: submissionDate,
    })
  }

  const isFormDisabled =
    isSubmitting ||
    animals.length === 0 ||
    (requireUserSelection &&
      (isUsersLoading || users.length === 0 || usersErrorMessage.length > 0))

  const feedbackMessage =
    validationMessage || (requireUserSelection ? usersErrorMessage : '') || errorMessage

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>{t('production.form.animal')}</span>
          <select
            name="animalId"
            value={formData.animalId}
            onChange={handleChange}
            required
            disabled={isFormDisabled}
          >
            <option value="">{t('production.form.selectAnimal')}</option>
            {animals.map((animal) => (
              <option key={animal.id} value={animal.id}>
                {animal.tag}
              </option>
            ))}
          </select>
        </label>

        {requireUserSelection && (
          <label className="animal-form__field">
            <span>{t('production.form.user')}</span>
            <select
              name="userId"
              value={formData.userId}
              onChange={handleChange}
              required
              disabled={isFormDisabled}
            >
              <option value="">
                {isUsersLoading ? t('production.form.loadingUsers') : t('production.form.selectUser')}
              </option>
              {users.map((user) => (
                <option key={user.id} value={user.id}>
                  {user.name}
                </option>
              ))}
            </select>
          </label>
        )}

        {allowDateSelection && (
          <label className="animal-form__field">
            <span>{t('production.form.date')}</span>
            <input
              name="date"
              type="date"
              value={formData.date}
              onChange={handleChange}
              required
            />
          </label>
        )}

        <label className="animal-form__field">
          <span>{t('production.form.quantity')}</span>
          <input
            name="quantity"
            type="number"
            min="0"
            step="0.01"
            value={formData.quantity}
            onChange={handleChange}
            placeholder="0"
            required
          />
        </label>
      </div>

      {feedbackMessage && (
        <p className="animal-form__feedback animal-form__feedback--error">
          {feedbackMessage}
        </p>
      )}

      <div className="animal-form__actions">
        <button type="submit" disabled={isFormDisabled}>
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

export default ProductionForm
