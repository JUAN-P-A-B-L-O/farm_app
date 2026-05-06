import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-avatar')

function compileAvatarUtil() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  const avatarSource = readFileSync(
    path.join(projectRoot, 'src', 'utils', 'avatar.ts'),
    'utf8',
  )

  writeFileSync(
    path.join(compiledRoot, 'avatar.js'),
    avatarSource
      .replace(
        'export const ALLOWED_AVATAR_FILE_TYPES = [\n',
        'export const ALLOWED_AVATAR_FILE_TYPES = /** @type {const} */ ([\n',
      )
      .replace(
        "] as const\n\nconst SUPPORTED_AVATAR_DATA_URL =\n",
        "])\n\nconst SUPPORTED_AVATAR_DATA_URL =\n",
      )
      .replaceAll(': string', '')
      .replaceAll(
        " as (typeof ALLOWED_AVATAR_FILE_TYPES)[number]",
        '',
      ),
  )
}

compileAvatarUtil()

const avatarModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'avatar.js')).href}?t=${Date.now()}`
const avatarModule = await import(avatarModuleUrl)

test('accepts http, https, and supported image data URLs', () => {
  assert.equal(avatarModule.isValidAvatarUrl('https://example.com/avatar.png'), true)
  assert.equal(avatarModule.isValidAvatarUrl('http://example.com/avatar.webp'), true)
  assert.equal(
    avatarModule.isValidAvatarUrl('data:image/png;base64,iVBORw0KGgoAAAANSUhEUgAAAAUA'),
    true,
  )
})

test('rejects unsupported schemes and unsupported inline formats', () => {
  assert.equal(avatarModule.isValidAvatarUrl('javascript:alert(1)'), false)
  assert.equal(
    avatarModule.isValidAvatarUrl('data:text/html;base64,PHNjcmlwdD5hbGVydCgxKTwvc2NyaXB0Pg=='),
    false,
  )
  assert.equal(
    avatarModule.isValidAvatarUrl('data:image/svg+xml;base64,PHN2Zy8+'),
    false,
  )
})

test('accepts only the configured avatar upload MIME types', () => {
  assert.equal(avatarModule.isAllowedAvatarFileType('image/png'), true)
  assert.equal(avatarModule.isAllowedAvatarFileType('image/webp'), true)
  assert.equal(avatarModule.isAllowedAvatarFileType('image/svg+xml'), false)
})
