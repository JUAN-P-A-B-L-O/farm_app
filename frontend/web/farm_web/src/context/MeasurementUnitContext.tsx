import {
  createContext,
  useContext,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import type { FeedingUnit, ProductionUnit } from '../utils/measurementUnits'

interface MeasurementUnitContextValue {
  productionUnit: ProductionUnit
  feedingUnit: FeedingUnit
  setProductionUnit: (unit: ProductionUnit) => void
  setFeedingUnit: (unit: FeedingUnit) => void
}

const STORAGE_KEY = 'farm-web-measurement-units'
const DEFAULT_PRODUCTION_UNIT: ProductionUnit = 'LITER'
const DEFAULT_FEEDING_UNIT: FeedingUnit = 'KILOGRAM'

const MeasurementUnitContext = createContext<MeasurementUnitContextValue | undefined>(undefined)

function isProductionUnit(value: string | null): value is ProductionUnit {
  return value === 'LITER' || value === 'MILLILITER'
}

function isFeedingUnit(value: string | null): value is FeedingUnit {
  return value === 'KILOGRAM' || value === 'GRAM'
}

export function MeasurementUnitProvider({ children }: { children: ReactNode }) {
  const [productionUnit, setProductionUnit] = useState<ProductionUnit>(DEFAULT_PRODUCTION_UNIT)
  const [feedingUnit, setFeedingUnit] = useState<FeedingUnit>(DEFAULT_FEEDING_UNIT)

  useEffect(() => {
    const storedValue = window.localStorage.getItem(STORAGE_KEY)

    if (!storedValue) {
      return
    }

    try {
      const parsedValue = JSON.parse(storedValue) as {
        productionUnit?: string
        feedingUnit?: string
      }

      const nextProductionUnit = parsedValue.productionUnit ?? null
      if (isProductionUnit(nextProductionUnit)) {
        setProductionUnit(nextProductionUnit)
      }

      const nextFeedingUnit = parsedValue.feedingUnit ?? null
      if (isFeedingUnit(nextFeedingUnit)) {
        setFeedingUnit(nextFeedingUnit)
      }
    } catch {
      window.localStorage.removeItem(STORAGE_KEY)
    }
  }, [])

  useEffect(() => {
    window.localStorage.setItem(
      STORAGE_KEY,
      JSON.stringify({
        productionUnit,
        feedingUnit,
      }),
    )
  }, [feedingUnit, productionUnit])

  const value = useMemo(
    () => ({
      productionUnit,
      feedingUnit,
      setProductionUnit,
      setFeedingUnit,
    }),
    [feedingUnit, productionUnit],
  )

  return (
    <MeasurementUnitContext.Provider value={value}>
      {children}
    </MeasurementUnitContext.Provider>
  )
}

export function useMeasurementUnits() {
  const context = useContext(MeasurementUnitContext)

  if (!context) {
    throw new Error('useMeasurementUnits must be used within a MeasurementUnitProvider')
  }

  return context
}
