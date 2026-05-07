import { useState, type FormEvent } from 'react'
import axios from 'axios'
import { Link, Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useTranslation } from '../../hooks/useTranslation'
import { registerAccount } from '../../services/authService'
import { isManager } from '../../utils/authorization'
import '../../App.css'

interface LoginLocationState {
  registrationSuccess?: boolean
  registrationEmail?: string
  from?: {
    pathname?: string
  }
}

function LoginPage() {
  const { isAuthenticated, login, user } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const locationState = (location.state as LoginLocationState | null) ?? null
  const isSignupMode = new URLSearchParams(location.search).get('mode') === 'signup'
  const [name, setName] = useState('')
  const [email, setEmail] = useState(locationState?.registrationEmail ?? '')
  const [password, setPassword] = useState('')
  const [errorMessage, setErrorMessage] = useState('')
  const [isSubmitting, setIsSubmitting] = useState(false)

  if (isAuthenticated) {
    return <Navigate to={isManager(user) ? '/dashboard' : '/animals'} replace />
  }

  async function handleSubmit(event: FormEvent<HTMLFormElement>) {
    event.preventDefault()
    setErrorMessage('')
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
      if (isSignupMode && axios.isAxiosError<{ error?: string }>(error)) {
        setErrorMessage(error.response?.data?.error ?? t('auth.signup.errors.generic'))
      } else if (axios.isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage(t('auth.errors.invalidCredentials'))
      } else {
        setErrorMessage(isSignupMode ? t('auth.signup.errors.generic') : t('auth.errors.generic'))
      }
    } finally {
      setIsSubmitting(false)
    }
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
          {!isSignupMode && locationState?.registrationSuccess && (
            <p className="animal-form__feedback animal-form__feedback--success">
              {t('auth.success.registration')}
            </p>
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
