import { useLanguage, translations } from '../context/LanguageContext'

function getNestedValue(source: unknown, key: string): string | undefined {
  return key.split('.').reduce<unknown>((currentValue, part) => {
    if (currentValue && typeof currentValue === 'object' && part in currentValue) {
      return (currentValue as Record<string, unknown>)[part]
    }

    return undefined
  }, source) as string | undefined
}

export function useTranslation() {
  const { language } = useLanguage()

  function t(key: string): string {
    const value = getNestedValue(translations[language], key)
    return typeof value === 'string' ? value : key
  }

  return {
    language,
    t,
  }
}
