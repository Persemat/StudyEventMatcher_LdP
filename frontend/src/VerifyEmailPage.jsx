import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';

export default function VerifyEmailPage() {
  const navigate = useNavigate();
  const location = useLocation();
  
  const email = location.state?.email || '';

  const [code, setCode] = useState('');
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  
  const [resendLoading, setResendLoading] = useState(false);
  const [resendMessage, setResendMessage] = useState('');

  async function handleVerify(e) {
    e.preventDefault();
    setError('');
    setSuccess('');
    setResendMessage('');

    if (code.length !== 6 || !/^\d+$/.test(code)) {
      setError('Il codice deve essere composto da 6 cifre numeriche.');
      return;
    }

    try {
      const response = await fetch(`/api/user/verify?token=${code}`, {
        method: 'GET'
      });

      const data = await response.json();

      if (!response.ok) {
        throw new Error(data.message || "Impossibile verificare l'account.");
}

      setSuccess(data.message);
      
      setTimeout(() => {
        navigate("/login");
      }, 2500);

    } catch (err) {
      setError(err.message);
    }
  }

  async function handleResendCode() {
    if (!email) {
      setError("Impossibile reinviare il codice: indirizzo email mancante. Torna alla pagina di registrazione.");
      return;
    }

    setResendLoading(true);
    setResendMessage('');
    setError('');

    try {
      const response = await fetch(`/api/user/resend-verification?email=${encodeURIComponent(email)}`, {
        method: 'POST'
      });

      const messageText = await response.text();

      if (!response.ok) {
        throw new Error(messageText || "Impossibile inviare un nuovo codice.");
      }

      setResendMessage("Nuovo codice inviato con successo! Controlla la tua casella di posta.");
    } catch (err) {
      setError(err.message);
    } finally {
      setResendLoading(false);
    }
  }

  return (
    <main style={styles.container}>
      <section style={styles.wrapper}>
        <div style={styles.logoCircle}>✉️</div>
        <h1 style={styles.title}>Conferma Account</h1>
        <p style={styles.subtitle}>
          Inserisci il codice a 6 cifre inviato a: <br />
          <strong>{email || "la tua email"}</strong>
        </p>

        <div style={styles.card}>
          <form onSubmit={handleVerify} style={styles.form}>
            <div style={styles.field}>
              <label style={styles.label}>Codice di verifica</label>
              <input
                type="text"
                maxLength={6}
                value={code}
                onChange={(e) => setCode(e.target.value.replace(/\D/g, ''))}
                placeholder="123456"
                required
                style={styles.codeInput}
              />
            </div>

            <button type="submit" style={styles.submitButton}>
              Verifica Account
            </button>
          </form>

          {success && <p style={styles.success}>{success}</p>}
          {resendMessage && <p style={styles.resendSuccess}>{resendMessage}</p>}
          {error && <p style={styles.error}>{error}</p>}

          <div style={styles.resendContainer}>
            <span style={styles.resendText}>Non hai ricevuto l'email? </span>
            <button 
              type="button" 
              onClick={handleResendCode} 
              disabled={resendLoading}
              style={{
                ...styles.resendButton,
                cursor: resendLoading ? 'not-allowed' : 'pointer',
                opacity: resendLoading ? 0.6 : 1
              }}
            >
              {resendLoading ? 'Invio in corso...' : 'Clicca qui per riceverne uno nuovo!'}
            </button>
          </div>
        </div>
      </section>
    </main>
  );
}

const styles = {
  
  container: { minHeight: '100vh', display: 'flex', justifyContent: 'center', alignItems: 'center', backgroundColor: '#eef3ff', fontFamily: 'Arial, sans-serif', padding: '24px' },
  wrapper: { width: '100%', maxWidth: '450px', display: 'flex', flexDirection: 'column', alignItems: 'center' },
  logoCircle: { width: '72px', height: '72px', borderRadius: '50%', backgroundColor: '#10b981', display: 'flex', justifyContent: 'center', alignItems: 'center', fontSize: '30px', marginBottom: '20px' },
  title: { fontSize: '36px', fontWeight: '800', color: '#0f172a', marginBottom: '10px', textAlign: 'center' },
  subtitle: { fontSize: '16px', color: '#334155', marginBottom: '32px', textAlign: 'center', lineHeight: '1.5' },
  card: { width: '100%', backgroundColor: '#ffffff', padding: '28px', borderRadius: '22px', boxShadow: '0 10px 30px rgba(0,0,0,0.08)', boxSizing: 'border-box' },
  form: { display: 'flex', flexDirection: 'column', gap: '18px' },
  field: { display: 'flex', flexDirection: 'column', gap: '8px' },
  label: { fontWeight: '700', fontSize: '16px', color: '#1e293b', textAlign: 'center' },
  codeInput: { padding: '16px', border: '1px solid #cbd5e1', borderRadius: '14px', fontSize: '28px', letterSpacing: '8px', textAlign: 'center', outline: 'none', backgroundColor: '#f1f5f9', color: '#000000', fontWeight: 'bold' },
  submitButton: { padding: '16px', border: 'none', borderRadius: '14px', cursor: 'pointer', fontSize: '18px', fontWeight: '700', backgroundColor: '#10b981', color: '#ffffff' },
  success: { marginTop: '18px', color: '#16a34a', fontWeight: '600', textAlign: 'center' },
  error: { marginTop: '18px', color: '#dc2626', fontWeight: '600', textAlign: 'center' },
  
  
  resendSuccess: {
    marginTop: '18px',
    color: '#2563eb',
    fontWeight: '600',
    textAlign: 'center',
  },
  resendContainer: {
    marginTop: '24px',
    textAlign: 'center',
    fontSize: '14px',
    borderTop: '1px solid #f1f5f9',
    paddingTop: '16px'
  },
  resendText: {
    color: '#64748b'
  },
  resendButton: {
    background: 'none',
    border: 'none',
    color: '#2563eb',
    fontWeight: 'bold',
    textDecoration: 'underline',
    padding: 0,
    fontFamily: 'inherit',
    fontSize: '14px'
  }
};