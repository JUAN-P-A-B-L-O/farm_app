import assert from 'node:assert/strict'
import { mkdirSync, readFileSync, rmSync, writeFileSync } from 'node:fs'
import path from 'node:path'
import { setTimeout as delay } from 'node:timers/promises'
import test from 'node:test'
import ts from 'typescript'
import { fileURLToPath, pathToFileURL } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')
const compiledRoot = path.join(projectRoot, 'tests', '.compiled-use-auto-applied-filters')

function compileUseAutoAppliedFilters() {
  rmSync(compiledRoot, { force: true, recursive: true })
  mkdirSync(compiledRoot, { recursive: true })

  const hookSource = readFileSync(
    path.join(projectRoot, 'src', 'hooks', 'useAutoAppliedFilters.ts'),
    'utf8',
  ).replace(
    "import { useCallback, useEffect, useRef, useState } from 'react'",
    "import { useCallback, useEffect, useRef, useState } from './reactStub.js'",
  )

  const compiledHook = ts.transpileModule(hookSource, {
    compilerOptions: {
      module: ts.ModuleKind.ES2022,
      target: ts.ScriptTarget.ES2022,
    },
  })

  writeFileSync(path.join(compiledRoot, 'useAutoAppliedFilters.js'), compiledHook.outputText)
  writeFileSync(
    path.join(compiledRoot, 'reactStub.js'),
    `let currentHarness = null

function getHarness() {
  if (!currentHarness) {
    throw new Error('Hooks can only run during a test render')
  }

  return currentHarness
}

function areHookDepsEqual(previousDeps, nextDeps) {
  if (previousDeps === undefined || nextDeps === undefined) {
    return false
  }

  return previousDeps.length === nextDeps.length
    && previousDeps.every((value, index) => Object.is(value, nextDeps[index]))
}

export function createHookHarness(hook) {
  return {
    hook,
    cursor: 0,
    state: [],
    refs: [],
    effects: [],
    pendingEffects: [],
    result: undefined,
    render() {
      this.cursor = 0
      currentHarness = this

      try {
        this.result = this.hook()
      } finally {
        currentHarness = null
      }

      const pendingEffects = [...this.pendingEffects]
      this.pendingEffects = []

      for (const { index, effect, deps } of pendingEffects) {
        const previousEffect = this.effects[index]
        previousEffect?.cleanup?.()

        const cleanup = effect()
        this.effects[index] = {
          deps,
          cleanup: typeof cleanup === 'function' ? cleanup : undefined,
        }
      }

      return this.result
    },
    unmount() {
      for (const effect of this.effects) {
        effect?.cleanup?.()
      }

      this.effects = []
      this.pendingEffects = []
    },
  }
}

export function useState(initialValue) {
  const harness = getHarness()
  const index = harness.cursor++

  if (!(index in harness.state)) {
    harness.state[index] = typeof initialValue === 'function' ? initialValue() : initialValue
  }

  const setValue = (updater) => {
    harness.state[index] = typeof updater === 'function'
      ? updater(harness.state[index])
      : updater
  }

  return [harness.state[index], setValue]
}

export function useRef(initialValue) {
  const harness = getHarness()
  const index = harness.cursor++

  if (!(index in harness.refs)) {
    harness.refs[index] = { current: initialValue }
  }

  return harness.refs[index]
}

export function useCallback(callback) {
  const harness = getHarness()
  harness.cursor++
  return callback
}

export function useEffect(effect, deps) {
  const harness = getHarness()
  const index = harness.cursor++
  const previousEffect = harness.effects[index]

  if (!previousEffect || !areHookDepsEqual(previousEffect.deps, deps)) {
    harness.pendingEffects.push({ index, effect, deps })
  }
}
`,
  )
}

compileUseAutoAppliedFilters()

const reactStubUrl = pathToFileURL(path.join(compiledRoot, 'reactStub.js')).href
const hookModuleUrl = `${pathToFileURL(path.join(compiledRoot, 'useAutoAppliedFilters.js')).href}?t=${Date.now()}`
const { createHookHarness } = await import(reactStubUrl)
const { useAutoAppliedFilters } = await import(hookModuleUrl)

function copyFilters(filters) {
  return {
    ...filters,
    animalIds: Array.isArray(filters.animalIds) ? [...filters.animalIds] : filters.animalIds,
  }
}

test('useAutoAppliedFilters applies the initial filters on mount', () => {
  const calls = []
  const harness = createHookHarness(() => useAutoAppliedFilters(
    () => ({ search: '', animalIds: [] }),
    {
      debounceKeys: ['search'],
      debounceMs: 10,
      onAppliedChange: (filters) => calls.push(copyFilters(filters)),
    },
  ))

  harness.render()

  assert.deepEqual(calls, [{ search: '', animalIds: [] }])
  assert.deepEqual(harness.result.filters, { search: '', animalIds: [] })
  assert.deepEqual(harness.result.appliedFilters, { search: '', animalIds: [] })
})

test('useAutoAppliedFilters debounces configured keys but applies mixed updates immediately', async () => {
  const calls = []
  const harness = createHookHarness(() => useAutoAppliedFilters(
    { search: '', status: '' },
    {
      debounceKeys: ['search'],
      debounceMs: 20,
      onAppliedChange: (filters) => calls.push({ ...filters }),
    },
  ))

  harness.render()
  assert.equal(calls.length, 1)

  harness.result.setFilters((current) => ({ ...current, search: 'milk' }))
  harness.render()

  assert.equal(harness.result.filters.search, 'milk')
  assert.equal(harness.result.appliedFilters.search, '')
  assert.equal(calls.length, 1)

  await delay(30)
  harness.render()

  assert.deepEqual(harness.result.appliedFilters, { search: 'milk', status: '' })
  assert.deepEqual(calls.at(-1), { search: 'milk', status: '' })

  harness.result.setFilters((current) => ({ ...current, search: 'corn' }))
  harness.render()
  harness.result.setFilters((current) => ({ ...current, status: 'ACTIVE' }))
  harness.render()

  assert.deepEqual(harness.result.appliedFilters, { search: 'corn', status: 'ACTIVE' })
  assert.equal(calls.length, 3)

  await delay(30)
  harness.render()

  assert.equal(calls.length, 3)
  harness.unmount()
})

test('useAutoAppliedFilters clones array filters and ignores equivalent array updates', () => {
  const calls = []
  const harness = createHookHarness(() => useAutoAppliedFilters(
    { animalIds: [] },
    {
      onAppliedChange: (filters) => calls.push(copyFilters(filters)),
    },
  ))

  harness.render()
  assert.equal(calls.length, 1)

  const nextAnimalIds = ['animal-1']
  harness.result.applyFiltersImmediately({ animalIds: nextAnimalIds })
  nextAnimalIds.push('animal-2')
  harness.render()

  assert.deepEqual(harness.result.appliedFilters, { animalIds: ['animal-1'] })
  assert.equal(calls.length, 2)

  harness.result.setFilters((current) => ({ ...current, animalIds: ['animal-1'] }))
  harness.render()

  assert.equal(calls.length, 2)
  harness.unmount()
})
