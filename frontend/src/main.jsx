import React from 'react'
import ReactDOM from 'react-dom/client'
import AuthPage from './authPage.jsx'
import './index.css'
import CreaEvento from './creaEvento.jsx'

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <AuthPage />
    <CreaEvento />
  </React.StrictMode>,
)