import { hasAtMostTwoDecimals } from './decimal'

export type ProductionUnit = 'LITER' | 'MILLILITER'
export type FeedingUnit = 'KILOGRAM' | 'GRAM'
export type MeasurementUnit = ProductionUnit | FeedingUnit

const UNIT_FACTORS: Record<MeasurementUnit, number> = {
  LITER: 1,
  MILLILITER: 0.001,
  KILOGRAM: 1,
  GRAM: 0.001,
}

const UNIT_INPUT_STEPS: Record<MeasurementUnit, string> = {
  LITER: '0.01',
  MILLILITER: '10',
  KILOGRAM: '0.01',
  GRAM: '10',
}

const FEED_COST_INPUT_STEPS: Record<FeedingUnit, string> = {
  KILOGRAM: '0.01',
  GRAM: '0.00001',
}

function roundToDisplayPrecision(value: number) {
  return Number(value.toFixed(6))
}

export function convertMeasurementToBase(value: number, unit: MeasurementUnit) {
  if (!Number.isFinite(value)) {
    return 0
  }

  return roundToDisplayPrecision(value * UNIT_FACTORS[unit])
}

export function convertMeasurementFromBase(value: number, unit: MeasurementUnit) {
  if (!Number.isFinite(value)) {
    return 0
  }

  return roundToDisplayPrecision(value / UNIT_FACTORS[unit])
}

export function formatMeasurementValue(
  value: number,
  unit: MeasurementUnit,
  language: 'pt-BR' | 'en',
) {
  const convertedValue = convertMeasurementFromBase(value, unit)
  return formatConvertedMeasurementValue(convertedValue, language)
}

export function formatConvertedMeasurementValue(
  value: number,
  language: 'pt-BR' | 'en',
) {
  const locale = language === 'pt-BR' ? 'pt-BR' : 'en-US'
  const hasDecimals = !Number.isInteger(value)

  return value.toLocaleString(locale, {
    minimumFractionDigits: hasDecimals ? 2 : 0,
    maximumFractionDigits: 2,
  })
}

export function isMeasurementCompatibleWithBasePrecision(value: number, unit: MeasurementUnit) {
  return hasAtMostTwoDecimals(convertMeasurementToBase(value, unit))
}

export function getMeasurementInputStep(unit: MeasurementUnit) {
  return UNIT_INPUT_STEPS[unit]
}

export function convertFeedCostToBase(value: number, unit: FeedingUnit) {
  if (!Number.isFinite(value)) {
    return 0
  }

  return roundToDisplayPrecision(value / UNIT_FACTORS[unit])
}

export function convertFeedCostFromBase(value: number, unit: FeedingUnit) {
  if (!Number.isFinite(value)) {
    return 0
  }

  return roundToDisplayPrecision(value * UNIT_FACTORS[unit])
}

export function isFeedCostCompatibleWithBasePrecision(value: number, unit: FeedingUnit) {
  return hasAtMostTwoDecimals(convertFeedCostToBase(value, unit))
}

export function getFeedCostInputStep(unit: FeedingUnit) {
  return FEED_COST_INPUT_STEPS[unit]
}

export function getMeasurementUnitShortLabelKey(unit: MeasurementUnit) {
  return `measurementUnits.short.${unit}`
}

export function appendUnitToLabel(label: string, unitLabel: string) {
  return `${label} (${unitLabel})`
}
