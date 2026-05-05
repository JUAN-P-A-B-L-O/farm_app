import {
  useCallback,
  useEffect,
  useMemo,
  useRef,
  useState,
  type ReactNode,
} from 'react'
import FeedbackToast from '../components/common/FeedbackToast'
import { FeedbackContext } from './feedbackContext'
import {
  publishSuccess,
  subscribeToFeedback,
  type FeedbackMessage,
  type FeedbackMessageOptions,
} from '../services/feedbackService'

interface ActiveFeedbackMessage extends FeedbackMessage {
  dedupeKey: string
  id: number
}

const DEFAULT_DURATION_MS = 4200

function buildMessageId(messageKey: string, dedupeKey?: string) {
  return dedupeKey ?? messageKey
}

export function FeedbackProvider({ children }: { children: ReactNode }) {
  const [activeMessage, setActiveMessage] = useState<ActiveFeedbackMessage | null>(null)
  const activeMessageRef = useRef<ActiveFeedbackMessage | null>(null)
  const nextIdRef = useRef(1)
  const previousFocusRef = useRef<HTMLElement | null>(null)
  const toastRef = useRef<HTMLDivElement | null>(null)

  useEffect(() => {
    activeMessageRef.current = activeMessage
  }, [activeMessage])

  const dismissFeedback = useCallback(() => {
    const shouldRestoreFocus = toastRef.current?.contains(document.activeElement) ?? false
    const elementToRestore = previousFocusRef.current

    setActiveMessage(null)

    if (shouldRestoreFocus && elementToRestore) {
      window.requestAnimationFrame(() => {
        if (document.contains(elementToRestore)) {
          elementToRestore.focus()
        }
      })
    }
  }, [])

  const showSuccessFeedback = useCallback((messageKey: string, options?: FeedbackMessageOptions) => {
    previousFocusRef.current = document.activeElement instanceof HTMLElement ? document.activeElement : null

    setActiveMessage((currentMessage) => {
      const dedupeKey = buildMessageId(messageKey, options?.dedupeKey)

      return {
        severity: 'success',
        messageKey,
        ...options,
        dedupeKey,
        id: currentMessage?.dedupeKey === dedupeKey ? currentMessage.id : nextIdRef.current++,
      }
    })
  }, [])

  useEffect(() => {
    return subscribeToFeedback((message) => {
      if (message.severity === 'success') {
        showSuccessFeedback(message.messageKey, message)
      }
    })
  }, [showSuccessFeedback])

  useEffect(() => {
    if (!activeMessage) {
      return undefined
    }

    const timeoutId = window.setTimeout(dismissFeedback, activeMessage.durationMs ?? DEFAULT_DURATION_MS)

    return () => {
      window.clearTimeout(timeoutId)
    }
  }, [activeMessage, dismissFeedback])

  const handleAction = useCallback(() => {
    const action = activeMessageRef.current?.action

    if (!action) {
      return
    }

    action.onAction()
    dismissFeedback()
  }, [dismissFeedback])

  const value = useMemo(
    () => ({
      dismissFeedback,
      showSuccessFeedback: (messageKey: string, options?: FeedbackMessageOptions) => {
        publishSuccess(messageKey, options)
      },
    }),
    [dismissFeedback],
  )

  return (
    <FeedbackContext.Provider value={value}>
      {children}
      {activeMessage && (
        <FeedbackToast
          ref={toastRef}
          action={activeMessage.action}
          messageKey={activeMessage.messageKey}
          onAction={activeMessage.action ? handleAction : undefined}
          onClose={dismissFeedback}
          severity={activeMessage.severity}
          variables={activeMessage.variables}
          visible
        />
      )}
    </FeedbackContext.Provider>
  )
}
