import { createContext } from 'react'
import type { FeedbackMessageOptions } from '../services/feedbackService'

export interface FeedbackContextValue {
  dismissFeedback: () => void
  showSuccessFeedback: (messageKey: string, options?: FeedbackMessageOptions) => void
}

export const FeedbackContext = createContext<FeedbackContextValue | undefined>(undefined)
