import { useState } from 'react'

export default function Registrazione() {
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [messaggio, setMessaggio] = useState('')
  const [errore, setErrore] = useState('')

  async function handleSubmit(event) {
    event.preventDefault()
    setMessaggio('')
    setErrore('')

    try {
      const response = await fetch('/Utenti', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          email,
          password,
        }),
      })

      if (!response.ok) {
        const errorText = await response.text()
        throw new Error(errorText)      }

      const data = await response.json()

      setMessaggio(`Registrazione completata per: ${data.email}`)
      setEmail('')
      setPassword('')
    } catch (error) {
      console.error(error)
      setErrore(error.message)
    }
  }

  return (
    <main style={styles.container}>
      <section style={styles.card}>
        <h1 style={{ ...styles.title, color: '#000000' }}>Registrazione</h1>
        <form onSubmit={handleSubmit} style={styles.form}>
          <div style={styles.field}>
            <label htmlFor="email" style={styles.label}>Email</label>
            <input
              id="email"
              type="email"
              value={email}
              onChange={(event) => setEmail(event.target.value)}
              required
              style={styles.input}
            />
          </div>

          <div style={styles.field}>
            <label htmlFor="password" style={styles.label}>Password</label>
            <input
              id="password"
              type="password"
              value={password}
              onChange={(event) => setPassword(event.target.value)}
              required
              style={styles.input}
            />
          </div>

          <button type="submit" style={styles.button}>
            Registrati
          </button>
        </form>

        {messaggio && <p style={styles.success}>{messaggio}</p>}
        {errore && <p style={styles.error}>{errore}</p>}
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
    backgroundColor: '#f5f5f5',
    fontFamily: 'Arial, sans-serif',
    padding: '24px',
  },
  card: {
    width: '100%',
    maxWidth: '420px',
    backgroundColor: '#ffffff',
    padding: '32px',
    borderRadius: '12px',
    boxShadow: '0 8px 24px rgba(0,0,0,0.08)',
  },
  title: {
    marginBottom: '24px',
    textAlign: 'center',
    color: 'var(--text-h)'
  },
  form: {
    display: 'flex',
    flexDirection: 'column',
    gap: '16px',
  },
  field: {
    display: 'flex',
    flexDirection: 'column',
    gap: '8px',
  },
  label: {
    fontWeight: 'bold',
  },
  input: {
    padding: '10px 12px',
    border: '1px solid #ccc',
    borderRadius: '8px',
    fontSize: '14px',
  },
  button: {
    marginTop: '8px',
    padding: '12px',
    border: 'none',
    borderRadius: '8px',
    cursor: 'pointer',
    fontSize: '15px',
  },
  success: {
    marginTop: '16px',
  },
  error: {
    marginTop: '16px',
  },
}