import React from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route } from 'react-router-dom'
import MainLayout from './MainLayout' 
import Dashboard from './Dashboard'
import AuthPage from './authPage'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <BrowserRouter>
      <Routes>
        
        {/* Gestione della ROOT: se l'utente entra nel sito (/), lo mandiamo al login */}
        <Route path="/" element={<Navigate to="/login" replace />} />

        <Route path="/login" element={<AuthPage />} />

        <Route element={<MainLayout />}>
          <Route path="/dashboard" element={<Dashboard />} />
          {/*<Route path="/eventi" element={<PaginaEventi />} /> */}
          {/* Aggiungi qui le altre: miei-eventi, profilo, ecc. */}
        </Route>
      </Routes>
    </BrowserRouter>
  </React.StrictMode>
)