import { useEffect, useState, type ChangeEvent, type FormEvent } from 'react'
import type { ProductionAnimalOption, ProductionFormData } from '../../types/production'

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

  useEffect(() => {
    setFormData(initialValues)
  }, [initialValues])

  function handleChange(event: ChangeEvent<HTMLInputElement | HTMLSelectElement>) {
    const { name, value } = event.target

    setFormData((currentData) => ({
      ...currentData,
      [name]: name === 'quantity' ? Number(value) : value,
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
          <span>Animal</span>
          <select
            name="animalId"
            value={formData.animalId}
            onChange={handleChange}
            required
            disabled={isSubmitting || animals.length === 0}
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

      {errorMessage && (
        <p className="animal-form__feedback animal-form__feedback--error">
          {errorMessage}
        </p>
      )}

      <div className="animal-form__actions">
        <button type="submit" disabled={isSubmitting || animals.length === 0}>
          {isSubmitting ? 'Saving...' : submitLabel}
        </button>
      </div>
    </form>
  )
}

export default ProductionForm
