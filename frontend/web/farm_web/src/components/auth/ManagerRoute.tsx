import { Navigate } from 'react-router-dom'
import type { ReactElement } from 'react'
import { useAuth } from '../../hooks/useAuth'
import { isManager } from '../../utils/authorization'

interface ManagerRouteProps {
  children: ReactElement
}

function ManagerRoute({ children }: ManagerRouteProps) {
  const { user } = useAuth()

  if (!isManager(user)) {
    return <Navigate to="/animals" replace />
  }

  return children
}

export default ManagerRoute
