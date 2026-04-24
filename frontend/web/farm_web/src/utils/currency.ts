import type { CurrencyCode } from '../context/CurrencyContext'
import type { Language } from '../context/LanguageContext'

const DISPLAY_RATES: Record<CurrencyCode, number> = {
  BRL: 1,
  USD: 0.2,
}

function normalizeMonetaryValue(value: number): number {
  return Math.round((value + Number.EPSILON) * 100) / 100
}

function resolveLocale(language: Language): string {
  return language === 'pt-BR' ? 'pt-BR' : 'en-US'
}

export function convertMonetaryValue(value: number | null | undefined, currency: CurrencyCode): number | null {
  if (value == null) {
    return null
  }

  return normalizeMonetaryValue(value * DISPLAY_RATES[currency])
}

export function formatCurrencyValue(value: number, language: Language, currency: CurrencyCode): string {
  const hasDecimals = !Number.isInteger(value)

  return value.toLocaleString(resolveLocale(language), {
    style: 'currency',
    currency,
    minimumFractionDigits: hasDecimals ? 2 : 0,
    maximumFractionDigits: 2,
  })
}

export function formatDisplayMoney(
  value: number | null | undefined,
  language: Language,
  currency: CurrencyCode,
): string | null {
  const convertedValue = convertMonetaryValue(value, currency)

  if (convertedValue == null) {
    return null
  }

  return formatCurrencyValue(convertedValue, language, currency)
}

export function appendCurrencyCode(label: string, currency: CurrencyCode): string {
  return `${label} (${currency})`
}
