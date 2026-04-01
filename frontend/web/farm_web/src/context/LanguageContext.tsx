import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import en from '../i18n/en'
import ptBR from '../i18n/pt-BR'

export type Language = 'pt-BR' | 'en'

interface LanguageContextValue {
  language: Language
  setLanguage: (language: Language) => void
}

const STORAGE_KEY = 'farm-web-language'
const DEFAULT_LANGUAGE: Language = 'pt-BR'

export const translations = {
  'pt-BR': ptBR,
  en,
} as const

const LanguageContext = createContext<LanguageContextValue | undefined>(undefined)

function isLanguage(value: string | null): value is Language {
  return value === 'pt-BR' || value === 'en'
}

export function LanguageProvider({ children }: { children: ReactNode }) {
  const [language, setLanguage] = useState<Language>(DEFAULT_LANGUAGE)

  useEffect(() => {
    const storedLanguage = window.localStorage.getItem(STORAGE_KEY)

    if (isLanguage(storedLanguage)) {
      setLanguage(storedLanguage)
    }
  }, [])

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, language)
  }, [language])

  const value = useMemo(
    () => ({
      language,
      setLanguage,
    }),
    [language],
  )

  return <LanguageContext.Provider value={value}>{children}</LanguageContext.Provider>
}

export function useLanguage() {
  const context = useContext(LanguageContext)

  if (!context) {
    throw new Error('useLanguage must be used within a LanguageProvider')
  }

  return context
}
