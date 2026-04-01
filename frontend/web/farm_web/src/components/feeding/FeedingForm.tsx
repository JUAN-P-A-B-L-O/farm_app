import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import { getAllUsers } from '../../services/userService'
import type {
  FeedingAnimalOption,
  FeedingFeedTypeOption,
  FeedingFormData,
} from '../../types/feeding'
import type { User } from '../../types/user'

interface FeedingFormProps {
  initialValues: FeedingFormData
  animals: FeedingAnimalOption[]
  feedTypes: FeedingFeedTypeOption[]
  onSubmit: (data: FeedingFormData) => Promise<void>
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function FeedingForm({
  initialValues,
  animals,
  feedTypes,
  onSubmit,
  isSubmitting,
  submitLabel,
  errorMessage,
}: FeedingFormProps) {
  const { t, language } = useTranslation()
  const [formData, setFormData] = useState<FeedingFormData>(initialValues)
  const [users, setUsers] = useState<User[]>([])
  const [isUsersLoading, setIsUsersLoading] = useState(true)
  const [usersErrorMessage, setUsersErrorMessage] = useState('')
  const [validationMessage, setValidationMessage] = useState('')

  useEffect(() => {
    setFormData(initialValues)
    setValidationMessage('')
  }, [initialValues])

  useEffect(() => {
    async function loadUsers() {
      setIsUsersLoading(true)
      setUsersErrorMessage('')

      try {
        const usersData = await getAllUsers()
        setUsers(usersData)
      } catch {
        setUsersErrorMessage(t('feeding.errors.loadUsers'))
      } finally {
        setIsUsersLoading(false)
      }
    }

    void loadUsers()
  }, [language])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target

    if (name === 'userId') {
      setValidationMessage('')
    }

    setFormData((currentData) => ({
      ...currentData,
      [name]: name === 'quantity' ? Number(value) : value,
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!formData.userId) {
      setValidationMessage(t('feeding.errors.userRequired'))
      return
    }

    await onSubmit(formData)
  }

  const isFormDisabled =
    isSubmitting ||
    isUsersLoading ||
    animals.length === 0 ||
    feedTypes.length === 0 ||
    users.length === 0 ||
    usersErrorMessage.length > 0

  const feedbackMessage = validationMessage || usersErrorMessage || errorMessage

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>{t('feeding.form.animal')}</span>
          <select
            name="animalId"
            value={formData.animalId}
            onChange={handleChange}
            required
            disabled={isFormDisabled}
          >
            <option value="">{t('feeding.form.selectAnimal')}</option>
            {animals.map((animal) => (
              <option key={animal.id} value={animal.id}>
                {animal.tag}
              </option>
            ))}
          </select>
        </label>

        <label className="animal-form__field">
          <span>{t('feeding.form.feedType')}</span>
          <select
            name="feedTypeId"
            value={formData.feedTypeId}
            onChange={handleChange}
            required
            disabled={isFormDisabled}
          >
            <option value="">{t('feeding.form.selectFeedType')}</option>
            {feedTypes.map((feedType) => (
              <option key={feedType.id} value={feedType.id}>
                {feedType.name}
              </option>
            ))}
          </select>
        </label>

        <label className="animal-form__field">
          <span>{t('feeding.form.user')}</span>
          <select
            name="userId"
            value={formData.userId}
            onChange={handleChange}
            required
            disabled={isFormDisabled}
          >
            <option value="">
              {isUsersLoading ? t('feeding.form.loadingUsers') : t('feeding.form.selectUser')}
            </option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.name}
              </option>
            ))}
          </select>
        </label>

        <label className="animal-form__field">
          <span>{t('feeding.form.date')}</span>
          <input
            name="date"
            type="date"
            value={formData.date}
            onChange={handleChange}
            required
          />
        </label>

        <label className="animal-form__field">
          <span>{t('feeding.form.quantity')}</span>
          <input
            name="quantity"
            type="number"
            min="0"
            step="any"
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
      </div>
    </form>
  )
}

export default FeedingForm
