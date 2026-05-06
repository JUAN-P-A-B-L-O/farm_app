
const listeners = new Set()

export function subscribeToFeedback(listener) {
  listeners.add(listener)

  return () => {
    listeners.delete(listener)
  }
}

export function publishFeedback(message) {
  listeners.forEach((listener) => {
    listener(message)
  })
}

export function publishSuccess(messageKey, options) {
  publishFeedback({
    severity: 'success',
    messageKey,
    ...options,
  })
}
