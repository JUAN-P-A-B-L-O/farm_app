import { StrictMode } from 'react'
import { createRoot } from 'react-dom/client'
import { BrowserRouter } from 'react-router-dom'
import './index.css'
import App from './App.tsx'
import { AuthProvider } from './context/AuthContext.tsx'
import { CurrencyProvider } from './context/CurrencyContext.tsx'
import { FarmProvider } from './context/FarmContext.tsx'
import { LanguageProvider } from './context/LanguageContext.tsx'
import { MeasurementUnitProvider } from './context/MeasurementUnitContext.tsx'

createRoot(document.getElementById('root')!).render(
  <StrictMode>
    <AuthProvider>
      <FarmProvider>
        <LanguageProvider>
          <CurrencyProvider>
            <MeasurementUnitProvider>
              <BrowserRouter>
                <App />
              </BrowserRouter>
            </MeasurementUnitProvider>
          </CurrencyProvider>
        </LanguageProvider>
      </FarmProvider>
    </AuthProvider>
  </StrictMode>,
)
