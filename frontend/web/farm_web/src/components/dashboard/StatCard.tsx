interface StatCardProps {
  title: string
  value: number
}

function StatCard({ title, value }: StatCardProps) {
  return (
    <article className="stat-card">
      <span className="stat-card__title">{title}</span>
      <strong className="stat-card__value">{value.toLocaleString()}</strong>
    </article>
  )
}

export default StatCard
