import { useState, type FormEvent } from 'react'
import axios from 'axios'
import { Navigate, useLocation, useNavigate } from 'react-router-dom'
import { useAuth } from '../../hooks/useAuth'
import { useTranslation } from '../../hooks/useTranslation'
import { isManager } from '../../utils/authorization'
import '../../App.css'

interface LoginLocationState {
  from?: {
    pathname?: string
  }
}

function LoginPage() {
  const { isAuthenticated, login, user } = useAuth()
  const { t } = useTranslation()
  const navigate = useNavigate()
  const location = useLocation()
  const [email, setEmail] = useState('')
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
      await login(email, password)

      const requestedPath = (location.state as LoginLocationState | null)?.from?.pathname
      const nextPath = requestedPath ?? '/dashboard'

      navigate(nextPath, { replace: true })
    } catch (error) {
      if (axios.isAxiosError(error) && error.response?.status === 401) {
        setErrorMessage(t('auth.errors.invalidCredentials'))
      } else {
        setErrorMessage(t('auth.errors.generic'))
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
          <h1>{t('auth.title')}</h1>
          <p className="login-page__description">{t('auth.description')}</p>
        </div>

        <form className="animal-form" onSubmit={handleSubmit}>
          <label className="animal-form__field" htmlFor="email">
            {t('auth.form.email')}
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
            {t('auth.form.password')}
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              autoComplete="current-password"
              required
            />
          </label>

          {errorMessage && <p className="animal-form__feedback animal-form__feedback--error">{errorMessage}</p>}

          <div className="animal-form__actions">
            <button type="submit" disabled={isSubmitting}>
              {isSubmitting ? t('auth.submitting') : t('auth.submit')}
            </button>
          </div>
        </form>
      </section>
    </main>
  )
}

export default LoginPage
