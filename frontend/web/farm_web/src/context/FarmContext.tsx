import {
  useCallback,
  useEffect,
  useMemo,
  useState,
  type ReactNode,
} from 'react'
import { useAuth } from '../hooks/useAuth'
import { getAccessibleFarms } from '../services/farmService'
import { clearStoredFarmId, getStoredFarmId, persistFarmId } from '../services/farmStorage'
import type { Farm } from '../types/farm'
import { FarmContext } from './farmContext'

export function FarmProvider({ children }: { children: ReactNode }) {
  const { isAuthenticated } = useAuth()
  const [farms, setFarms] = useState<Farm[]>([])
  const [selectedFarmId, setSelectedFarmIdState] = useState('')
  const [isLoading, setIsLoading] = useState(false)
  const [errorMessage, setErrorMessage] = useState('')

  const setSelectedFarmId = useCallback((farmId: string) => {
    setSelectedFarmIdState(farmId)

    if (farmId) {
      persistFarmId(farmId)
      return
    }

    clearStoredFarmId()
  }, [])

  const refreshFarms = useCallback(async () => {
    if (!isAuthenticated) {
      setFarms([])
      setSelectedFarmIdState('')
      setErrorMessage('')
      setIsLoading(false)
      clearStoredFarmId()
      return
    }

    setIsLoading(true)
    setErrorMessage('')

    try {
      const data = await getAccessibleFarms()
      const storedFarmId = getStoredFarmId()

      setFarms(data)
      setSelectedFarmIdState((currentSelectedFarmId) => {
        const nextSelectedFarmId = data.some((farm) => farm.id === currentSelectedFarmId)
          ? currentSelectedFarmId
          : data.some((farm) => farm.id === storedFarmId)
            ? storedFarmId ?? ''
            : data[0]?.id ?? ''

        if (nextSelectedFarmId) {
          persistFarmId(nextSelectedFarmId)
        } else {
          clearStoredFarmId()
        }

        return nextSelectedFarmId
      })
    } catch {
      setFarms([])
      setSelectedFarmIdState('')
      setErrorMessage('farm.loadError')
      clearStoredFarmId()
    } finally {
      setIsLoading(false)
    }
  }, [isAuthenticated])

  useEffect(() => {
    void refreshFarms()
  }, [refreshFarms])

  const selectedFarm = useMemo(
    () => farms.find((farm) => farm.id === selectedFarmId) ?? null,
    [farms, selectedFarmId],
  )

  const value = useMemo(
    () => ({
      farms,
      selectedFarmId,
      selectedFarm,
      isLoading,
      errorMessage,
      setSelectedFarmId,
      refreshFarms,
    }),
    [errorMessage, farms, isLoading, refreshFarms, selectedFarm, selectedFarmId, setSelectedFarmId],
  )

  return <FarmContext.Provider value={value}>{children}</FarmContext.Provider>
}
