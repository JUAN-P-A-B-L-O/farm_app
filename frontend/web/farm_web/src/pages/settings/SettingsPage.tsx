import { useState, type FormEvent } from 'react'
import axios from 'axios'
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
  const [currentPassword, setCurrentPassword] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')
  const [validationMessage, setValidationMessage] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [successMessage, setSuccessMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()

    if (!currentPassword.trim()) {
      setValidationMessage(t('settings.errors.currentPasswordRequired'))
      setErrorMessage('')
      setSuccessMessage('')
      return
    }

    if (!newPassword.trim()) {
      setValidationMessage(t('settings.errors.newPasswordRequired'))
      setErrorMessage('')
      setSuccessMessage('')
      return
    }

    if (newPassword !== confirmPassword) {
      setValidationMessage(t('settings.errors.passwordMismatch'))
      setErrorMessage('')
      setSuccessMessage('')
      return
    }

    setIsSubmitting(true)
    setValidationMessage('')
    setErrorMessage('')
    setSuccessMessage('')

    try {
      await updateOwnPassword(currentPassword.trim(), newPassword.trim())
      setCurrentPassword('')
      setNewPassword('')
      setConfirmPassword('')
      setSuccessMessage(t('settings.success.passwordUpdated'))
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

            {successMessage && (
              <p className="animal-form__feedback">
                {successMessage}
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
