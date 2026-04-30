
const USER_ROLES = ['MANAGER', 'WORKER']
const ANIMAL_STATUSES = ['ACTIVE', 'SOLD', 'DEAD', 'INACTIVE']
const ANIMAL_ORIGINS = ['BORN', 'PURCHASED']

function hasValue(options, value) {
  return options.includes(value)
}

export { ANIMAL_ORIGINS, ANIMAL_STATUSES, USER_ROLES }

export function getUserRoleLabel(t, role) {
  return hasValue(USER_ROLES, role) ? t(`accessControl.roles.${role}`) : role
}

export function getUserActiveLabel(t, active) {
  return active ? t('accessControl.status.active') : t('accessControl.status.inactive')
}

export function getAnimalStatusLabel(t, status) {
  return hasValue(ANIMAL_STATUSES, status) ? t(`animals.statuses.${status}`) : status
}

export function getAnimalOriginLabel(t, origin) {
  return hasValue(ANIMAL_ORIGINS, origin) ? t(`animals.origins.${origin}`) : origin
}
