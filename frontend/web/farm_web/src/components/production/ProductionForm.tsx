import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import { getAllUsers } from '../../services/userService'
import type { ProductionAnimalOption, ProductionFormData } from '../../types/production'
import type { User } from '../../types/user'

interface ProductionFormProps {
  initialValues: ProductionFormData
  animals: ProductionAnimalOption[]
  onSubmit: (data: ProductionFormData) => Promise<void>
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function ProductionForm({
  initialValues,
  animals,
  onSubmit,
  isSubmitting,
  submitLabel,
  errorMessage,
}: ProductionFormProps) {
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
    async function loadUsers() {
      setIsUsersLoading(true)
      setUsersErrorMessage('')

      try {
        const usersData = await getAllUsers()
        setUsers(usersData)
      } catch {
        setUsersErrorMessage('Unable to load users.')
      } finally {
        setIsUsersLoading(false)
      }
    }

    void loadUsers()
  }, [])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target

    setValidationMessage('')
    setFormData((currentData) => ({
      ...currentData,
      [name]: name === 'quantity' ? Number(value) : value,
    }))
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!formData.animalId) {
      setValidationMessage('Select an animal before creating the production record.')
      return
    }

    if (!formData.date) {
      setValidationMessage('Select a production date before submitting.')
      return
    }

    if (!Number.isFinite(formData.quantity) || formData.quantity <= 0) {
      setValidationMessage('Quantity must be greater than zero.')
      return
    }

    if (!formData.userId) {
      setValidationMessage('Select a user before creating the production record.')
      return
    }

    await onSubmit(formData)
  }

  const isFormDisabled =
    isSubmitting || isUsersLoading || animals.length === 0 || users.length === 0 || usersErrorMessage.length > 0

  const feedbackMessage = validationMessage || usersErrorMessage || errorMessage

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>Animal</span>
          <select
            name="animalId"
            value={formData.animalId}
            onChange={handleChange}
            required
            disabled={isFormDisabled}
          >
            <option value="">Select an animal</option>
            {animals.map((animal) => (
              <option key={animal.id} value={animal.id}>
                {animal.tag}
              </option>
            ))}
          </select>
        </label>

        <label className="animal-form__field">
          <span>User</span>
          <select
            name="userId"
            value={formData.userId}
            onChange={handleChange}
            required
            disabled={isFormDisabled}
          >
            <option value="">
              {isUsersLoading ? 'Loading users...' : 'Select a user'}
            </option>
            {users.map((user) => (
              <option key={user.id} value={user.id}>
                {user.name}
              </option>
            ))}
          </select>
        </label>

        <label className="animal-form__field">
          <span>Date</span>
          <input
            name="date"
            type="date"
            value={formData.date}
            onChange={handleChange}
            required
          />
        </label>

        <label className="animal-form__field">
          <span>Quantity</span>
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
          {isSubmitting ? 'Saving...' : submitLabel}
        </button>
      </div>
    </form>
  )
}

export default ProductionForm
