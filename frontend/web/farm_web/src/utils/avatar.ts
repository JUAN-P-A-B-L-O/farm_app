export const MAX_AVATAR_FILE_SIZE_BYTES = 1024 * 1024

export const ALLOWED_AVATAR_FILE_TYPES = [
  'image/png',
  'image/jpeg',
  'image/jpg',
  'image/gif',
  'image/webp',
] as const

const SUPPORTED_AVATAR_DATA_URL =
  /^data:image\/(?:png|jpeg|jpg|gif|webp);base64,[a-z0-9+/=\r\n]+$/i

export function isAllowedAvatarFileType(fileType: string) {
  return ALLOWED_AVATAR_FILE_TYPES.includes(
    fileType.toLowerCase() as (typeof ALLOWED_AVATAR_FILE_TYPES)[number],
  )
}

export function isValidAvatarUrl(value: string) {
  const normalizedValue = value.trim()

  if (!normalizedValue) {
    return false
  }

  if (SUPPORTED_AVATAR_DATA_URL.test(normalizedValue)) {
    return true
  }

  try {
    const url = new URL(normalizedValue)
    return url.protocol === 'http:' || url.protocol === 'https:'
  } catch {
    return false
  }
}
