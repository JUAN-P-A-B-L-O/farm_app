const TWO_DECIMAL_PATTERN = /^\d*(?:\.\d{0,2})?$/

export function isValidTwoDecimalInput(value: string) {
  return value === '' || TWO_DECIMAL_PATTERN.test(value)
}

export function parseTwoDecimalInput(value: string, currentValue: number) {
  if (!isValidTwoDecimalInput(value)) {
    return currentValue
  }

  if (value === '') {
    return 0
  }

  return Number(value)
}

export function hasAtMostTwoDecimals(value: number) {
  if (!Number.isFinite(value)) {
    return false
  }

  const [, decimalPart] = value.toString().split('.')
  return !decimalPart || decimalPart.length <= 2
}

export function normalizeToTwoDecimals(value: number) {
  return Number(value.toFixed(2))
}
