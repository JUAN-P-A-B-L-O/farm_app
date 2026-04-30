import axios from 'axios'
import { clearAuthSession, getStoredToken } from './authStorage'

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | null = null
let isHandlingUnauthorized = false

export function registerUnauthorizedHandler(handler: UnauthorizedHandler | null) {
  unauthorizedHandler = handler
}

export function resetUnauthorizedHandling() {
  isHandlingUnauthorized = false
}

function shouldHandleUnauthorized(error: unknown) {
  if (!axios.isAxiosError(error)) {
    return false
  }

  const hasStoredToken = Boolean(getStoredToken())
  const requestUrl = error.config?.url ?? ''
  const isLoginRequest = requestUrl.includes('/auth/login')

  return hasStoredToken && !isLoginRequest && error.response?.status === 401
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});

console.log('API URL:', import.meta.env.VITE_API_URL);

api.interceptors.request.use((config) => {
  const token = getStoredToken()

  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }

  return config
})

api.interceptors.response.use(
  (response) => response,
  async (error) => {
    if (shouldHandleUnauthorized(error) && !isHandlingUnauthorized) {
      isHandlingUnauthorized = true
      clearAuthSession()
      unauthorizedHandler?.()
    }

    return Promise.reject(error)
  },
)

export default api
