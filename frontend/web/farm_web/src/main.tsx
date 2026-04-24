import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext.tsx'
import { CurrencyProvider } from './context/CurrencyContext.tsx'
import { FarmProvider } from './context/FarmContext.tsx'
import { LanguageProvider } from './context/LanguageContext.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <FarmProvider>
        <LanguageProvider>
          <CurrencyProvider>
            <BrowserRouter>
              <App />
            </BrowserRouter>
          </CurrencyProvider>
        </LanguageProvider>
      </FarmProvider>
    </AuthProvider>
  </StrictMode>,
)
