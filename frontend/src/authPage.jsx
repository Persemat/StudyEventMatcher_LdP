//authPage
import { useState } from 'react'
import { useNavigate, Link } from "react-router-dom";

export default function AuthPage(props) {
  const [isLogin, setIsLogin] = useState(true)

  const navigate = useNavigate();

  const [name, setName] = useState('')
  const [lastname, setLastname] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confirmPassword, setConfirmPassword] = useState('')

  const [showPassword, setShowPassword] = useState(false)
  const [showConfirmPassword, setShowConfirmPassword] = useState(false)

  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  function resetMessages() {
    setMessage('')
    setError('')
  }

  async function handleSubmit(event) {
    event.preventDefault()
    resetMessages()

    if (!isLogin && password !== confirmPassword) {
      setError('Passwords do not match')
      return
    }

    try {
      const response = await fetch(isLogin ? '/api/user/login' : '/api/user', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(
          isLogin
            ? { email, password }
            : { name, lastname, email, password }
        ),
        credentials: 'include'
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)
      }

      const data = await response.json()

      if (isLogin) {
        setMessage(`Logged in as: ${data.email}`);
        props.onLoginSuccess(data);

        setTimeout(() => {
          navigate("/dashboard");
        }, 1000);

      } else {
        setMessage(
          `Registration completed for: ${data.name ?? ''} ${data.lastname ?? ''}`
        );

        setTimeout(() => {
          navigate("/login");
        }, 2000);
      }

      // reset fields
      setName('')
      setLastname('')
      setEmail('')
      setPassword('')
      setConfirmPassword('')

    } catch (error) {
      console.error(error)
      setError(error.message)
    }
  }

  return (
    <main style={styles.container}>
      <section style={styles.wrapper}>
        <div style={styles.logoCircle}>📖</div>

        <h1 style={styles.title}>StudyTogether</h1>
        <br />

        <p style={styles.subtitle}>
          {isLogin
            ? 'Bentornato! Entra nel tuo account'
            : 'Crea un tuo account e inizia a studiare con gli altri studenti'}
        </p>

        <div style={styles.card}>
          <div style={styles.tabs}>
            <button
              type="button"
              onClick={() => {
                setIsLogin(true)
                resetMessages()
              }}
              style={{
                ...styles.tabButton,
                ...(isLogin ? styles.activeTab : {}),
              }}
            >
              Accedi
            </button>

            <button
              type="button"
              onClick={() => {
                setIsLogin(false)
                resetMessages()
              }}
              style={{
                ...styles.tabButton,
                ...(!isLogin ? styles.activeTab : {}),
              }}
            >
              Registrati
            </button>
          </div>

          <form onSubmit={handleSubmit} style={styles.form}>

            {!isLogin && (
              <div style={styles.row}>
                <div style={{ ...styles.field, flex: 1 }}>
                  <label htmlFor="name" style={styles.label}>Nome</label>
                  <input
                    id="name"
                    type="text"
                    value={name}
                    onChange={(e) => setName(e.target.value)}
                    placeholder="Mario"
                    required
                    style={styles.input}
                  />
                </div>

                <div style={{ ...styles.field, flex: 1 }}>
                  <label htmlFor="lastname" style={styles.label}>Cognome</label>
                  <input
                    id="lastname"
                    type="text"
                    value={lastname}
                    onChange={(e) => setLastname(e.target.value)}
                    placeholder="Rossi"
                    required
                    style={styles.input}
                  />
                </div>
              </div>
            )}

            <div style={styles.field}>
              <label htmlFor="email" style={styles.label}>Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="mario.rossi@example.com"
                required
                style={styles.input}
              />
            </div>

            <div style={styles.field}>
              <label htmlFor="password" style={styles.label}>Password</label>

              <div style={styles.passwordWrapper}>
                <input
                  id="password"
                  type={showPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  style={styles.passwordInput}
                />

                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  style={styles.eyeButton}
                >
                  👁
                </button>
              </div>
            </div>

            {!isLogin && (
              <div style={styles.field}>
                <label htmlFor="confirmPassword" style={styles.label}>
                  Conferma password
                </label>

                <div style={styles.passwordWrapper}>
                  <input
                    id="confirmPassword"
                    type={showConfirmPassword ? 'text' : 'password'}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />

                  <button
                    type="button"
                    onClick={() => setShowConfirmPassword(!showConfirmPassword)}
                    style={styles.eyeButton}
                  >
                    👁
                  </button>
                </div>
              </div>
            )}

            {isLogin && (
              <Link to="/forgot-password" style={styles.linkButton}>
                Password dimenticata?
              </Link>
            )}

            <button type="submit" style={styles.submitButton}>
              {isLogin ? 'Accedi' : 'Registrati'}
            </button>

          </form>

          {message && <p style={styles.success}>{message}</p>}
          {error && <p style={styles.error}>{error}</p>}
        </div>
      </section>
    </main>
  )
}

const styles = {
  container: {
    minHeight: '100vh',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#eef3ff',
    fontFamily: 'Arial, sans-serif',
    padding: '24px',
  },
  wrapper: {
    width: '100%',
    maxWidth: '620px',
    display: 'flex',
    flexDirection: 'column',
    alignItems: 'center',
  },
  logoCircle: {
    width: '72px',
    height: '72px',
    borderRadius: '50%',
    backgroundColor: '#2563eb',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    fontSize: '30px',
    marginBottom: '20px',
  },
  title: {
    fontSize: '56px',
    fontWeight: '800',
    color: '#0f172a',
    marginBottom: '10px',
    textAlign: 'center',
  },
  subtitle: {
    fontSize: '18px',
    color: '#334155',
    marginBottom: '32px',
    textAlign: 'center',
  },
  card: {
    width: '100%',
    backgroundColor: '#ffffff',
    padding: '28px',
    borderRadius: '22px',
    boxShadow: '0 10px 30px rgba(0,0,0,0.08)',
  },
  tabs: {
    display: 'grid',
    gridTemplateColumns: '1fr 1fr',
    gap: '8px',
    backgroundColor: '#f1f5f9',
    borderRadius: '14px',
    padding: '6px',
    marginBottom: '28px',
  },
  tabButton: {
    padding: '16px',
    border: 'none',
    borderRadius: '12px',
    backgroundColor: 'transparent',
    fontSize: '18px',
    fontWeight: '600',
    cursor: 'pointer',
    color: '#334155',
  },
  activeTab: {
    backgroundColor: '#ffffff',
    color: '#2563eb',
    boxShadow: '0 2px 8px rgba(0,0,0,0.08)',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '18px',
  },
  row: {
    display: 'flex',
    gap: '12px',
    flexWrap: 'wrap',
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    fontWeight: '700',
    fontSize: '16px',
    color: '#1e293b',
  },
  input: {
    padding: '16px',
    border: '1px solid #cbd5e1',
    borderRadius: '14px',
    fontSize: '16px',
    outline: 'none',
    backgroundColor: '#f1f5f9',
    color: '#000000',
  },
  passwordWrapper: {
    display: 'flex',
    alignItems: 'center',
    border: '1px solid #cbd5e1',
    borderRadius: '14px',
  },
  passwordInput: {
    flex: 1,
    padding: '16px',
    border: 'none',
    fontSize: '16px',
    outline: 'none',
    borderRadius: '14px 0 0 14px',
    backgroundColor: '#f1f5f9',
    color: '#000000',
  },
  eyeButton: {
    border: 'none',
    backgroundColor: 'transparent',
    padding: '0 16px',
    cursor: 'pointer',
    fontSize: '20px',
    color: '#64748b',
  },
  smallText: {
    color: '#64748b',
    fontSize: '14px',
  },
  submitButton: {
    marginTop: '8px',
    padding: '16px',
    border: 'none',
    borderRadius: '14px',
    cursor: 'pointer',
    fontSize: '18px',
    fontWeight: '700',
    backgroundColor: '#2563eb',
    color: '#ffffff',
  },
  success: {
    marginTop: '18px',
    color: '#16a34a',
    fontWeight: '600',
    textAlign: 'center',
  },
  error: {
    marginTop: '18px',
    color: '#dc2626',
    fontWeight: '600',
    textAlign: 'center',
  },
  footerText: {
    marginTop: '20px',
    textAlign: 'center',
    color: '#475569',
    fontSize: '15px',
  },
  linkButton: {
  color: '#667eea',
  textDecoration: 'none',
  fontWeight: '600',
  textAlign: 'left',
  display: 'block',
  cursor: 'pointer',
  }

}