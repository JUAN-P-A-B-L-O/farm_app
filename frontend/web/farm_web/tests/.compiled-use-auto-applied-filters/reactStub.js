let currentHarness = null

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
    callbacks: [],
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

export function useCallback(callback, deps) {
  const harness = getHarness()
  const index = harness.cursor++
  const previousCallback = harness.callbacks[index]

  if (!previousCallback || !areHookDepsEqual(previousCallback.deps, deps)) {
    harness.callbacks[index] = {
      deps,
      value: callback,
    }
  }

  return harness.callbacks[index].value
}

export function useEffect(effect, deps) {
  const harness = getHarness()
  const index = harness.cursor++
  const previousEffect = harness.effects[index]

  if (!previousEffect || !areHookDepsEqual(previousEffect.deps, deps)) {
    harness.pendingEffects.push({ index, effect, deps })
  }
}
