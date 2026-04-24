import {
  Bar,
  BarChart as RechartsBarChart,
  CartesianGrid,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

export interface BarChartPoint {
  label: string
  value: number
}

interface BarChartProps {
  data: BarChartPoint[]
  color?: string
  valueFormatter?: (value: number) => string
}

function formatTooltipValue(value: unknown, valueFormatter?: (value: number) => string): string | number {
  if (typeof value !== 'number') {
    return String(value ?? '')
  }

  return valueFormatter ? valueFormatter(value) : value
}

function BarChart({ data, color = '#2e6a46', valueFormatter }: BarChartProps) {
  if (!Array.isArray(data) || data.length === 0) {
    return null
  }

  return (
    <div className="analytics-chart__surface" style={{ width: '100%', minHeight: 320, height: 320 }}>
      <ResponsiveContainer width="100%" height={320}>
        <RechartsBarChart data={data} margin={{ top: 8, right: 16, bottom: 8, left: 0 }}>
          <CartesianGrid stroke="#e4eaef" strokeDasharray="3 3" />
          <XAxis dataKey="label" stroke="#627285" tickLine={false} axisLine={false} />
          <YAxis stroke="#627285" tickLine={false} axisLine={false} tickFormatter={valueFormatter} />
          <Tooltip formatter={(value) => formatTooltipValue(value, valueFormatter)} />
          <Bar dataKey="value" fill={color} radius={[8, 8, 0, 0]} />
        </RechartsBarChart>
      </ResponsiveContainer>
    </div>
  )
}

export default BarChart
