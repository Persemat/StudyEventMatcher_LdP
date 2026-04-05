import { useState } from 'react'
import { useNavigate } from "react-router-dom";

export default function AuthPage() {
  const [isLogin, setIsLogin] = useState(true)
  const navigate = useNavigate();
  const [nome, setNome] = useState('')
  const [cognome, setCognome] = useState('')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [confermaPassword, setConfermaPassword] = useState('')
  const [mostraPassword, setMostraPassword] = useState(false)
  const [mostraConfermaPassword, setMostraConfermaPassword] = useState(false)

  const [messaggio, setMessaggio] = useState('')
  const [errore, setErrore] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setMessaggio('')
    setErrore('')

    if (!isLogin && password !== confermaPassword) {
      setErrore('Le password non coincidono')
      return
    }

    try {
      const response = await fetch(isLogin ? '/Utenti/login' : '/Utenti', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify(
          isLogin
            ? { email, password }
            : { nome, cognome, email, password }
        ),
        credentials: 'include'
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)
      }

      const data = await response.json()

      if (isLogin) {
        setMessaggio(`Accesso effettuato per: ${data.email}`);
        
        // Aspetta 1 secondo e poi vai alla Dashboard
        setTimeout(() => {
          navigate("/dashboard"); // Assicurati che la rotta in App.js si chiami così
        }, 1000);

      } else {
        // Per la registrazione, di solito è meglio mandarlo al login o loggarlo subito
        setMessaggio(
          `Registrazione completata per: ${data.nome ?? ''} ${data.cognome ?? ''}`
        );
        
        // Opzionale: dopo la registrazione lo mandiamo al login dopo 2 secondi
        setTimeout(() => {
          navigate("/login");
        }, 2000);
      }

      setNome('')
      setCognome('')
      setEmail('')
      setPassword('')
      setConfermaPassword('')
    } catch (error) {
      console.error(error)
      setErrore(error.message)
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
            ? 'Benvenuto! Accedi al tuo account'
            : 'Crea il tuo account e inizia a studiare insieme'}
        </p>

        <div style={styles.card}>
          <div style={styles.tabs}>
            <button
              type="button"
              onClick={() => {
                setIsLogin(true)
                setMessaggio('')
                setErrore('')
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
                setMessaggio('')
                setErrore('')
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
                  <label htmlFor="nome" style={styles.label}>
                    Nome
                  </label>
                  <input
                    id="nome"
                    type="text"
                    value={nome}
                    onChange={(e) => setNome(e.target.value)}
                    placeholder="Mario"
                    required
                    style={styles.input}
                  />
                </div>

                <div style={{ ...styles.field, flex: 1 }}>
                  <label htmlFor="cognome" style={styles.label}>
                    Cognome
                  </label>
                  <input
                    id="cognome"
                    type="text"
                    value={cognome}
                    onChange={(e) => setCognome(e.target.value)}
                    placeholder="Rossi"
                    required
                    style={styles.input}
                  />
                </div>
              </div>
            )}

            <div style={styles.field}>
              <label htmlFor="email" style={styles.label}>
                Email
              </label>
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
              <label htmlFor="password" style={styles.label}>
                Password
              </label>

              <div style={styles.passwordWrapper}>
                <input
                  id="password"
                  type={mostraPassword ? 'text' : 'password'}
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  placeholder="••••••••"
                  required
                  style={styles.passwordInput}
                />
                <button
                  type="button"
                  onClick={() => setMostraPassword(!mostraPassword)}
                  style={styles.eyeButton}
                >
                  👁
                </button>
              </div>

              
            </div>

            {!isLogin && (
              <div style={styles.field}>
                <label htmlFor="confermaPassword" style={styles.label}>
                  Conferma Password
                </label>

                <div style={styles.passwordWrapper}>
                  <input
                    id="confermaPassword"
                    type={mostraConfermaPassword ? 'text' : 'password'}
                    value={confermaPassword}
                    onChange={(e) => setConfermaPassword(e.target.value)}
                    placeholder="••••••••"
                    required
                    style={styles.passwordInput}
                  />
                  <button
                    type="button"
                    onClick={() =>
                      setMostraConfermaPassword(!mostraConfermaPassword)
                    }
                    style={styles.eyeButton}
                  >
                    👁
                  </button>
                </div>
              </div>
            )}

            <button type="submit" style={styles.submitButton}>
              {isLogin ? 'Accedi' : 'Crea Account'}
            </button>
          </form>

          {messaggio && <p style={styles.success}>{messaggio}</p>}
          {errore && <p style={styles.error}>{errore}</p>}

          {isLogin && (
            <p style={styles.footerText}>
              Inserisci le credenziali del tuo account per accedere
            </p>
          )}
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
}