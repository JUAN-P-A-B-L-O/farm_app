import type { CurrencyCode } from '../context/CurrencyContext'
import type { Language } from '../context/LanguageContext'

const DISPLAY_RATES: Record<CurrencyCode, number> = {
  BRL: 1,
  USD: 0.2,
}

interface CurrencyFormatOptions {
  minimumFractionDigits?: number
  maximumFractionDigits?: number
  conversionPrecision?: number
}

function normalizeMonetaryValue(value: number, precision = 2): number {
  const factor = 10 ** precision
  return Math.round((value + Number.EPSILON) * factor) / factor
}

function resolveLocale(language: Language): string {
  return language === 'pt-BR' ? 'pt-BR' : 'en-US'
}

export function convertMonetaryValue(
  value: number | null | undefined,
  currency: CurrencyCode,
  precision = 2,
): number | null {
  if (value == null) {
    return null
  }

  return normalizeMonetaryValue(value * DISPLAY_RATES[currency], precision)
}

export function formatCurrencyValue(
  value: number,
  language: Language,
  currency: CurrencyCode,
  options: CurrencyFormatOptions = {},
): string {
  const hasDecimals = !Number.isInteger(value)
  const minimumFractionDigits = options.minimumFractionDigits ?? (hasDecimals ? 2 : 0)
  const maximumFractionDigits = options.maximumFractionDigits ?? 2

  return value.toLocaleString(resolveLocale(language), {
    style: 'currency',
    currency,
    minimumFractionDigits,
    maximumFractionDigits,
  })
}

export function formatDisplayMoney(
  value: number | null | undefined,
  language: Language,
  currency: CurrencyCode,
  options: CurrencyFormatOptions = {},
): string | null {
  const convertedValue = convertMonetaryValue(
    value,
    currency,
    options.conversionPrecision ?? options.maximumFractionDigits ?? 2,
  )

  if (convertedValue == null) {
    return null
  }

  return formatCurrencyValue(convertedValue, language, currency, options)
}

export function appendCurrencyCode(label: string, currency: CurrencyCode): string {
  return `${label} (${currency})`
}
