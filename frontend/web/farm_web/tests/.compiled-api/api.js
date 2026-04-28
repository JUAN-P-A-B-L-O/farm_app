import axios from 'axios'
import { clearAuthSession, getStoredToken } from './authStorage.js'

let unauthorizedHandler = null
let isHandlingUnauthorized = false

export function registerUnauthorizedHandler(handler) {
  unauthorizedHandler = handler
}

export function resetUnauthorizedHandling() {
  isHandlingUnauthorized = false
}

function shouldHandleUnauthorized(error) {
  if (!axios.isAxiosError(error)) {
    return false
  }

  const hasStoredToken = Boolean(getStoredToken())
  const requestUrl = error.config?.url ?? ''
  const isLoginRequest = requestUrl.includes('/auth/login')

  return hasStoredToken && !isLoginRequest && error.response?.status === 401
}

const api = axios.create({
  baseURL: 'http://localhost:8080',
})

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
