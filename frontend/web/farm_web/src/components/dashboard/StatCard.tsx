interface StatCardProps {
  title: string
  value: number
}

function StatCard({ title, value }: StatCardProps) {
  // console.log({
  //   title,
  //   value
  // })
  return (
    // <article className="stat-card">
    //   <span className="stat-card__title">{title}</span>
    //   <strong className="stat-card__value">{value.toLocaleString()}</strong>
    <div>
      <div>{title}</div>
      <div>{value}</div>
    </div>

    // </article>
  )
}

export default StatCard
