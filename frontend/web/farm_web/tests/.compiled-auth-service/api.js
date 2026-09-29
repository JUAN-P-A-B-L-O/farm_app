import axios from 'axios'
import { clearAuthSession, getStoredToken } from './authStorage.js'

let unauthorizedHandler = null
let isHandlingUnauthorized = false
const PUBLIC_PATH_PREFIXES = ['/auth/', '/v3/api-docs/', '/swagger-ui/', '/swagger-ui.html']

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

function isPublicRequest(url) {
  return PUBLIC_PATH_PREFIXES.some((prefix) => url.startsWith(prefix))
}

const api = axios.create({
  baseURL: globalThis.__TEST_VITE_API_URL__ || 'http://localhost:8080',
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
