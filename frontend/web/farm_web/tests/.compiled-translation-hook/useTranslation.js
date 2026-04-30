import { useLanguage, translations } from './LanguageContext.js'

function getNestedValue(source, key) {
  return key.split('.').reduce((currentValue, part) => {
    if (currentValue && typeof currentValue === 'object' && part in currentValue) {
      return currentValue[part]
    }

    return undefined
  }, source)
}

function interpolate(
  template,
  variables,
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

  function t(key, variables) {
    const value = getNestedValue(translations[language], key)
    return typeof value === 'string' ? interpolate(value, variables) : key
  }

  return {
    language,
    t,
  }
}
