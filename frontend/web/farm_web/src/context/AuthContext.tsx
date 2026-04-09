import {
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import {
  login as loginRequest,
} from '../services/authService'
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

  async function login(email: string, password: string) {
    const response = await loginRequest(email, password)

    persistAuthSession(response.accessToken, response.user)
    setAuthState({
      token: response.accessToken,
      user: response.user,
    })
  }

  function logout() {
    clearAuthSession()
    setAuthState({
      token: null,
      user: null,
    })
  }

  const value = useMemo(
    () => ({
      isAuthenticated: Boolean(authState.token),
      token: authState.token,
      user: authState.user,
      login,
      logout,
    }),
    [authState],
  )

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}
