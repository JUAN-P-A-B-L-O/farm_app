import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import type { UserFormData } from '../../types/user'

const roleOptions = ['MANAGER', 'WORKER']

interface UserFormProps {
  initialValues: UserFormData
  onSubmit: (data: UserFormData) => Promise<void>
  onCancel?: () => void
  isSubmitting: boolean
  submitLabel: string
  errorMessage: string
}

function UserForm({
  initialValues,
  onSubmit,
  onCancel,
  isSubmitting,
  submitLabel,
  errorMessage,
}: UserFormProps) {
  const [formData, setFormData] = useState<UserFormData>(initialValues)
  const [validationMessage, setValidationMessage] = useState('')

  useEffect(() => {
    setFormData(initialValues)
    setValidationMessage('')
  }, [initialValues])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: value,
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
    }

    if (!payload.name) {
      setValidationMessage('Name is required.')
      return
    }

    if (!payload.email) {
      setValidationMessage('Email is required.')
      return
    }

    if (!payload.role) {
      setValidationMessage('Role is required.')
      return
    }

    setValidationMessage('')
    await onSubmit(payload)
  }

  return (
    <form className="animal-form" onSubmit={handleSubmit}>
      <div className="animal-form__grid">
        <label className="animal-form__field">
          <span>Name</span>
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
          <span>Email</span>
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
          <span>Role</span>
          <select name="role" value={formData.role} onChange={handleChange} required>
            <option value="">Select a role</option>
            {roleOptions.map((role) => (
              <option key={role} value={role}>
                {role}
              </option>
            ))}
          </select>
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
          {isSubmitting ? 'Saving...' : submitLabel}
        </button>

        {onCancel && (
          <button
            type="button"
            className="animal-form__secondary-button"
            onClick={onCancel}
            disabled={isSubmitting}
          >
            Cancel
          </button>
        )}
      </div>
    </form>
  )
}

export default UserForm
