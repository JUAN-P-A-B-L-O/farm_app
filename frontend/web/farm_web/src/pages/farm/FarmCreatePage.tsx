import { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import FarmForm from '../../components/farm/FarmForm'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { createFarm } from '../../services/farmService'
import type { FarmApiErrorResponse, FarmFormData } from '../../types/farm'
import '../../App.css'

const emptyFarmForm: FarmFormData = {
  name: '',
}

function getErrorMessage(error: unknown, fallbackMessage: string, t: (key: string) => string): string {
  if (axios.isAxiosError<FarmApiErrorResponse>(error)) {
    const status = error.response?.status
    const apiMessage = error.response?.data?.error

    if (status === 400) {
      return apiMessage ?? t('farm.errors.validationSave')
    }

    if (status === 401) {
      return apiMessage ?? t('farm.errors.unauthorized')
    }

    if (apiMessage) {
      return apiMessage
    }
  }

  return fallbackMessage
}

function FarmCreatePage() {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { refreshFarms } = useFarm()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  async function handleCreateFarm(data: FarmFormData) {
    setIsSubmitting(true)
    setErrorMessage('')

    try {
      await createFarm(data)
      await refreshFarms()
      navigate('/dashboard', { replace: true })
    } catch (error) {
      setErrorMessage(getErrorMessage(error, t('farm.errors.create'), t))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('farm.eyebrow')}</p>
        <h1>{t('farm.createTitle')}</h1>
        <p className="animals-page__description">{t('farm.createDescription')}</p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('farm.formTitle')}</h2>
              <p>{t('farm.formDescription')}</p>
            </div>
          </div>

          <FarmForm
            initialValues={emptyFarmForm}
            onSubmit={handleCreateFarm}
            isSubmitting={isSubmitting}
            submitLabel={t('farm.submitCreate')}
            errorMessage={errorMessage}
          />
        </article>
      </section>
    </main>
  )
}

export default FarmCreatePage
