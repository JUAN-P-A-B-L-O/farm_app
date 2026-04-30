import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-api')

class LocalStorageMock {
  #store = new Map()

  clear() {
    this.#store.clear()
  }

  getItem(key) {
    return this.#store.has(key) ? this.#store.get(key) : null
  }

  removeItem(key) {
    this.#store.delete(key)
  }

  setItem(key, value) {
    this.#store.set(key, String(value))
  }
}

function compileServices() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  const authStorageSource = readFileSync(
    path.join(projectRoot, 'src', 'services', 'authStorage.ts'),
    'utf8',
  )
  const apiSource = readFileSync(
    path.join(projectRoot, 'src', 'services', 'api.ts'),
    'utf8',
  )

  const authStorageOutputPath = path.join(compiledRoot, 'authStorage.js')
  const apiOutputPath = path.join(compiledRoot, 'api.js')

  writeFileSync(
    authStorageOutputPath,
    authStorageSource
      .replace("import type { User } from '../types/user'\n\n", '')
      .replaceAll(': User | null', '')
      .replaceAll(': string', '')
      .replaceAll(': User', '')
      .replaceAll(' as User', ''),
  )
  writeFileSync(
    apiOutputPath,
    apiSource
      .replace(
        "import { clearAuthSession, getStoredToken } from './authStorage'\n",
        "import { clearAuthSession, getStoredToken } from './authStorage.js'\n",
      )
      .replaceAll('import.meta.env.VITE_API_URL', 'globalThis.__TEST_VITE_API_URL__')
      .replace('type UnauthorizedHandler = () => void\n\n', '')
      .replaceAll(': UnauthorizedHandler | null', '')
      .replaceAll(': unknown', ''),
  )
}

function buildAxiosError(url, status) {
  return {
    config: { url },
    isAxiosError: true,
    response: { status },
  }
}

const localStorage = new LocalStorageMock()
globalThis.window = { localStorage }
globalThis.__TEST_VITE_API_URL__ = undefined

async function loadApiModules(viteApiUrl) {
  globalThis.__TEST_VITE_API_URL__ = viteApiUrl
  compileServices()

  const cacheKey = Date.now() + Math.random()
  const apiModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'api.js')).href}?t=${cacheKey}`
  const authStorageModuleUrl =
    `${pathToFileURL(path.join(compiledRoot, 'authStorage.js')).href}?t=${cacheKey}`

  const apiModule = await import(apiModuleUrl)
  const authStorage = await import(authStorageModuleUrl)
  const api = apiModule.default

  return {
    api,
    apiModule,
    authStorage,
    requestHandler: api.interceptors.request.handlers[0].fulfilled,
    responseErrorHandler: api.interceptors.response.handlers[0].rejected,
  }
}

let apiModule
let authStorage
let api
let requestHandler
let responseErrorHandler

test.beforeEach(async () => {
  ;({ api, apiModule, authStorage, requestHandler, responseErrorHandler } = await loadApiModules())
  window.localStorage.clear()
  apiModule.registerUnauthorizedHandler(null)
  apiModule.resetUnauthorizedHandling()
})

test('uses localhost as the default API base URL when VITE_API_URL is not set', () => {
  assert.equal(api.defaults.baseURL, 'http://localhost:8080')
})

test('uses VITE_API_URL as the API base URL when it is configured', async () => {
  const configuredApiUrl = 'https://farm.example/api'
  const loadedModules = await loadApiModules(configuredApiUrl)

  assert.equal(loadedModules.api.defaults.baseURL, configuredApiUrl)
})

test('attaches the stored JWT to protected requests', async () => {
  window.localStorage.setItem(authStorage.AUTH_TOKEN_STORAGE_KEY, 'token-123')

  const config = await requestHandler({ headers: {} })

  assert.equal(config.headers.Authorization, 'Bearer token-123')
})

test('clears the stored session and notifies the unauthorized handler on the first protected 401 only', async () => {
  window.localStorage.setItem(authStorage.AUTH_TOKEN_STORAGE_KEY, 'expired-token')
  window.localStorage.setItem(authStorage.AUTH_USER_STORAGE_KEY, JSON.stringify({ id: 'user-1' }))

  let handlerCalls = 0
  apiModule.registerUnauthorizedHandler(() => {
    handlerCalls += 1
  })

  const error = buildAxiosError('/animals', 401)

  await assert.rejects(responseErrorHandler(error), (received) => received === error)
  await assert.rejects(responseErrorHandler(error), (received) => received === error)

  assert.equal(window.localStorage.getItem(authStorage.AUTH_TOKEN_STORAGE_KEY), null)
  assert.equal(window.localStorage.getItem(authStorage.AUTH_USER_STORAGE_KEY), null)
  assert.equal(handlerCalls, 1)
})

test('ignores login 401 responses so failed authentication attempts do not log out the current session', async () => {
  window.localStorage.setItem(authStorage.AUTH_TOKEN_STORAGE_KEY, 'current-token')
  window.localStorage.setItem(authStorage.AUTH_USER_STORAGE_KEY, JSON.stringify({ id: 'user-1' }))

  let handlerCalls = 0
  apiModule.registerUnauthorizedHandler(() => {
    handlerCalls += 1
  })

  const error = buildAxiosError('/auth/login', 401)

  await assert.rejects(responseErrorHandler(error), (received) => received === error)

  assert.equal(window.localStorage.getItem(authStorage.AUTH_TOKEN_STORAGE_KEY), 'current-token')
  assert.equal(handlerCalls, 0)
})

test('handles a later 401 again after the session flow explicitly resets unauthorized handling', async () => {
  let handlerCalls = 0
  apiModule.registerUnauthorizedHandler(() => {
    handlerCalls += 1
  })

  window.localStorage.setItem(authStorage.AUTH_TOKEN_STORAGE_KEY, 'expired-token')
  window.localStorage.setItem(authStorage.AUTH_USER_STORAGE_KEY, JSON.stringify({ id: 'user-1' }))
  await assert.rejects(responseErrorHandler(buildAxiosError('/feedings', 401)), (received) => Boolean(received))

  window.localStorage.setItem(authStorage.AUTH_TOKEN_STORAGE_KEY, 'fresh-token')
  window.localStorage.setItem(authStorage.AUTH_USER_STORAGE_KEY, JSON.stringify({ id: 'user-1' }))
  apiModule.resetUnauthorizedHandling()

  await assert.rejects(responseErrorHandler(buildAxiosError('/productions', 401)), (received) => Boolean(received))

  assert.equal(handlerCalls, 2)
})
