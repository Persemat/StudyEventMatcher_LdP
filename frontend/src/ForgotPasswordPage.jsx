// Password recovery page.
import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [resetCode, setResetCode] = useState('')
  const [newPassword, setNewPassword] = useState('')
  const [confirmNewPassword, setConfirmNewPassword] = useState('')

  const [codeSent, setCodeSent] = useState(false)
  const [isSending, setIsSending] = useState(false)

  const [showNewPassword, setShowNewPassword] = useState(false)
  const [showConfirmNewPassword, setShowConfirmNewPassword] = useState(false)

  const [message, setMessage] = useState('')
  const [error, setError] = useState('')

  // Countdown before requesting another code.
  const [remainingSeconds, setRemainingSeconds] = useState(0)

  function resetMessages() {
    setMessage('')
    setError('')
  }

  useEffect(() => {
    if (remainingSeconds <= 0) return

    const timer = setInterval(() => {
      setRemainingSeconds((prev) => {
        if (prev <= 1) {
          clearInterval(timer)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [remainingSeconds])

  function formatTime(seconds) {
    const minutes = String(Math.floor(seconds / 60)).padStart(2, '0')
    const secs = String(seconds % 60).padStart(2, '0')
    return `${minutes}:${secs}`
  }

  async function handleSendCode(event) {
    event.preventDefault()
    resetMessages()

    if (isSending) return

    setIsSending(true)

    try {
      const response = await fetch('/api/password-reset/forgot', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email })
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)
      }

      const data = await response.json()
      setCodeSent(true)
      setMessage(data.message)

      setRemainingSeconds(30)
    } catch (err) {
      console.error(err)
      setError(err.message)
    } finally {
      setIsSending(false)
    }
  }

  async function handleResetPassword(event) {
    event.preventDefault()
    resetMessages()

    if (newPassword !== confirmNewPassword) {
      setError('Le password non corrispondono')
      return
    }

    try {
      const response = await fetch('/api/password-reset/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          email,
          code: resetCode,
          newPassword,
        })
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)
      }

      const data = await response.json()
      setMessage(data.message)

      setEmail('')
      setResetCode('')
      setNewPassword('')
      setConfirmNewPassword('')

      setTimeout(() => {
        navigate('/login')
      }, 1500)
    } catch (err) {
      console.error(err)
      setError(err.message)
    }
  }

  return (
    <main style={styles.container}>
      <section style={styles.wrapper}>
        <div style={styles.logoCircle}>🔐</div>
        <h1 style={styles.title}>Recupera Password</h1>
        <p style={styles.subtitle}>
          Inserisci la tua email per ricevere un codice di recupero password
        </p>

        <div style={styles.card}>
          <form onSubmit={handleSendCode} style={styles.form}>
            <div style={styles.field}>
              <label htmlFor="email" style={styles.label}>Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="mario.rossi@example.com"
                required
                disabled={codeSent}
                style={{
                  ...styles.input,
                  ...(codeSent ? styles.inputDisabled : {})
                }}
              />
            </div>

            {!codeSent && (
              <button
                type="submit"
                style={{
                  ...styles.submitButton,
                  ...(isSending ? styles.submitButtonDisabled : {})
                }}
                disabled={isSending}
              >
                {isSending ? 'Invio...' : 'Invia codice di recupero'}
              </button>
            )}
          </form>

          {codeSent && (
            <form onSubmit={handleResetPassword} style={{ ...styles.form, marginTop: '20px' }}>
              <div style={styles.field}>
                <label htmlFor="resetCode" style={styles.label}>Codice ricevuto via email</label>
                <input
                  id="resetCode"
                  type="text"
                  value={resetCode}
                  onChange={(e) => setResetCode(e.target.value)}
                  placeholder="123456"
                  required
                  style={styles.input}
                />
              </div>

              <div style={styles.countdownBox}>
                {remainingSeconds > 0 ? (
                  <p style={styles.countdownText}>
                    Puoi richiedere un nuovo codice in {formatTime(remainingSeconds)}
                  </p>
                ) : (
                  <button
                    type="button"
                    onClick={handleSendCode}
                    style={styles.linkAction}
                    disabled={isSending}
                  >
                    {isSending ? 'Invio...' : 'Richiedi un nuovo codice'}
                  </button>
                )}
              </div>

              <div style={styles.field}>
                <label htmlFor="newPassword" style={styles.label}>Nuova password</label>
                <div style={styles.passwordWrapper}>
                  <input
                    id="newPassword"
                    type={showNewPassword ? 'text' : 'password'}
                    value={newPassword}
                    onChange={(e) => setNewPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />
                  <button
                    type="button"
                    onClick={() => setShowNewPassword(!showNewPassword)}
                    style={styles.eyeButton}
                  >
                    👁
                  </button>
                </div>
              </div>

              <div style={styles.field}>
                <label htmlFor="confirmNewPassword" style={styles.label}>Conferma la nuova password</label>
                <div style={styles.passwordWrapper}>
                  <input
                    id="confirmNewPassword"
                    type={showConfirmNewPassword ? 'text' : 'password'}
                    value={confirmNewPassword}
                    onChange={(e) => setConfirmNewPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />
                  <button
                    type="button"
                    onClick={() => setShowConfirmNewPassword(!showConfirmNewPassword)}
                    style={styles.eyeButton}
                  >
                    👁
                  </button>
                </div>
              </div>

              <button type="submit" style={styles.submitButton}>
                Aggiorna password
              </button>
            </form>
          )}

          {message && <p style={styles.success}>{message}</p>}
          {error && <p style={styles.error}>{error}</p>}

          <Link to="/login" style={styles.linkButton}>
            Torna al login
          </Link>
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
    background: 'linear-gradient(135deg, #667eea 0%, #764ba2 100%)',
    fontFamily: 'Arial, sans-serif',
    padding: '20px',
  },
  wrapper: {
    width: '100%',
    maxWidth: '500px',
    textAlign: 'center',
  },
  logoCircle: {
    width: '80px',
    height: '80px',
    backgroundColor: 'rgba(255,255,255,0.2)',
    borderRadius: '50%',
    display: 'flex',
    justifyContent: 'center',
    alignItems: 'center',
    fontSize: '36px',
    margin: '0 auto 20px',
    backdropFilter: 'blur(10px)',
  },
  title: {
    color: '#fff',
    fontSize: '36px',
    fontWeight: 'bold',
    marginBottom: '10px',
  },
  subtitle: {
    color: 'rgba(255,255,255,0.9)',
    fontSize: '18px',
    marginBottom: '30px',
  },
  card: {
    backgroundColor: '#fff',
    borderRadius: '20px',
    padding: '40px',
    boxShadow: '0 20px 40px rgba(0,0,0,0.1)',
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '20px',
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    textAlign: 'left',
  },
  label: {
    marginBottom: '8px',
    fontWeight: '600',
    color: '#333',
  },
  input: {
    padding: '14px 16px',
    borderRadius: '12px',
    border: '2px solid #e1e5e9',
    fontSize: '16px',
    outline: 'none',
  },
  inputDisabled: {
    backgroundColor: '#f5f5f5',
    color: '#777',
    cursor: 'not-allowed',
  },
  passwordWrapper: {
    display: 'flex',
    alignItems: 'center',
    border: '2px solid #e1e5e9',
    borderRadius: '12px',
    overflow: 'hidden',
  },
  passwordInput: {
    flex: 1,
    padding: '14px 16px',
    border: 'none',
    fontSize: '16px',
    outline: 'none',
  },
  eyeButton: {
    border: 'none',
    background: 'transparent',
    padding: '0 14px',
    cursor: 'pointer',
    fontSize: '18px',
  },
  submitButton: {
    backgroundColor: '#667eea',
    color: '#fff',
    border: 'none',
    padding: '14px',
    borderRadius: '12px',
    fontSize: '16px',
    fontWeight: 'bold',
    cursor: 'pointer',
  }, 
  submitButtonDisabled: {
    opacity: 0.7,
    cursor: 'not-allowed',
  },
  countdownBox: {
    marginTop: '-5px',
    marginBottom: '5px',
    textAlign: 'left', 
  },
  countdownText: {
    margin: 0,
    fontSize: '14px',
    color: '#555',
    textAlign: 'left',
  },
  linkButton: {
    display: 'inline-block',
    marginTop: '20px',
    color: '#667eea',
    textDecoration: 'none',
    fontWeight: '600',
    textAlign: 'center',
  },
  success: {
    marginTop: '20px',
    color: 'green',
    fontWeight: '600',
  },
  error: {
    marginTop: '20px',
    color: 'red',
    fontWeight: '600',
  },
  linkAction: {
  border: 'none',
  background: 'transparent',
  padding: 0,
  margin: 0,
  color: '#667eea',
  fontSize: '14px',
  fontWeight: '600',
  // textDecoration: 'underline',
  cursor: 'pointer',
  textAlign: 'left',
},
}


