import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, readdirSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-i18n-catalog')
const sourceRoot = path.join(projectRoot, 'src')

function writeCompiledModule(filename, content) {
  mkdirSync(compiledRoot, { recursive: true })
  writeFileSync(path.join(compiledRoot, filename), content)
}

function compileI18nModules() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  writeCompiledModule(
    'domainLabels.js',
    readFileSync(path.join(sourceRoot, 'i18n', 'domainLabels.ts'), 'utf8')
      .replace("import type { AnimalOrigin, AnimalStatus } from '../types/animal'\n", '')
      .replace("import type { UserRole } from '../types/user'\n", '')
      .replace("type Translate = (key: string) => string\n\n", '')
      .replace('const USER_ROLES: UserRole[] = [\'MANAGER\', \'WORKER\']\n', 'const USER_ROLES = [\'MANAGER\', \'WORKER\']\n')
      .replace('const ANIMAL_STATUSES: AnimalStatus[] = [\'ACTIVE\', \'SOLD\', \'DEAD\', \'INACTIVE\']\n', 'const ANIMAL_STATUSES = [\'ACTIVE\', \'SOLD\', \'DEAD\', \'INACTIVE\']\n')
      .replace('const ANIMAL_ORIGINS: AnimalOrigin[] = [\'BORN\', \'PURCHASED\']\n', 'const ANIMAL_ORIGINS = [\'BORN\', \'PURCHASED\']\n')
      .replace('function hasValue<Option extends string>(options: readonly Option[], value: string): value is Option {', 'function hasValue(options, value) {')
      .replace('  return options.includes(value as Option)\n', '  return options.includes(value)\n')
      .replace('export function getUserRoleLabel(t: Translate, role: string) {', 'export function getUserRoleLabel(t, role) {')
      .replace('export function getUserActiveLabel(t: Translate, active: boolean) {', 'export function getUserActiveLabel(t, active) {')
      .replace('export function getAnimalStatusLabel(t: Translate, status: string) {', 'export function getAnimalStatusLabel(t, status) {')
      .replace('export function getAnimalOriginLabel(t: Translate, origin: string) {', 'export function getAnimalOriginLabel(t, origin) {'),
  )

  writeCompiledModule(
    'translations.js',
    `${readFileSync(path.join(sourceRoot, 'i18n', 'en.ts'), 'utf8')
      .replace('const en =', 'const enTranslations =')
      .replaceAll(' as const', '')
      .replace('export default en', '')}
${readFileSync(path.join(sourceRoot, 'i18n', 'pt-BR.ts'), 'utf8')
      .replace('const ptBR =', 'const ptBRTranslations =')
      .replaceAll(' as const', '')
      .replace('export default ptBR', '')}

export const translations = {
  'pt-BR': ptBRTranslations,
  en: enTranslations,
}
`,
  )
}

function collectSourceFiles(dir, result = []) {
  for (const entry of readdirSync(dir, { withFileTypes: true })) {
    const entryPath = path.join(dir, entry.name)
    if (entry.isDirectory()) {
      if (!entryPath.includes(`${path.sep}i18n`)) {
        collectSourceFiles(entryPath, result)
      }
      continue
    }

    if (entry.name.endsWith('.ts') || entry.name.endsWith('.tsx')) {
      result.push(entryPath)
    }
  }

  return result
}

function extractTranslationKeys() {
  const keyPattern = /(?:^|[^A-Za-z0-9_$.])t\((['"`])([^'"`$]+)\1/g
  const keys = new Set()

  for (const file of collectSourceFiles(sourceRoot)) {
    const contents = readFileSync(file, 'utf8')
    let match

    while ((match = keyPattern.exec(contents)) !== null) {
      keys.add(match[2])
    }
  }

  return [...keys].sort()
}

function flattenTranslations(source, prefix = '', result = new Set()) {
  for (const [key, value] of Object.entries(source)) {
    const nextKey = prefix ? `${prefix}.${key}` : key

    if (typeof value === 'string') {
      result.add(nextKey)
      continue
    }

    flattenTranslations(value, nextKey, result)
  }

  return result
}

compileI18nModules()

const translationsModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'translations.js')).href}?t=${Date.now()}`
const domainLabelsModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'domainLabels.js')).href}?t=${Date.now()}`
const { translations } = await import(translationsModuleUrl)
const {
  getAnimalOriginLabel,
  getAnimalStatusLabel,
  getUserActiveLabel,
  getUserRoleLabel,
} = await import(domainLabelsModuleUrl)

function createTranslator(language) {
  const flattenedKeys = flattenTranslations(translations[language])

  return (key) => flattenedKeys.has(key) ? key.split('.').reduce((current, part) => current?.[part], translations[language]) : key
}

test('every translation key referenced by the frontend exists in both catalogs', () => {
  const usedKeys = extractTranslationKeys()
  const ptKeys = flattenTranslations(translations['pt-BR'])
  const enKeys = flattenTranslations(translations.en)

  const missingPt = usedKeys.filter((key) => !ptKeys.has(key))
  const missingEn = usedKeys.filter((key) => !enKeys.has(key))

  assert.deepEqual(missingPt, [])
  assert.deepEqual(missingEn, [])
})

test('domain label helpers centralize enum and boolean display labels with safe fallback', () => {
  const translatePt = createTranslator('pt-BR')
  const translateEn = createTranslator('en')

  assert.equal(getUserRoleLabel(translatePt, 'MANAGER'), 'Gerente')
  assert.equal(getUserRoleLabel(translateEn, 'WORKER'), 'Worker')
  assert.equal(getUserActiveLabel(translatePt, true), 'Ativo')
  assert.equal(getAnimalStatusLabel(translatePt, 'SOLD'), 'Vendido')
  assert.equal(getAnimalOriginLabel(translateEn, 'PURCHASED'), 'Purchased')
  assert.equal(getUserRoleLabel(translatePt, 'OWNER'), 'OWNER')
  assert.equal(getAnimalStatusLabel(translatePt, 'ARCHIVED'), 'ARCHIVED')
})
