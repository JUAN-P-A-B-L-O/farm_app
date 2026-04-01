import {
  CartesianGrid,
  Line,
  LineChart as RechartsLineChart,
  ResponsiveContainer,
  Tooltip,
  XAxis,
  YAxis,
} from 'recharts'

export interface LineChartPoint {
  date: string
  quantity: number
}

interface LineChartProps {
  data: LineChartPoint[]
  color?: string
}

function LineChart({ data, color = '#2e6a46' }: LineChartProps) {
  return (
    <div className="analytics-chart__surface">
      <ResponsiveContainer width="100%" height="100%">
        <RechartsLineChart data={data} margin={{ top: 8, right: 16, bottom: 8, left: 0 }}>
          <CartesianGrid stroke="#e4eaef" strokeDasharray="3 3" />
          <XAxis dataKey="date" stroke="#627285" tickLine={false} axisLine={false} />
          <YAxis stroke="#627285" tickLine={false} axisLine={false} allowDecimals={false} />
          <Tooltip />
          <Line
            type="monotone"
            dataKey="quantity"
            stroke={color}
            strokeWidth={3}
            dot={{ r: 4 }}
            activeDot={{ r: 6 }}
          />
        </RechartsLineChart>
      </ResponsiveContainer>
    </div>
  )
}

export default LineChart
