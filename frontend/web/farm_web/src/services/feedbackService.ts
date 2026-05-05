export type FeedbackSeverity = 'success'

export interface FeedbackAction {
  labelKey: string
  onAction: () => void
}

export interface FeedbackMessageOptions {
  action?: FeedbackAction
  dedupeKey?: string
  durationMs?: number
  variables?: Record<string, number | string>
}

export interface FeedbackMessage extends FeedbackMessageOptions {
  messageKey: string
  severity: FeedbackSeverity
}

type FeedbackListener = (message: FeedbackMessage) => void

const listeners = new Set<FeedbackListener>()

export function subscribeToFeedback(listener: FeedbackListener) {
  listeners.add(listener)

  return () => {
    listeners.delete(listener)
  }
}

export function publishFeedback(message: FeedbackMessage) {
  listeners.forEach((listener) => {
    listener(message)
  })
}

export function publishSuccess(messageKey: string, options?: FeedbackMessageOptions) {
  publishFeedback({
    severity: 'success',
    messageKey,
    ...options,
  })
}
