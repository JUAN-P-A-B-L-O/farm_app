import { useEffect, useState, type FormEvent } from 'react'
import axios from 'axios'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useTranslation } from '../../hooks/useTranslation'
import {
  confirmAccountEmail,
  registerAccount,
  resendConfirmationEmail,
} from '../../services/authService'
import { isManager } from '../../utils/authorization'
import '../../App.css'

interface ApiErrorResponse {
  error?: string
}

interface LoginLocationState {
  registrationSuccess?: boolean
  registrationEmail?: string
  confirmationSuccess?: boolean
  from?: {
    pathname?: string
  }
}

function LoginPage() {
  const { isAuthenticated, login, user } = useAuth()
  const { t, language } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const locationState = (location.state as LoginLocationState | null) ?? null
  const searchParams = new URLSearchParams(location.search)
  const mode = searchParams.get('mode')
  const isSignupMode = mode === 'signup'
  const isConfirmationMode = mode === 'confirm'
  const confirmationToken = searchParams.get('token')?.trim() ?? ''
  const [name, setName] = useState('')
  const [email, setEmail] = useState(locationState?.registrationEmail ?? '')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [infoMessage, setInfoMessage] = useState(
    locationState?.confirmationSuccess
      ? t('auth.success.confirmation')
      : locationState?.registrationSuccess
        ? t('auth.success.registration')
        : '',
  )
  const [isSubmitting, setIsSubmitting] = useState(false)
  const [isResendingConfirmation, setIsResendingConfirmation] = useState(false)
  const [showResendConfirmation, setShowResendConfirmation] = useState(
    Boolean(locationState?.registrationSuccess),
  )
  const [confirmationStatus, setConfirmationStatus] = useState<'loading' | 'success' | 'error'>('loading')
  const [confirmationMessage, setConfirmationMessage] = useState(t('auth.confirmation.loading'))

  useEffect(() => {
    if (!isConfirmationMode) {
      return
    }

    if (!confirmationToken) {
      setConfirmationStatus('error')
      setConfirmationMessage(t('auth.confirmation.errors.invalidLink'))
      return
    }

    let isMounted = true

    confirmAccountEmail(confirmationToken)
      .then(() => {
        if (!isMounted) {
          return
        }

        setConfirmationStatus('success')
        setConfirmationMessage(t('auth.confirmation.success'))
      })
      .catch((error) => {
        if (!isMounted) {
          return
        }

        if (axios.isAxiosError<ApiErrorResponse>(error)) {
          setConfirmationMessage(error.response?.data?.error ?? t('auth.confirmation.errors.generic'))
        } else {
          setConfirmationMessage(t('auth.confirmation.errors.generic'))
        }
        setConfirmationStatus('error')
      })

    return () => {
      isMounted = false
    }
  }, [confirmationToken, isConfirmationMode, language])

  if (isAuthenticated) {
    return <Navigate to={isManager(user) ? '/dashboard' : '/animals'} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage('')
    setInfoMessage('')
    setShowResendConfirmation(false)
    setIsSubmitting(true)

    try {
      if (isSignupMode) {
        await registerAccount({ name, email, password })
        navigate('/login', {
          replace: true,
          state: {
            registrationSuccess: true,
            registrationEmail: email.trim(),
          },
        })
        return
      }

      await login(email, password)

      const requestedPath = (location.state as LoginLocationState | null)?.from?.pathname
      const nextPath = requestedPath ?? '/dashboard'

      navigate(nextPath, { replace: true })
    } catch (error) {
      if (isSignupMode && axios.isAxiosError<ApiErrorResponse>(error)) {
        setErrorMessage(error.response?.data?.error ?? t('auth.signup.errors.generic'))
      } else if (axios.isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage(t('auth.errors.invalidCredentials'))
      } else if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.status === 403) {
        setErrorMessage(error.response?.data?.error ?? t('auth.errors.confirmationRequired'))
        setShowResendConfirmation(true)
      } else if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.error) {
        setErrorMessage(error.response.data.error)
      } else {
        setErrorMessage(isSignupMode ? t('auth.signup.errors.generic') : t('auth.errors.generic'))
      }
    } finally {
      setIsSubmitting(false)
    }
  }

  async function handleResendConfirmation() {
    setErrorMessage('')
    setInfoMessage('')
    setIsResendingConfirmation(true)

    try {
      await resendConfirmationEmail(email)
      setInfoMessage(t('auth.success.confirmationResent'))
      setShowResendConfirmation(true)
    } catch (error) {
      if (axios.isAxiosError<ApiErrorResponse>(error) && error.response?.data?.error) {
        setErrorMessage(error.response.data.error)
      } else {
        setErrorMessage(t('auth.errors.resendConfirmation'))
      }
    } finally {
      setIsResendingConfirmation(false)
    }
  }

  if (isConfirmationMode) {
    return (
      <main className="login-page">
        <section className="login-page__panel">
          <div className="login-page__header">
            <p className="login-page__eyebrow">{t('auth.eyebrow')}</p>
            <h1>{t('auth.confirmation.title')}</h1>
            <p className="login-page__description">{t('auth.confirmation.description')}</p>
          </div>

          <p className={`animal-form__feedback ${confirmationStatus === 'success' ? 'animal-form__feedback--success' : 'animal-form__feedback--error'}`}>
            {confirmationMessage}
          </p>

          <p className="login-page__supporting-action">
            <Link
              className="login-page__supporting-link"
              to="/login"
              state={confirmationStatus === 'success' ? { confirmationSuccess: true } : undefined}
            >
              {confirmationStatus === 'success'
                ? t('auth.confirmation.loginAction')
                : t('auth.confirmation.backToLogin')}
            </Link>
          </p>
        </section>
      </main>
    )
  }

  return (
    <main className="login-page">
      <section className="login-page__panel">
        <div className="login-page__header">
          <p className="login-page__eyebrow">{t('auth.eyebrow')}</p>
          <h1>{isSignupMode ? t('auth.signup.title') : t('auth.title')}</h1>
          <p className="login-page__description">
            {isSignupMode ? t('auth.signup.description') : t('auth.description')}
          </p>
        </div>

        <form className="animal-form" onSubmit={handleSubmit}>
          {isSignupMode && (
            <label className="animal-form__field" htmlFor="name">
              {t('auth.signup.form.name')}
              <input
                id="name"
                type="text"
                value={name}
                onChange={(event) => setName(event.target.value)}
                autoComplete="name"
                required
              />
            </label>
          )}

          <label className="animal-form__field" htmlFor="email">
            {isSignupMode ? t('auth.signup.form.email') : t('auth.form.email')}
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              autoComplete="email"
              required
            />
          </label>

          <label className="animal-form__field" htmlFor="password">
            {isSignupMode ? t('auth.signup.form.password') : t('auth.form.password')}
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete={isSignupMode ? 'new-password' : 'current-password'}
              required
            />
          </label>

          {errorMessage && <p className="animal-form__feedback animal-form__feedback--error">{errorMessage}</p>}
          {!isSignupMode && infoMessage && (
            <p className="animal-form__feedback animal-form__feedback--success">
              {infoMessage}
            </p>
          )}
          {!isSignupMode && showResendConfirmation && (
            <div className="animal-form__actions">
              <button
                type="button"
                disabled={isResendingConfirmation || !email.trim()}
                onClick={handleResendConfirmation}
              >
                {isResendingConfirmation
                  ? t('auth.actions.resendingConfirmation')
                  : t('auth.actions.resendConfirmation')}
              </button>
            </div>
          )}

          <div className="animal-form__actions">
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting
                ? (isSignupMode ? t('auth.signup.submitting') : t('auth.submitting'))
                : (isSignupMode ? t('auth.signup.submit') : t('auth.submit'))}
            </button>
          </div>
        </form>

        <p className="login-page__supporting-action">
          {isSignupMode ? t('auth.signup.loginPrompt') : t('auth.signup.entryLabel')}{' '}
          <Link className="login-page__supporting-link" to={isSignupMode ? '/login' : '/login?mode=signup'}>
            {isSignupMode ? t('auth.signup.loginAction') : t('auth.signup.entryAction')}
          </Link>
        </p>
      </section>
    </main>
  )
}

export default LoginPage
