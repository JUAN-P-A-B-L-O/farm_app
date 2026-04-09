import type { User } from '../types/user'

export const AUTH_TOKEN_STORAGE_KEY = 'auth_token'
export const AUTH_USER_STORAGE_KEY = 'auth_user'

export function persistAuthSession(accessToken: string, user: User) {
  window.localStorage.setItem(AUTH_TOKEN_STORAGE_KEY, accessToken)
  window.localStorage.setItem(AUTH_USER_STORAGE_KEY, JSON.stringify(user))
}

export function clearAuthSession() {
  window.localStorage.removeItem(AUTH_TOKEN_STORAGE_KEY)
  window.localStorage.removeItem(AUTH_USER_STORAGE_KEY)
}

export function getStoredToken() {
  return window.localStorage.getItem(AUTH_TOKEN_STORAGE_KEY)
}

export function getStoredUser(): User | null {
  const rawUser = window.localStorage.getItem(AUTH_USER_STORAGE_KEY)

  if (!rawUser) {
    return null
  }

  try {
    return JSON.parse(rawUser) as User
  } catch {
    clearAuthSession()
    return null
  }
}
