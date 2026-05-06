import axios from 'axios'
import { clearAuthSession, getStoredToken } from './authStorage'

type UnauthorizedHandler = () => void

let unauthorizedHandler: UnauthorizedHandler | null = null
let isHandlingUnauthorized = false
const PUBLIC_PATH_PREFIXES = ['/auth/', '/v3/api-docs/', '/swagger-ui/', '/swagger-ui.html']

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

function isPublicRequest(url: string) {
  return PUBLIC_PATH_PREFIXES.some((prefix) => url.startsWith(prefix))
}

const api = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
});


api.interceptors.request.use((config) => {
  const token = getStoredToken()
  const requestUrl = config.url ?? ''

  if (token && !isPublicRequest(requestUrl)) {
    config.headers.Authorization = `Bearer ${token}`
  } else if (config.headers?.Authorization) {
    delete config.headers.Authorization
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
