import type { User } from '../types/user'

export const MANAGER_ROLE = 'MANAGER'

export function isManager(user: Pick<User, 'role'> | null | undefined) {
  return user?.role?.trim().toUpperCase() === MANAGER_ROLE
}
