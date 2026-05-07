import type { User, UserPlan, UserRole } from '../types/user'

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
    const parsedUser = JSON.parse(rawUser) as Partial<User> & {
      id?: string
      name?: string
      email?: string
      role?: UserRole
      active?: boolean
      farmIds?: string[]
      plan?: UserPlan
    }

    if (
      typeof parsedUser.id !== 'string' ||
      typeof parsedUser.name !== 'string' ||
      typeof parsedUser.email !== 'string' ||
      typeof parsedUser.role !== 'string' ||
      typeof parsedUser.active !== 'boolean' ||
      !Array.isArray(parsedUser.farmIds)
    ) {
      clearAuthSession()
      return null
    }

    return {
      ...parsedUser,
      avatarUrl: parsedUser.avatarUrl ?? null,
      plan: parsedUser.plan ?? 'FREE',
      farmIds: parsedUser.farmIds,
    } as User
  } catch {
    clearAuthSession()
    return null
  }
}
