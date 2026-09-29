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

test('protected routing keeps first-access onboarding isolated from the main application shell', () => {
  const appSource = readSource('src/App.tsx')
  const protectedRouteSource = readSource('src/components/auth/ProtectedRoute.tsx')
  const farmContextSource = readSource('src/context/FarmContext.tsx')
  const loginSource = readSource('src/pages/login/LoginPage.tsx')

  assert.match(appSource, /<Route path="\/onboarding\/farm" element={<FarmOnboardingPage \/>} \/>/)
  assert.match(appSource, /<Route element={<AppLayout \/>}>/)

  assert.match(protectedRouteSource, /const isOnboardingRoute = location\.pathname === '\/onboarding\/farm'/)
  assert.match(protectedRouteSource, /if \(!hasResolvedFarms \|\| isLoading\) \{/)
  assert.match(protectedRouteSource, /if \(needsOnboarding && !isOnboardingRoute\) \{\s+return <Navigate to="\/onboarding\/farm" replace \/>\s+\}/)
  assert.match(protectedRouteSource, /if \(!needsOnboarding && isOnboardingRoute\) \{\s+return <Navigate to="\/" replace \/>\s+\}/)

  assert.match(farmContextSource, /const \[hasResolvedFarms, setHasResolvedFarms\] = useState\(!isAuthenticated\)/)
  assert.match(farmContextSource, /const needsOnboarding = isAuthenticated && hasResolvedFarms && farms\.length === 0/)

  assert.match(loginSource, /if \(!hasResolvedFarms\) \{\s+return null\s+\}/)
  assert.match(loginSource, /if \(needsOnboarding\) \{\s+return <Navigate to="\/onboarding\/farm" replace \/>\s+\}/)
})
