import { forwardRef } from 'react'
import { useTranslation } from '../../hooks/useTranslation'
import type { FeedbackAction, FeedbackSeverity } from '../../services/feedbackService'
import './FeedbackToast.css'

interface FeedbackToastProps {
  action?: FeedbackAction
  messageKey: string
  onClose: () => void
  onAction?: () => void
  severity: FeedbackSeverity
  visible: boolean
  variables?: Record<string, number | string>
}

const FeedbackToast = forwardRef<HTMLDivElement, FeedbackToastProps>(function FeedbackToast(
  {
    action,
    messageKey,
    onAction,
    onClose,
    severity,
    visible,
    variables,
  },
  ref,
) {
  const { t } = useTranslation()

  return (
    <div
      ref={ref}
      className={`feedback-toast feedback-toast--${severity} ${visible ? 'feedback-toast--visible' : ''}`}
      role="status"
      aria-live="polite"
      aria-atomic="true"
    >
      <div className="feedback-toast__content">
        <p className="feedback-toast__message">{t(messageKey, variables)}</p>
        <div className="feedback-toast__actions">
          {action && onAction && (
            <button type="button" className="feedback-toast__action" onClick={onAction}>
              {t(action.labelKey)}
            </button>
          )}
          <button
            type="button"
            className="feedback-toast__close"
            onClick={onClose}
            aria-label={t('common.feedback.close')}
          >
            {t('common.feedback.dismiss')}
          </button>
        </div>
      </div>
    </div>
  )
})

export default FeedbackToast
