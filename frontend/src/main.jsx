/* eslint-disable react-refresh/only-export-components */
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
// Email verification page.
import VerifyEmailPage from './VerifyEmailPage' 

// Protects routes that require an authenticated user.
const ProtectedRoute = ({ user, children }) => {
  if (user === undefined) {
    return <div>Loading session...</div>;
  }

  if (user === null) {
    return <Navigate to="/login" replace />;
  }

  return children;
};

// Root app router.
const App = () => {
  const [loggedUser, setLoggedUser] = useState(undefined);

  // Restores the current session on page load.
  useEffect(() => {
    fetch("/api/user/me", {
      credentials: "include"
    })
      .then((res) => (res.ok ? res.json() : null))
      .then((data) => setLoggedUser(data))
      .catch(() => setLoggedUser(null));
  }, []);

  // Ends the session and returns the app to the login state.
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
        {/* Root redirect */}
        <Route 
          path="/" element={<Navigate to="/login" replace />} />

        {/* Public routes */}
        <Route
          path="/login"
          element={<AuthPage onLoginSuccess={setLoggedUser} />} />

        <Route 
          path="/forgot-password" element={<ForgotPasswordPage />} />

        {/* Email verification route */}
        <Route 
          path="/verify-email" element={<VerifyEmailPage />} />

        {/* Protected layout */}
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
            path="/events" element={<AllEvents user={loggedUser} />} />

          <Route 
            path="/my-events" element={<MyEvents />} />

          <Route
            path="/profile"
            element={
              <Profile
                user={loggedUser}
                onLogout={handleLogout}
                onUserUpdated={setLoggedUser}
              />
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
