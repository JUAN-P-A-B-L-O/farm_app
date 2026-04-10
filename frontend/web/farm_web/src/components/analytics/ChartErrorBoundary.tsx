import { Component, type ReactNode } from 'react'

interface ChartErrorBoundaryProps {
  children: ReactNode
  fallback: ReactNode
}

interface ChartErrorBoundaryState {
  hasError: boolean
}

class ChartErrorBoundary extends Component<ChartErrorBoundaryProps, ChartErrorBoundaryState> {
  state: ChartErrorBoundaryState = {
    hasError: false,
  }

  static getDerivedStateFromError(): ChartErrorBoundaryState {
    return { hasError: true }
  }

  componentDidUpdate(prevProps: ChartErrorBoundaryProps) {
    if (this.state.hasError && prevProps.children !== this.props.children) {
      this.setState({ hasError: false })
    }
  }

  render() {
    if (this.state.hasError) {
      return this.props.fallback
    }

    return this.props.children
  }
}

export default ChartErrorBoundary
