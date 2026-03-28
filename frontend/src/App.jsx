import { useEffect, useState } from 'react'

function App() {
  const [utenti, setUtenti] = useState([])
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [messaggio, setMessaggio] = useState('')

  async function caricaUtenti() {
    try {
      const response = await fetch('/Utenti')
      if (!response.ok) {
        throw new Error('Errore nel recupero utenti')
      }
      const data = await response.json()
      setUtenti(data)
    } catch (error) {
      console.error(error)
      setMessaggio('Errore nel caricamento utenti')
    }
  }

  useEffect(() => {
    caricaUtenti()
  }, [])

  async function handleSubmit(e) {
    e.preventDefault()
    setMessaggio('')

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
        throw new Error('Errore nella creazione utente')
      }

      setEmail('')
      setPassword('')
      setMessaggio('Utente creato correttamente')
      await caricaUtenti()
    } catch (error) {
      console.error(error)
      setMessaggio('Errore nella creazione utente')
    }
  }

  return (
    <div style={{ padding: '2rem', fontFamily: 'Arial, sans-serif' }}>
      <h1>Gestione utenti</h1>

      <form onSubmit={handleSubmit} style={{ marginBottom: '2rem' }}>
        <div style={{ marginBottom: '1rem' }}>
          <label>Email</label>
          <br />
          <input
            type="email"
            value={email}
            onChange={(e) => setEmail(e.target.value)}
            required
            style={{ padding: '0.5rem', width: '260px' }}
          />
        </div>

        <div style={{ marginBottom: '1rem' }}>
          <label>Password</label>
          <br />
          <input
            type="password"
            value={password}
            onChange={(e) => setPassword(e.target.value)}
            required
            style={{ padding: '0.5rem', width: '260px' }}
          />
        </div>

        <button type="submit" style={{ padding: '0.6rem 1rem' }}>
          Crea utente
        </button>
      </form>

      {messaggio && <p>{messaggio}</p>}

      <h2>Utenti</h2>

      {utenti.length === 0 ? (
        <p>Nessun utente trovato</p>
      ) : (
        <ul>
          {utenti.map((utente) => (
            <li key={utente.id}>
              {utente.id} - {utente.email}
            </li>
          ))}
        </ul>
      )}
    </div>
  )
}

export default App