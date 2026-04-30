import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-translation-hook')

function compileTranslationModules() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  writeFileSync(
    path.join(compiledRoot, 'useTranslation.js'),
    readFileSync(path.join(projectRoot, 'src', 'hooks', 'useTranslation.ts'), 'utf8')
      .replace(
        "import { useLanguage, translations } from '../context/LanguageContext'\n",
        "import { useLanguage, translations } from './LanguageContext.js'\n",
      )
      .replace(
        'function getNestedValue(source: unknown, key: string): string | undefined {',
        'function getNestedValue(source, key) {',
      )
      .replace(
        '      return (currentValue as Record<string, unknown>)[part]\n',
        '      return currentValue[part]\n',
      )
      .replace('.reduce<unknown>(', '.reduce(')
      .replace('  }, source) as string | undefined\n', '  }, source)\n')
      .replace(
        `function interpolate(
  template: string,
  variables: Record<string, string | number> | undefined,
) {`,
        `function interpolate(
  template,
  variables,
) {`,
      )
      .replace(
        '  function t(key: string, variables?: Record<string, string | number>): string {',
        '  function t(key, variables) {',
      ),
  )

  writeFileSync(
    path.join(compiledRoot, 'LanguageContext.js'),
    `${readFileSync(path.join(projectRoot, 'src', 'i18n', 'en.ts'), 'utf8')
      .replace('const en =', 'const enTranslations =')
      .replaceAll(' as const', '')
      .replace('export default en', '')}
${readFileSync(path.join(projectRoot, 'src', 'i18n', 'pt-BR.ts'), 'utf8')
      .replace('const ptBR =', 'const ptBRTranslations =')
      .replaceAll(' as const', '')
      .replace('export default ptBR', '')}

export const translations = {
  'pt-BR': ptBRTranslations,
  en: enTranslations,
}

export function useLanguage() {
  return { language: globalThis.__testLanguage ?? 'pt-BR' }
}
`,
  )
}

compileTranslationModules()

const translationHookModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'useTranslation.js')).href}?t=${Date.now()}`
const { useTranslation } = await import(translationHookModuleUrl)

test.beforeEach(() => {
  globalThis.__testLanguage = 'pt-BR'
})

test('interpolates pagination and dialog templates through the translation hook', () => {
  const { t } = useTranslation()

  assert.equal(
    t('common.pagination.summary', { from: 1, to: 10, total: 42 }),
    'Exibindo 1-10 de 42',
  )
  assert.equal(
    t('animals.sellDescription', { tag: 'A-102' }),
    'Registre o valor da venda de A-102 e encerre seu ciclo como vendido.',
  )
  assert.equal(
    t('accessControl.activateDescription', { name: 'Maria Silva' }),
    'Defina uma senha temporária para reativar Maria Silva.',
  )
})

test('returns translated placeholders and details-section labels for both supported languages', () => {
  let translation = useTranslation()

  assert.equal(translation.t('layout.navigation.dashboard'), 'Painel')
  assert.equal(translation.t('auth.title'), 'Entrar')
  assert.equal(translation.t('dashboard.title'), 'Painel')
  assert.equal(
    translation.t('dashboard.filters.allAnimalsHint'),
    'Deixe vazio para incluir todos os animais. A seleção múltipla é suportada.',
  )
  assert.equal(translation.t('animals.form.placeholders.tag'), 'A-102')
  assert.equal(translation.t('animals.form.placeholders.farmId'), 'fazenda-001')
  assert.equal(translation.t('accessControl.form.placeholders.password'), 'farmapp@123')
  assert.equal(translation.t('animals.detailsSections.productionTitle'), 'Histórico de produção')
  assert.equal(translation.t('animals.detailsSections.feedingEmpty'), 'Nenhum registro de alimentação encontrado para este animal.')
  assert.equal(translation.t('feedType.confirmDelete'), 'Tem certeza de que deseja excluir este tipo de ração?')
  assert.equal(translation.t('analytics.includeAcquisitionCost'), 'Incluir custo de aquisição no lucro')
  assert.equal(translation.t('layout.languageOptions.pt-BR'), 'Português')
  assert.equal(translation.t('layout.currencyOptions.USD'), 'Dólar americano (USD)')

  globalThis.__testLanguage = 'en'
  translation = useTranslation()

  assert.equal(translation.t('animals.form.placeholders.farmId'), 'farm-001')
  assert.equal(translation.t('accessControl.form.placeholders.email'), 'mary.smith@farmapp.com')
  assert.equal(
    translation.t('dashboard.filters.allAnimalsHint'),
    'Leave empty to include every animal. Multiple selection is supported.',
  )
  assert.equal(translation.t('animals.detailsSections.productionDescription'), 'Production records sorted from most recent to oldest.')
  assert.equal(translation.t('animals.detailsSections.feedingTitle'), 'Feeding History')
  assert.equal(translation.t('analytics.includeAcquisitionCost'), 'Include acquisition cost in profit')
  assert.equal(translation.t('layout.languageOptions.en'), 'English')
  assert.equal(translation.t('layout.currencyOptions.BRL'), 'Brazilian real (BRL)')
})

test('falls back to the key when a translation path does not exist', () => {
  const { t } = useTranslation()

  assert.equal(t('animals.missing.translation'), 'animals.missing.translation')
})
