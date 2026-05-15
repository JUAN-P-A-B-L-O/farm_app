import { useAuth } from '../../hooks/useAuth'
import { usePlanUpgrade } from '../../hooks/usePlanUpgrade'
import { useTranslation } from '../../hooks/useTranslation'
import { getFeatureAccessState } from '../../utils/planAccess'

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
  const { user } = useAuth()
  const { openUpgradePrompt } = usePlanUpgrade()
  const { t } = useTranslation()
  const accessState = getFeatureAccessState(user, 'CSV_EXPORT')
  const isPlanRestricted = !accessState.allowed
  const visibleLabel = isLoading
    ? loadingLabel
    : isPlanRestricted
      ? `${label} (${t('plan.badge')})`
      : label

  return (
    <button
      type="button"
      className={className}
      onClick={() => {
        if (isPlanRestricted) {
          openUpgradePrompt('CSV_EXPORT')
          return
        }

        onClick()
      }}
      disabled={disabled || isLoading}
      title={isPlanRestricted ? t(accessState.metadata.descriptionKey) : undefined}
    >
      {visibleLabel}
    </button>
  )
}

export default ExportCsvButton
