import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'

export type CurrencyCode = 'BRL' | 'USD'

interface CurrencyContextValue {
  currency: CurrencyCode
  setCurrency: (currency: CurrencyCode) => void
}

const STORAGE_KEY = 'farm-web-currency'
const DEFAULT_CURRENCY: CurrencyCode = 'BRL'

const CurrencyContext = createContext<CurrencyContextValue | undefined>(undefined)

function isCurrencyCode(value: string | null): value is CurrencyCode {
  return value === 'BRL' || value === 'USD'
}

export function CurrencyProvider({ children }: { children: ReactNode }) {
  const [currency, setCurrency] = useState<CurrencyCode>(DEFAULT_CURRENCY)

  useEffect(() => {
    const storedCurrency = window.localStorage.getItem(STORAGE_KEY)

    if (isCurrencyCode(storedCurrency)) {
      setCurrency(storedCurrency)
    }
  }, [])

  useEffect(() => {
    window.localStorage.setItem(STORAGE_KEY, currency)
  }, [currency])

  const value = useMemo(
    () => ({
      currency,
      setCurrency,
    }),
    [currency],
  )

  return <CurrencyContext.Provider value={value}>{children}</CurrencyContext.Provider>
}

export function useCurrency() {
  const context = useContext(CurrencyContext)

  if (!context) {
    throw new Error('useCurrency must be used within a CurrencyProvider')
  }

  return context
}
