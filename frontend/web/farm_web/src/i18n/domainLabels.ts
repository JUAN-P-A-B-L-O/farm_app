import type { AnimalOrigin, AnimalStatus } from '../types/animal'
import type { UserRole } from '../types/user'

type Translate = (key: string) => string

const USER_ROLES: UserRole[] = ['MANAGER', 'WORKER']
const ANIMAL_STATUSES: AnimalStatus[] = ['ACTIVE', 'SOLD', 'DEAD', 'INACTIVE']
const ANIMAL_ORIGINS: AnimalOrigin[] = ['BORN', 'PURCHASED']

function hasValue<Option extends string>(options: readonly Option[], value: string): value is Option {
  return options.includes(value as Option)
}

export { ANIMAL_ORIGINS, ANIMAL_STATUSES, USER_ROLES }

export function getUserRoleLabel(t: Translate, role: string) {
  return hasValue(USER_ROLES, role) ? t(`accessControl.roles.${role}`) : role
}

export function getUserActiveLabel(t: Translate, active: boolean) {
  return active ? t('accessControl.status.active') : t('accessControl.status.inactive')
}

export function getAnimalStatusLabel(t: Translate, status: string) {
  return hasValue(ANIMAL_STATUSES, status) ? t(`animals.statuses.${status}`) : status
}

export function getAnimalOriginLabel(t: Translate, origin: string) {
  return hasValue(ANIMAL_ORIGINS, origin) ? t(`animals.origins.${origin}`) : origin
}
