import type { ReactNode } from 'react'
import { useCurrency } from '../../hooks/useCurrency'
import { useTranslation } from '../../hooks/useTranslation'
import { formatCurrencyValue } from '../../utils/currency'
import './StatCard.css'

interface StatCardProps {
  title: string
  value: number
  format?: 'number' | 'currency'
  icon?: ReactNode
}

function formatValue(
  value: number,
  format: 'number' | 'currency',
  language: 'pt-BR' | 'en',
  currency: 'BRL' | 'USD',
) {
  const hasDecimals = !Number.isInteger(value)
  const locale = language === 'pt-BR' ? 'pt-BR' : 'en-US'

  if (format === 'currency') {
    return formatCurrencyValue(value, language, currency)
  }

  return value.toLocaleString(locale, {
    minimumFractionDigits: hasDecimals ? 2 : 0,
    maximumFractionDigits: 2,
  })
}

function StatCard({ title, value, format = 'number', icon }: StatCardProps) {
  const { language } = useTranslation()
  const { currency } = useCurrency()

  return (
    <article className="stat-card">
      <div className="stat-card__header">
        <span className="stat-card__title">{title}</span>
        {icon ? <span className="stat-card__icon" aria-hidden="true">{icon}</span> : null}
      </div>
      <strong className="stat-card__value">{formatValue(value, format, language, currency)}</strong>
    </article>
  )
}

export default StatCard
