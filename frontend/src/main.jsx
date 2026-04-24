//main
import React, { useState, useEffect } from 'react'
import ReactDOM from 'react-dom/client'
import { BrowserRouter, Routes, Route, Navigate } from 'react-router-dom'
import MainLayout from './MainLayout' 
import Dashboard from './Dashboard'
import AuthPage from './authPage'
import AllEvents from './AllEvents'
import Profile from './Profile'
import './index.css'
import MyEvents from './MyEvents'
import ForgotPasswordPage from './ForgotPasswordPage'

// ===================== PROTECTED ROUTE =====================
const ProtectedRoute = ({ user, children }) => {
  if (user === undefined) {
    return <div>Loading session...</div>;
  }

  if (user === null) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

// ===================== APP =====================
const App = () => {
  const [loggedUser, setLoggedUser] = useState(undefined);

  // CHECK SESSION
  useEffect(() => {
    fetch("/api/user/me", {
      credentials: "include"
    })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => setLoggedUser(data))
      .catch(() => setLoggedUser(null));
  }, []);

  // LOGOUT
  const handleLogout = async () => {
    try {
      await fetch("/api/user/logout", {
        method: "POST"
      });
    } catch (err) {
      console.error("Errore logout", err);
    } finally {
      setLoggedUser(null);
    }
  };

  return (
    <BrowserRouter>
      <Routes>
        {/* redirect root */}
        <Route 
          path="/" element={<Navigate to="/login" replace />} />

        {/* public routes */}
        <Route
          path="/login"
          element={<AuthPage onLoginSuccess={setLoggedUser} />} />

        <Route 
          path="/forgot-password" element={<ForgotPasswordPage />} />

        {/* protected layout */}
        <Route
          element={
            <ProtectedRoute user={loggedUser}>
              <MainLayout user={loggedUser} />
            </ProtectedRoute>
          }
        >
          <Route
            path="/dashboard"
            element={<Dashboard user={loggedUser} />}
          />

          <Route 
            path="/events" element={<AllEvents />} />

          <Route 
            path="/my-events" element={<MyEvents />} />

          <Route
            path="/profile"
            element={
              <Profile user={loggedUser} onLogout={handleLogout} />
            }
          />
        </Route>
      </Routes>
    </BrowserRouter>
  );
};

ReactDOM.createRoot(document.getElementById("root")).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);