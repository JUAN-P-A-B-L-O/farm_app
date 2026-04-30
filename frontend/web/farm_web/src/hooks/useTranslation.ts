import { useLanguage, translations } from '../context/LanguageContext'

function getNestedValue(source: unknown, key: string): string | undefined {
  return key.split('.').reduce<unknown>((currentValue, part) => {
    if (currentValue && typeof currentValue === 'object' && part in currentValue) {
      return (currentValue as Record<string, unknown>)[part]
    }

    return undefined
  }, source) as string | undefined
}

function interpolate(
  template: string,
  variables: Record<string, string | number> | undefined,
) {
  if (!variables) {
    return template
  }

  return Object.entries(variables).reduce(
    (currentValue, [key, value]) => currentValue.replaceAll(`{${key}}`, String(value)),
    template,
  )
}

export function useTranslation() {
  const { language } = useLanguage()

  function t(key: string, variables?: Record<string, string | number>): string {
    const value = getNestedValue(translations[language], key)
    return typeof value === 'string' ? interpolate(value, variables) : key
  }

  return {
    language,
    t,
  }
}
