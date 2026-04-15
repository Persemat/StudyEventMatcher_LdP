import { useEffect, useState } from 'react'
import { Link, useNavigate } from 'react-router-dom'

export default function ForgotPasswordPage() {
  const navigate = useNavigate()

  const [email, setEmail] = useState('')
  const [codiceReset, setCodiceReset] = useState('')
  const [nuovaPassword, setNuovaPassword] = useState('')
  const [confermaNuovaPassword, setConfermaNuovaPassword] = useState('')

  const [codiceInviato, setCodiceInviato] = useState(false)
  const [invioInCorso, setInvioInCorso] = useState(false)

  const [mostraNuovaPassword, setMostraNuovaPassword] = useState(false)
  const [mostraConfermaNuovaPassword, setMostraConfermaNuovaPassword] = useState(false)

  const [messaggio, setMessaggio] = useState('')
  const [errore, setErrore] = useState('')

  // Countdown per richiedere un nuovo codice
  const [secondiRimanenti, setSecondiRimanenti] = useState(0)

  function resetMessaggi() {
    setMessaggio('')
    setErrore('')
  }

  useEffect(() => {
    if (secondiRimanenti <= 0) {
      return
    }

    const timer = setInterval(() => {
      setSecondiRimanenti((prev) => {
        if (prev <= 1) {
          clearInterval(timer)
          return 0
        }
        return prev - 1
      })
    }, 1000)

    return () => clearInterval(timer)
  }, [secondiRimanenti])

  function formattaTempo(secondi) {
    const minuti = String(Math.floor(secondi / 60)).padStart(2, '0')
    const sec = String(secondi % 60).padStart(2, '0')
    return `${minuti}:${sec}`
  }

  async function handleInviaCodice(event) {
    event.preventDefault()
    resetMessaggi()

    if (invioInCorso) {
      return
    }

    setInvioInCorso(true)

    try {
      const response = await fetch('/password-reset/forgot', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        // Inviamo solo la mail per far generare il codice al backend
        body: JSON.stringify({ email })
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)
      }

      const data = await response.json()
      setCodiceInviato(true)
      setMessaggio(data.message)

      // Ad ogni nuovo invio riparte il countdown
      setSecondiRimanenti(30)
    } catch (error) {
      console.error(error)
      setErrore(error.message)
    } finally {
      setInvioInCorso(false)
    }
  }

  async function handleResetPassword(event) {
    event.preventDefault()
    resetMessaggi()

    if (nuovaPassword !== confermaNuovaPassword) {
      setErrore('Le nuove password non coincidono')
      return
    }

    try {
      const response = await fetch('/password-reset/confirm', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        // Qui inviamo i dati necessari per completare il cambio password
        body: JSON.stringify({
          email,
          codice: codiceReset,
          nuovaPassword,
        })
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)
      }

      const data = await response.json()
      setMessaggio(data.message)

      setEmail('')
      setCodiceReset('')
      setNuovaPassword('')
      setConfermaNuovaPassword('')

      // Dopo il reset torniamo alla login
      setTimeout(() => {
        navigate('/login')
      }, 1500)
    } catch (error) {
      console.error(error)
      setErrore(error.message)
    }
  }

  return (
    <main style={styles.container}>
      <section style={styles.wrapper}>
        <div style={styles.logoCircle}>🔐</div>
        <h1 style={styles.title}>Recupero password</h1>
        <p style={styles.subtitle}>
          Inserisci la tua email per ricevere un codice di recupero
        </p>

        <div style={styles.card}>
          <form onSubmit={handleInviaCodice} style={styles.form}>
            <div style={styles.field}>
              <label htmlFor="email" style={styles.label}>Email</label>
              <input
                id="email"
                type="email"
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                placeholder="mario.rossi@example.com"
                required
                disabled={codiceInviato}
                style={{
                  ...styles.input,
                  ...(codiceInviato ? styles.inputDisabled : {})
                }}
              />
            </div>

            {!codiceInviato && (
              <button
                type="submit"
                style={{
                  ...styles.submitButton,
                  ...(invioInCorso ? styles.submitButtonDisabled : {})
                }}
                disabled={invioInCorso}
              >
                {invioInCorso ? 'Invio in corso...' : 'Invia codice di recupero'}
              </button>
            )}
          </form>

          {codiceInviato && (
            <form onSubmit={handleResetPassword} style={{ ...styles.form, marginTop: '20px' }}>
              <div style={styles.field}>
                <label htmlFor="codiceReset" style={styles.label}>Codice ricevuto via email</label>
                <input
                  id="codiceReset"
                  type="text"
                  value={codiceReset}
                  onChange={(e) => setCodiceReset(e.target.value)}
                  placeholder="123456"
                  required
                  style={styles.input}
                />
              </div>

              {/* Countdown per nuovo invio */}
              <div style={styles.countdownBox}>
                {secondiRimanenti > 0 ? (
                <p style={styles.countdownText}>
                  Potrai richiedere un nuovo codice tra {formattaTempo(secondiRimanenti)}
                </p>
              ) : (
                <button
                  type="button"
                  onClick={handleInviaCodice}
                  style={styles.linkAction}
                  disabled={invioInCorso}
                >
                  {invioInCorso ? 'Invio in corso...' : 'Richiedi un nuovo codice'}
                </button>
              )}
              </div>

              <div style={styles.field}>
                <label htmlFor="nuovaPassword" style={styles.label}>Nuova password</label>
                <div style={styles.passwordWrapper}>
                  <input
                    id="nuovaPassword"
                    type={mostraNuovaPassword ? 'text' : 'password'}
                    value={nuovaPassword}
                    onChange={(e) => setNuovaPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />
                  <button
                    type="button"
                    onClick={() => setMostraNuovaPassword(!mostraNuovaPassword)}
                    style={styles.eyeButton}
                  >
                    👁
                  </button>
                </div>
              </div>

              <div style={styles.field}>
                <label htmlFor="confermaNuovaPassword" style={styles.label}>Conferma nuova password</label>
                <div style={styles.passwordWrapper}>
                  <input
                    id="confermaNuovaPassword"
                    type={mostraConfermaNuovaPassword ? 'text' : 'password'}
                    value={confermaNuovaPassword}
                    onChange={(e) => setConfermaNuovaPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />
                  <button
                    type="button"
                    onClick={() => setMostraConfermaNuovaPassword(!mostraConfermaNuovaPassword)}
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

          {messaggio && <p style={styles.success}>{messaggio}</p>}
          {errore && <p style={styles.error}>{errore}</p>}

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
  //textDecoration: 'underline',
  cursor: 'pointer',
  textAlign: 'left',
},
}