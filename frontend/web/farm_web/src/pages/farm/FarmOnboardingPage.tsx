import { useState } from 'react'
import axios from 'axios'
import { useNavigate } from 'react-router-dom'
import FarmForm from '../../components/farm/FarmForm'
import { useAuth } from '../../hooks/useAuth'
import { useFarm } from '../../hooks/useFarm'
import { useTranslation } from '../../hooks/useTranslation'
import { createFarm } from '../../services/farmService'
import type { FarmApiErrorResponse, FarmFormData } from '../../types/farm'
import { hasFeatureAccess } from '../../utils/planAccess'
import { isManager } from '../../utils/authorization'
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

function FarmOnboardingPage() {
  const navigate = useNavigate()
  const { t } = useTranslation()
  const { user } = useAuth()
  const { refreshFarms } = useFarm()
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  async function handleCreateFarm(data: FarmFormData) {
    setIsSubmitting(true)
    setErrorMessage('')

    try {
      await createFarm(data)
      await refreshFarms()

      if (isManager(user) && hasFeatureAccess(user, 'DASHBOARD')) {
        navigate('/dashboard', { replace: true })
        return
      }

      navigate('/animals', { replace: true })
    } catch (error) {
      setErrorMessage(getErrorMessage(error, t('farm.errors.create'), t))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="login-page">
      <section className="login-page__panel">
        <div className="login-page__header">
          <p className="login-page__eyebrow">{t('farm.eyebrow')}</p>
          <h1>{t('farm.onboardingTitle')}</h1>
          <p className="login-page__description">{t('farm.onboardingDescription')}</p>
        </div>

        <div className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('farm.onboardingFormTitle')}</h2>
              <p>{t('farm.onboardingFormDescription')}</p>
            </div>
          </div>

          <FarmForm
            initialValues={emptyFarmForm}
            onSubmit={handleCreateFarm}
            isSubmitting={isSubmitting}
            submitLabel={t('farm.onboardingSubmit')}
            errorMessage={errorMessage}
          />
        </div>
      </section>
    </main>
  )
}

export default FarmOnboardingPage
