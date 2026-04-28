import { useCallback, useEffect, useRef, useState } from './reactStub.js';
function resolveFiltersUpdater(updater, currentFilters) {
    return typeof updater === 'function'
        ? updater(currentFilters)
        : updater;
}
function cloneFilters(filters) {
    const nextFilters = { ...filters };
    for (const key of Object.keys(nextFilters)) {
        const value = nextFilters[key];
        if (Array.isArray(value)) {
            ;
            nextFilters[key] = [...value];
        }
    }
    return nextFilters;
}
function areFilterValuesEqual(left, right) {
    if (Array.isArray(left) && Array.isArray(right)) {
        return left.length === right.length && left.every((value, index) => value === right[index]);
    }
    return left === right;
}
function areFiltersEqual(left, right) {
    const keys = new Set([...Object.keys(left), ...Object.keys(right)]);
    for (const key of keys) {
        if (!areFilterValuesEqual(left[key], right[key])) {
            return false;
        }
    }
    return true;
}
export function useAutoAppliedFilters(initialFilters, { debounceKeys = [], debounceMs = 300, onAppliedChange, } = {}) {
    const createInitialFilters = useCallback(() => cloneFilters(typeof initialFilters === 'function' ? initialFilters() : initialFilters), [initialFilters]);
    const initialFiltersValue = createInitialFilters();
    const [filters, setFiltersState] = useState(initialFiltersValue);
    const [appliedFilters, setAppliedFiltersState] = useState(initialFiltersValue);
    const filtersRef = useRef(initialFiltersValue);
    const appliedFiltersRef = useRef(initialFiltersValue);
    const debounceTimerRef = useRef(null);
    const onAppliedChangeRef = useRef(onAppliedChange);
    useEffect(() => {
        onAppliedChangeRef.current = onAppliedChange;
    }, [onAppliedChange]);
    useEffect(() => {
        onAppliedChangeRef.current?.(appliedFiltersRef.current);
        return () => {
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current);
            }
        };
    }, []);
    const applyCommittedFilters = useCallback((nextFilters) => {
        appliedFiltersRef.current = nextFilters;
        setAppliedFiltersState(nextFilters);
        onAppliedChangeRef.current?.(nextFilters);
    }, []);
    const setFilters = useCallback((updater) => {
        const currentFilters = filtersRef.current;
        const nextFilters = cloneFilters(resolveFiltersUpdater(updater, currentFilters));
        if (areFiltersEqual(currentFilters, nextFilters)) {
            return;
        }
        const changedKeys = Object.keys(nextFilters).filter((key) => !areFilterValuesEqual(currentFilters[key], nextFilters[key]));
        const hasImmediateChange = changedKeys.some((key) => !debounceKeys.includes(key));
        filtersRef.current = nextFilters;
        setFiltersState(nextFilters);
        if (hasImmediateChange || debounceKeys.length === 0) {
            if (debounceTimerRef.current) {
                clearTimeout(debounceTimerRef.current);
                debounceTimerRef.current = null;
            }
            applyCommittedFilters(nextFilters);
            return;
        }
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
        }
        debounceTimerRef.current = setTimeout(() => {
            debounceTimerRef.current = null;
            applyCommittedFilters(cloneFilters(filtersRef.current));
        }, debounceMs);
    }, [applyCommittedFilters, debounceKeys, debounceMs]);
    const applyFiltersImmediately = useCallback((updater) => {
        if (debounceTimerRef.current) {
            clearTimeout(debounceTimerRef.current);
            debounceTimerRef.current = null;
        }
        const nextFilters = cloneFilters(updater ? resolveFiltersUpdater(updater, filtersRef.current) : filtersRef.current);
        filtersRef.current = nextFilters;
        setFiltersState(nextFilters);
        applyCommittedFilters(nextFilters);
    }, [applyCommittedFilters]);
    const resetFilters = useCallback(() => {
        applyFiltersImmediately(createInitialFilters());
    }, [applyFiltersImmediately, createInitialFilters]);
    return {
        filters,
        appliedFilters,
        setFilters,
        applyFiltersImmediately,
        resetFilters,
    };
}
