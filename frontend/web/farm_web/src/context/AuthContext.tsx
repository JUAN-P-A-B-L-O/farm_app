import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  login as loginRequest,
} from '../services/authService'
import { registerUnauthorizedHandler, resetUnauthorizedHandling } from '../services/api'
import { clearAuthSession, getStoredToken, getStoredUser, persistAuthSession } from '../services/authStorage'
import { AuthContext } from './authContext'

function getInitialAuthState() {
  const storedToken = getStoredToken()
  const storedUser = getStoredUser()

  if (storedToken && storedUser) {
    return {
      token: storedToken,
      user: storedUser,
    }
  }

  clearAuthSession()

  return {
    token: null,
    user: null,
  }
}

export function AuthProvider({ children }: { children: ReactNode }) {
  const [authState, setAuthState] = useState(getInitialAuthState)

  const logout = useCallback(() => {
    clearAuthSession()
    resetUnauthorizedHandling()
    setAuthState({
      token: null,
      user: null,
    })
  }, [])

  const login = useCallback(async (email: string, password: string) => {
    const response = await loginRequest(email, password)

    persistAuthSession(response.accessToken, response.user)
    resetUnauthorizedHandling()
    setAuthState({
      token: response.accessToken,
      user: response.user,
    })
  }, [])

  useEffect(() => {
    registerUnauthorizedHandler(logout)

    return () => {
      registerUnauthorizedHandler(null)
    }
  }, [logout])

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(authState.token),
      token: authState.token,
      user: authState.user,
      login,
      logout,
    }),
    [authState, login, logout],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
