export function publishSuccess(messageKey, options) {
  globalThis.__csvExportPublishSuccessCalls.push({ messageKey, options })
}
