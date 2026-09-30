import { useState, type FormEvent } from 'react'
import axios from 'axios'
import { useLanguage } from '../../context/LanguageContext'
import { useCurrency } from '../../hooks/useCurrency'
import { useMeasurementUnits } from '../../hooks/useMeasurementUnits'
import { useTranslation } from '../../hooks/useTranslation'
import { updateOwnPassword } from '../../services/userService'
import type { UserApiErrorResponse } from '../../types/user'
import '../../App.css'

function getErrorMessage(error: unknown, fallbackMessage: string): string {
  if (axios.isAxiosError<UserApiErrorResponse>(error)) {
    return error.response?.data?.error ?? fallbackMessage
  }

  return fallbackMessage
}

function SettingsPage() {
  const { t } = useTranslation()
  const { language, setLanguage } = useLanguage()
  const { currency, setCurrency } = useCurrency()
  const { productionUnit, feedingUnit, setProductionUnit, setFeedingUnit } = useMeasurementUnits()
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [validationMessage, setValidationMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!currentPassword.trim()) {
      setValidationMessage(t('settings.errors.currentPasswordRequired'))
      setErrorMessage('')
      return
    }

    if (!newPassword.trim()) {
      setValidationMessage(t('settings.errors.newPasswordRequired'))
      setErrorMessage('')
      return
    }

    if (newPassword !== confirmPassword) {
      setValidationMessage(t('settings.errors.passwordMismatch'))
      setErrorMessage('')
      return
    }

    setIsSubmitting(true)
    setValidationMessage('')
    setErrorMessage('')

    try {
      await updateOwnPassword(currentPassword.trim(), newPassword.trim())
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
    } catch (error) {
      setErrorMessage(getErrorMessage(error, t('settings.errors.updatePassword')))
    } finally {
      setIsSubmitting(false)
    }
  }

  return (
    <main className="animals-page">
      <section className="animals-page__header">
        <p className="animals-page__eyebrow">{t('settings.eyebrow')}</p>
        <h1>{t('settings.title')}</h1>
        <p className="animals-page__description">
          {t('settings.description')}
        </p>
      </section>

      <section className="animals-layout">
        <article className="animals-panel">
          <div className="animals-panel__header">
            <h2>{t('settings.preferencesTitle')}</h2>
            <p>{t('settings.preferencesDescription')}</p>
          </div>

          <div className="settings-preferences__grid">
            <div className="animal-form__field" role="group" aria-label={t('layout.languageLabel')}>
              <span>{t('layout.languageLabel')}</span>
              <div className="app-layout__language-options">
                <button
                  type="button"
                  className={`app-layout__language-button${language === 'pt-BR' ? ' app-layout__language-button--active' : ''}`}
                  onClick={() => setLanguage('pt-BR')}
                  aria-pressed={language === 'pt-BR'}
                >
                  {t('layout.languageOptions.pt-BR')}
                </button>
                <button
                  type="button"
                  className={`app-layout__language-button${language === 'en' ? ' app-layout__language-button--active' : ''}`}
                  onClick={() => setLanguage('en')}
                  aria-pressed={language === 'en'}
                >
                  {t('layout.languageOptions.en')}
                </button>
              </div>
            </div>

            <label className="animal-form__field">
              <span>{t('layout.currencyLabel')}</span>
              <select value={currency} onChange={(event) => setCurrency(event.target.value as 'BRL' | 'USD')}>
                <option value="BRL">{t('layout.currencyOptions.BRL')}</option>
                <option value="USD">{t('layout.currencyOptions.USD')}</option>
              </select>
            </label>

            <label className="animal-form__field">
              <span>{t('measurementUnits.productionLabel')}</span>
              <select
                value={productionUnit}
                onChange={(event) => setProductionUnit(event.target.value as typeof productionUnit)}
              >
                <option value="LITER">{t('measurementUnits.options.LITER')}</option>
                <option value="MILLILITER">{t('measurementUnits.options.MILLILITER')}</option>
              </select>
            </label>

            <label className="animal-form__field">
              <span>{t('measurementUnits.feedingLabel')}</span>
              <select
                value={feedingUnit}
                onChange={(event) => setFeedingUnit(event.target.value as typeof feedingUnit)}
              >
                <option value="KILOGRAM">{t('measurementUnits.options.KILOGRAM')}</option>
                <option value="GRAM">{t('measurementUnits.options.GRAM')}</option>
              </select>
            </label>
          </div>
        </article>

        <article className="animals-panel">
          <div className="animals-panel__header">
            <div>
              <h2>{t('settings.passwordTitle')}</h2>
              <p>{t('settings.passwordDescription')}</p>
            </div>
          </div>

          <form className="animal-form" onSubmit={handleSubmit}>
            <div className="animal-form__grid">
              <label className="animal-form__field">
                <span>{t('settings.form.currentPassword')}</span>
                <input
                  type="password"
                  value={currentPassword}
                  onChange={(event) => setCurrentPassword(event.target.value)}
                  autoComplete="current-password"
                  required
                />
              </label>

              <label className="animal-form__field">
                <span>{t('settings.form.newPassword')}</span>
                <input
                  type="password"
                  value={newPassword}
                  onChange={(event) => setNewPassword(event.target.value)}
                  autoComplete="new-password"
                  required
                />
              </label>

              <label className="animal-form__field">
                <span>{t('settings.form.confirmPassword')}</span>
                <input
                  type="password"
                  value={confirmPassword}
                  onChange={(event) => setConfirmPassword(event.target.value)}
                  autoComplete="new-password"
                  required
                />
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
                {isSubmitting ? t('common.saving') : t('settings.submitPassword')}
              </button>
            </div>
          </form>
        </article>
      </section>
    </main>
  )
}

export default SettingsPage
