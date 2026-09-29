import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-auth-service')

class LocalStorageMock {
  #store = new Map()

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

function compileModules() {
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
  const authServiceSource = readFileSync(
    path.join(projectRoot, 'src', 'services', 'authService.ts'),
    'utf8',
  )

  writeFileSync(
    path.join(compiledRoot, 'authStorage.js'),
    authStorageSource
      .replace("import type { User } from '../types/user'\n\n", '')
      .replaceAll(': User | null', '')
      .replaceAll(': string', '')
      .replaceAll(': User', '')
      .replaceAll(' as User', ''),
  )

  writeFileSync(
    path.join(compiledRoot, 'api.js'),
    apiSource
      .replace(
        "import { clearAuthSession, getStoredToken } from './authStorage'\n",
        "import { clearAuthSession, getStoredToken } from './authStorage.js'\n",
      )
      .replaceAll('import.meta.env.VITE_API_URL', 'globalThis.__TEST_VITE_API_URL__')
      .replace('type UnauthorizedHandler = () => void\n\n', '')
      .replaceAll(': UnauthorizedHandler | null', '')
      .replaceAll(': string', '')
      .replaceAll(': boolean', '')
      .replaceAll(': unknown', ''),
  )

  writeFileSync(
    path.join(compiledRoot, 'authService.js'),
    authServiceSource
      .replace("import api from './api'\n", "import api from './api.js'\n")
      .replace("import type { User } from '../types/user'\n\n", '')
      .replace(/export interface LoginResponse \{[\s\S]*?\}\n\n/g, '')
      .replace(/export interface LoginCredentials \{[\s\S]*?\}\n\n/g, '')
      .replace(/export interface RegisterAccountRequest \{[\s\S]*?\}\n\n/g, '')
      .replace(
        'export async function login(email: string, password: string): Promise<LoginResponse> {',
        'export async function login(email, password) {',
      )
      .replace('api.post<LoginResponse>', 'api.post')
      .replace(
        'export async function registerAccount(data: RegisterAccountRequest): Promise<User> {',
        'export async function registerAccount(data) {',
      )
      .replace('api.post<User>', 'api.post'),
  )
}

const localStorage = new LocalStorageMock()
globalThis.window = { localStorage }
globalThis.__TEST_VITE_API_URL__ = undefined

async function loadModules() {
  compileModules()
  const cacheKey = Date.now() + Math.random()
  const authServiceModuleUrl =
    `${pathToFileURL(path.join(compiledRoot, 'authService.js')).href}?t=${cacheKey}`
  const apiModuleUrl = pathToFileURL(path.join(compiledRoot, 'api.js')).href

  const authServiceModule = await import(authServiceModuleUrl)
  const apiModule = await import(apiModuleUrl)

  return {
    api: apiModule.default,
    registerAccount: authServiceModule.registerAccount,
  }
}

test('registerAccount posts the signup payload to the register endpoint and returns the created user', async () => {
  const { api, registerAccount } = await loadModules()
  const payload = {
    name: 'Maria Silva',
    email: 'maria@farm.com',
    password: 'farmapp@123',
  }
  const createdUser = {
    id: 'user-1',
    name: 'Maria Silva',
    email: 'maria@farm.com',
    role: 'MANAGER',
    active: true,
    farmIds: [],
  }

  let capturedRequest = null
  api.post = async (url, data) => {
    capturedRequest = { url, data }
    return { data: createdUser }
  }

  const response = await registerAccount(payload)

  assert.deepEqual(capturedRequest, {
    url: '/auth/register',
    data: payload,
  })
  assert.deepEqual(response, createdUser)
})
