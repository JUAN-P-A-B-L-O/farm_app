import { useCallback, useEffect, useRef, useState } from 'react'

type FiltersUpdater<T> = T | ((currentFilters: T) => T)

interface UseAutoAppliedFiltersOptions<T> {
  debounceKeys?: Array<keyof T>
  debounceMs?: number
  onAppliedChange?: (filters: T) => void
}

function resolveFiltersUpdater<T>(updater: FiltersUpdater<T>, currentFilters: T): T {
  return typeof updater === 'function'
    ? (updater as (currentFilters: T) => T)(currentFilters)
    : updater
}

function cloneFilters<T extends object>(filters: T): T {
  const nextFilters = { ...filters } as T

  for (const key of Object.keys(nextFilters) as Array<keyof T>) {
    const value = nextFilters[key]

    if (Array.isArray(value)) {
      ;(nextFilters as Record<keyof T, unknown>)[key] = [...value]
    }
  }

  return nextFilters
}

function areFilterValuesEqual(left: unknown, right: unknown): boolean {
  if (Array.isArray(left) && Array.isArray(right)) {
    return left.length === right.length && left.every((value, index) => value === right[index])
  }

  return left === right
}

function areFiltersEqual<T extends object>(left: T, right: T): boolean {
  const keys = new Set([...Object.keys(left), ...Object.keys(right)])

  for (const key of keys) {
    if (!areFilterValuesEqual(
      (left as Record<string, unknown>)[key],
      (right as Record<string, unknown>)[key],
    )) {
      return false
    }
  }

  return true
}

export function useAutoAppliedFilters<T extends object>(
  initialFilters: T | (() => T),
  {
    debounceKeys = [],
    debounceMs = 300,
    onAppliedChange,
  }: UseAutoAppliedFiltersOptions<T> = {},
) {
  const createInitialFilters = useCallback(() => cloneFilters(
    typeof initialFilters === 'function' ? (initialFilters as () => T)() : initialFilters,
  ), [initialFilters])
  const initialFiltersValue = createInitialFilters()
  const [filters, setFiltersState] = useState<T>(initialFiltersValue)
  const [appliedFilters, setAppliedFiltersState] = useState<T>(initialFiltersValue)
  const filtersRef = useRef(initialFiltersValue)
  const appliedFiltersRef = useRef(initialFiltersValue)
  const debounceTimerRef = useRef<ReturnType<typeof setTimeout> | null>(null)
  const onAppliedChangeRef = useRef(onAppliedChange)

  useEffect(() => {
    onAppliedChangeRef.current = onAppliedChange
  }, [onAppliedChange])

  useEffect(() => {
    onAppliedChangeRef.current?.(appliedFiltersRef.current)

    return () => {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current)
      }
    }
  }, [])

  const applyCommittedFilters = useCallback((nextFilters: T) => {
    appliedFiltersRef.current = nextFilters
    setAppliedFiltersState(nextFilters)
    onAppliedChangeRef.current?.(nextFilters)
  }, [])

  const setFilters = useCallback((updater: FiltersUpdater<T>) => {
    const currentFilters = filtersRef.current
    const nextFilters = cloneFilters(resolveFiltersUpdater(updater, currentFilters))

    if (areFiltersEqual(currentFilters, nextFilters)) {
      return
    }

    const changedKeys = (Object.keys(nextFilters) as Array<keyof T>).filter((key) => !areFilterValuesEqual(
      currentFilters[key],
      nextFilters[key],
    ))
    const hasImmediateChange = changedKeys.some((key) => !debounceKeys.includes(key))

    filtersRef.current = nextFilters
    setFiltersState(nextFilters)

    if (hasImmediateChange || debounceKeys.length === 0) {
      if (debounceTimerRef.current) {
        clearTimeout(debounceTimerRef.current)
        debounceTimerRef.current = null
      }

      applyCommittedFilters(nextFilters)
      return
    }

    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
    }

    debounceTimerRef.current = setTimeout(() => {
      debounceTimerRef.current = null
      applyCommittedFilters(cloneFilters(filtersRef.current))
    }, debounceMs)
  }, [applyCommittedFilters, debounceKeys, debounceMs])

  const applyFiltersImmediately = useCallback((updater?: FiltersUpdater<T>) => {
    if (debounceTimerRef.current) {
      clearTimeout(debounceTimerRef.current)
      debounceTimerRef.current = null
    }

    const nextFilters = cloneFilters(
      updater ? resolveFiltersUpdater(updater, filtersRef.current) : filtersRef.current,
    )

    filtersRef.current = nextFilters
    setFiltersState(nextFilters)
    applyCommittedFilters(nextFilters)
  }, [applyCommittedFilters])

  const resetFilters = useCallback(() => {
    applyFiltersImmediately(createInitialFilters())
  }, [applyFiltersImmediately, createInitialFilters])

  return {
    filters,
    appliedFilters,
    setFilters,
    applyFiltersImmediately,
    resetFilters,
  }
}
