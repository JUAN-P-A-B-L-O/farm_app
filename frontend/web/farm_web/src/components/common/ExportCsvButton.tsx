interface ExportCsvButtonProps {
  onClick: () => void
  label: string
  loadingLabel: string
  isLoading?: boolean
  disabled?: boolean
  className?: string
}

function ExportCsvButton({
  onClick,
  label,
  loadingLabel,
  isLoading = false,
  disabled = false,
  className = 'animals-table__action-button animals-table__action-button--secondary',
}: ExportCsvButtonProps) {
  return (
    <button type="button" className={className} onClick={onClick} disabled={disabled || isLoading}>
      {isLoading ? loadingLabel : label}
    </button>
  )
}

export default ExportCsvButton
