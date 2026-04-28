import assert from 'node:assert/strict'
import { readFileSync } from 'node:fs'
import path from 'node:path'
import test from 'node:test'
import { fileURLToPath } from 'node:url'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)
const projectRoot = path.resolve(__dirname, '..')

function readSource(relativePath) {
  return readFileSync(path.join(projectRoot, relativePath), 'utf8')
}

test('app layout keeps the mobile navigation state wired to all new close and toggle entry points', () => {
  const source = readSource('src/layout/AppLayout.tsx')

  assert.match(source, /const \[isMobileNavigationOpen, setIsMobileNavigationOpen\] = useState\(false\)/)
  assert.match(source, /className=\{`app-layout\$\{isMobileNavigationOpen \? ' app-layout--nav-open' : ''\}`\}/)
  assert.match(source, /className="app-layout__backdrop"/)
  assert.match(source, /aria-label=\{t\('layout\.closeNavigation'\)\}/)
  assert.match(source, /onClick=\{\(\) => setIsMobileNavigationOpen\(false\)\}/)
  assert.match(source, /className="app-layout__sidebar-close"/)
  assert.match(source, /onClick=\{\(\) => setIsMobileNavigationOpen\(\(current\) => !current\)\}/)
  assert.match(source, /aria-expanded=\{isMobileNavigationOpen\}/)
  assert.match(source, /aria-controls="app-layout-sidebar-navigation"/)
  assert.match(source, /onClick=\{\(\) => \{\s+setIsMobileNavigationOpen\(false\)\s+navigate\('\/farms\/new'\)\s+\}\}/)
  assert.match(source, /function handleLogout\(\) {\s+setIsMobileNavigationOpen\(false\)/)
  assert.match(source, /onClick=\{\(\) => setIsMobileNavigationOpen\(false\)\}\s+className=\{\(\{ isActive \}\) =>/)
})

test('app layout source preserves the new mobile context summary and responsive navigation styles', () => {
  const layoutSource = readSource('src/layout/AppLayout.tsx')
  const cssSource = readSource('src/App.css')

  assert.match(layoutSource, /activeNavigationItem \? t\(activeNavigationItem\.labelKey\) : t\('layout\.farmLabel'\)/)
  assert.match(layoutSource, /selectedFarm\?\.name \?\? t\('layout\.selectFarm'\)/)

  assert.match(cssSource, /\.app-layout__backdrop \{/)
  assert.match(cssSource, /\.app-layout__sidebar \{[\s\S]*transform: translateX\(-100%\)/)
  assert.match(cssSource, /\.app-layout--nav-open \.app-layout__backdrop \{[\s\S]*pointer-events: auto;/)
  assert.match(cssSource, /\.app-layout--nav-open \.app-layout__sidebar \{[\s\S]*transform: translateX\(0\);/)
  assert.match(cssSource, /\.app-layout__mobile-bar \{/)
  assert.match(cssSource, /@media \(min-width: 901px\) \{[\s\S]*\.app-layout__backdrop,\s+\.app-layout__mobile-bar,\s+\.app-layout__sidebar-close \{[\s\S]*display: none;/)
  assert.match(cssSource, /@media \(min-width: 901px\) \{[\s\S]*\.app-layout__sidebar \{[\s\S]*position: sticky;[\s\S]*transform: none;/)
})
