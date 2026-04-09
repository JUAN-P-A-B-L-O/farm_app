import { createContext } from 'react'
import type { User } from '../types/user'

export interface AuthContextValue {
  isAuthenticated: boolean
  token: string | null
  user: User | null
  login: (email: string, password: string) => Promise<void>
  logout: () => void
}

export const AuthContext = createContext<AuthContextValue | undefined>(undefined)
