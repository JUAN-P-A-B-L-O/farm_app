export type SupportedLanguage = 'pt-BR' | 'en'

function getLocale(language: SupportedLanguage) {
  return language === 'pt-BR' ? 'pt-BR' : 'en-US'
}

export function formatDate(value: string, language: SupportedLanguage) {
  if (!value) {
    return '-'
  }

  const date = new Date(`${value}T00:00:00`)

  if (Number.isNaN(date.getTime())) {
    return value
  }

  return new Intl.DateTimeFormat(getLocale(language), {
    day: language === 'pt-BR' ? '2-digit' : undefined,
    month: language === 'pt-BR' ? '2-digit' : undefined,
    year: 'numeric',
  }).format(date)
}

export function formatNumber(value: number, language: SupportedLanguage, options?: Intl.NumberFormatOptions) {
  return value.toLocaleString(getLocale(language), {
    maximumFractionDigits: 2,
    ...options,
  })
}

export function formatCurrency(value: number, language: SupportedLanguage) {
  return value.toLocaleString(getLocale(language), {
    style: 'currency',
    currency: 'USD',
    minimumFractionDigits: Number.isInteger(value) ? 0 : 2,
    maximumFractionDigits: 2,
  })
}
