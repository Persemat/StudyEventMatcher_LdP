import React, { useState, useEffect } from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from './MainLayout' 
import Dashboard from './Dashboard'
import AuthPage from './authPage'
import './index.css'


// Se l'utente non è loggato, lo rispedisce al login.
const ProtectedRoute = ({ user, children }) => {
  // CASO 1: La fetch sta ancora viaggiando. 
  if (user === undefined) {
    return <div>Caricamento sessione...</div>; 
  }

  // CASO 2: La fetch è finita e il backend non ha rilevato una sessione attiva
  if (user === null) {
    return <Navigate to="/login" replace />;
  }

  // CASO 3: La fetch è finita e l'utente esiste
  return children;
};

const App = () => {
  
  // Usiamo 'undefined' per dire "sto caricando" e 'null' per dire "non loggato"
const [loggedUser, setLoggedUser] = useState(undefined); 

useEffect(() => {
  fetch('/Utenti/me', { credentials: 'include' })
    .then(res => res.ok ? res.json() : null)
    .then(data => setLoggedUser(data)) // Sarà l'oggetto utente o null
    .catch(() => setLoggedUser(null));
}, []);

  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Navigate to="/login" replace />} />
        
        {/* Passiamo la funzione per settare l'utente dopo il login */}
        <Route path="/login" element={<AuthPage onLoginSuccess={setLoggedUser} />} />

        {/* Avvolgiamo il MainLayout nel ProtectedRoute */}
        <Route element={
          <ProtectedRoute user={loggedUser}>
            <MainLayout user={loggedUser} />
          </ProtectedRoute>
        }>
          <Route path="/dashboard" element={<Dashboard user={loggedUser} />} />
          {/* Qui vanno aggiunte le rotte per le altre pagine */}
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

ReactDOM.createRoot(document.getElementById('root')).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
)