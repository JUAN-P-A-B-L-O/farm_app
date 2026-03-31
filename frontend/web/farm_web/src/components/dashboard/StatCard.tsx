import type { ReactNode } from 'react'
import './StatCard.css'

interface StatCardProps {
  title: string
  value: number
  icon?: ReactNode
}

function isCurrencyMetric(title: string) {
  const normalizedTitle = title.toLowerCase()

  return (
    normalizedTitle.includes('cost') ||
    normalizedTitle.includes('revenue') ||
    normalizedTitle.includes('profit')
  )
}

function formatValue(title: string, value: number) {
  const hasDecimals = !Number.isInteger(value)

  if (isCurrencyMetric(title)) {
    return value.toLocaleString(undefined, {
      style: 'currency',
      currency: 'USD',
      minimumFractionDigits: hasDecimals ? 2 : 0,
      maximumFractionDigits: 2,
    })
  }

  return value.toLocaleString(undefined, {
    minimumFractionDigits: hasDecimals ? 2 : 0,
    maximumFractionDigits: 2,
  })
}

function StatCard({ title, value, icon }: StatCardProps) {
  return (
    <article className="stat-card">
      <div className="stat-card__header">
        <span className="stat-card__title">{title}</span>
        {icon ? <span className="stat-card__icon" aria-hidden="true">{icon}</span> : null}
      </div>
      <strong className="stat-card__value">{formatValue(title, value)}</strong>
    </article>
  )
}

export default StatCard
