import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-feedback-service')

function compileFeedbackService() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  const feedbackServiceSource = readFileSync(
    path.join(projectRoot, 'src', 'services', 'feedbackService.ts'),
    'utf8',
  )

  writeFileSync(
    path.join(compiledRoot, 'feedbackService.js'),
    feedbackServiceSource
      .replace(/export type FeedbackSeverity = .*\n/g, '')
      .replace(/export interface FeedbackAction \{[\s\S]*?\}\n\n/g, '')
      .replace(/export interface FeedbackMessageOptions \{[\s\S]*?\}\n\n/g, '')
      .replace(/export interface FeedbackMessage extends FeedbackMessageOptions \{[\s\S]*?\}\n\n/g, '')
      .replace(/type FeedbackListener = .*\n\n/g, '')
      .replace('const listeners = new Set<FeedbackListener>()', 'const listeners = new Set()')
      .replace('export function subscribeToFeedback(listener: FeedbackListener) {', 'export function subscribeToFeedback(listener) {')
      .replace('export function publishFeedback(message: FeedbackMessage) {', 'export function publishFeedback(message) {')
      .replace(
        'export function publishSuccess(messageKey: string, options?: FeedbackMessageOptions) {',
        'export function publishSuccess(messageKey, options) {',
      ),
  )
}

compileFeedbackService()

const feedbackServiceModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'feedbackService.js')).href}?t=${Date.now()}`
const { publishSuccess, subscribeToFeedback } = await import(feedbackServiceModuleUrl)

test('publishSuccess notifies subscribers with the success payload and forwarded options', () => {
  const received = []
  const unsubscribe = subscribeToFeedback((message) => {
    received.push(message)
  })

  publishSuccess('settings.success.passwordUpdated', {
    dedupeKey: 'settings:update-password',
    durationMs: 1200,
    variables: { user: 'Maria' },
  })

  unsubscribe()

  assert.deepEqual(received, [
    {
      severity: 'success',
      messageKey: 'settings.success.passwordUpdated',
      dedupeKey: 'settings:update-password',
      durationMs: 1200,
      variables: { user: 'Maria' },
    },
  ])
})

test('unsubscribe stops subsequent feedback notifications', () => {
  const received = []
  const unsubscribe = subscribeToFeedback((message) => {
    received.push(message)
  })

  unsubscribe()
  publishSuccess('animals.success.create', { dedupeKey: 'animals:create' })

  assert.deepEqual(received, [])
})

test('feedback wiring keeps translated toast accessibility labels and provider ordering in the app root', () => {
  const mainSource = readFileSync(path.join(projectRoot, 'src', 'main.tsx'), 'utf8')
  const toastSource = readFileSync(path.join(projectRoot, 'src', 'components', 'common', 'FeedbackToast.tsx'), 'utf8')

  assert.match(
    mainSource,
    /<LanguageProvider>\s*<FeedbackProvider>\s*<CurrencyProvider>/,
  )
  assert.match(toastSource, /role="status"/)
  assert.match(toastSource, /aria-live="polite"/)
  assert.match(toastSource, /aria-atomic="true"/)
  assert.match(toastSource, /aria-label=\{t\('common\.feedback\.close'\)\}/)
  assert.match(toastSource, /\{t\('common\.feedback\.dismiss'\)\}/)
})
