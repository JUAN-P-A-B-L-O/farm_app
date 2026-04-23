import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-csv-export')

function compileCsvExportService() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  const csvExportSource = readFileSync(
    path.join(projectRoot, 'src', 'services', 'csvExportService.ts'),
    'utf8',
  )

  writeFileSync(
    path.join(compiledRoot, 'csvExportService.js'),
    csvExportSource
      .replace("import api from './api'\n", "import api from './api.js'\n")
      .replace(/type CsvParamValue = .*\n/g, '')
      .replace(/type CsvParams = .*\n/g, '')
      .replace('function sanitizeParams(params?: CsvParams) {', 'function sanitizeParams(params) {')
      .replace(
        'function parseFileName(contentDisposition: string | undefined, fallbackFileName: string) {',
        'function parseFileName(contentDisposition, fallbackFileName) {',
      )
      .replace(
        `export async function downloadCsv(
  endpoint: string,
  params?: CsvParams,
  fallbackFileName = 'export.csv',
): Promise<void> {`,
        `export async function downloadCsv(
  endpoint,
  params,
  fallbackFileName = 'export.csv',
) {`,
      )
      .replace('api.get<Blob>', 'api.get'),
  )
}

compileCsvExportService()

const apiStub = {
  requests: [],
  response: {
    data: new Blob(['default'], { type: 'text/csv;charset=utf-8' }),
    headers: {},
  },
  async get(url, config) {
    this.requests.push({ url, config })
    return this.response
  },
}

writeFileSync(
  path.join(compiledRoot, 'api.js'),
  `const api = globalThis.__csvExportApiStub;\nexport default api;\n`,
)

const createdLinks = []
const createObjectUrlCalls = []
const revokeObjectUrlCalls = []

globalThis.__csvExportApiStub = apiStub
globalThis.document = {
  body: {
    appendChild(link) {
      createdLinks.push(link)
    },
  },
  createElement(tagName) {
    assert.equal(tagName, 'a')
    return {
      clickCalled: false,
      removed: false,
      click() {
        this.clickCalled = true
      },
      remove() {
        this.removed = true
      },
    }
  },
}
globalThis.window = {
  URL: {
    createObjectURL(blob) {
      createObjectUrlCalls.push(blob)
      return 'blob:csv-download'
    },
    revokeObjectURL(url) {
      revokeObjectUrlCalls.push(url)
    },
  },
}

const csvExportModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'csvExportService.js')).href}?t=${Date.now()}`
const { downloadCsv } = await import(csvExportModuleUrl)

test.beforeEach(() => {
  apiStub.requests = []
  apiStub.response = {
    data: new Blob(['id,name\n1,Jane\n'], { type: 'text/csv;charset=utf-8' }),
    headers: {},
  }
  createdLinks.length = 0
  createObjectUrlCalls.length = 0
  revokeObjectUrlCalls.length = 0
})

test('downloadCsv sanitizes params but preserves false and zero values', async () => {
  await downloadCsv('/users/export', {
    search: '',
    active: false,
    page: 0,
    role: null,
    farmId: 'farm-1',
  }, 'users.csv')

  assert.deepEqual(apiStub.requests[0], {
    url: '/users/export',
    config: {
      params: {
        active: false,
        page: 0,
        farmId: 'farm-1',
      },
      responseType: 'blob',
    },
  })
})

test('downloadCsv uses the UTF-8 filename from the response headers', async () => {
  apiStub.response = {
    data: new Blob(['header\nvalue\n'], { type: 'text/csv;charset=utf-8' }),
    headers: {
      'content-disposition': "attachment; filename*=UTF-8''relat%C3%B3rio.csv",
    },
  }

  await downloadCsv('/dashboard/export', { includeAcquisitionCost: true }, 'fallback.csv')

  assert.equal(createdLinks.length, 1)
  assert.equal(createdLinks[0].href, 'blob:csv-download')
  assert.equal(createdLinks[0].download, 'relatório.csv')
  assert.equal(createdLinks[0].clickCalled, true)
  assert.equal(createdLinks[0].removed, true)
  assert.equal(createObjectUrlCalls.length, 1)
  assert.equal(revokeObjectUrlCalls[0], 'blob:csv-download')
})
