import { useState } from 'react';

export default function CreaEvento() {
  // Definiamo gli stati per i campi del form
  const [titolo, setTitolo] = useState('');
  const [materia, setMateria] = useState(''); // Questo lo useremo come Tag
  const [descrizione, setDescrizione] = useState('');
  const [data, setData] = useState('');
  const [ora, setOra] = useState('');
  const [durata, setDurata] = useState('');
  const [luogo, setLuogo] = useState('');
  const [maxPartecipanti, setMaxPartecipanti] = useState('');
  const [messaggio, setMessaggio] = useState('');

  //La funzione che scatta quando premi "Crea Evento"
  const handleSubmit = async (e) => {
    e.preventDefault();

    // Uniamo Data e Ora nel formato ISO atteso dal backend (LocalDateTime)
    // Es: "2026-04-10" + "T" + "15:00" + ":00" -> "2026-04-10T15:00:00"
    const dataOraFormattata = `${data}T${ora}:00`;

    // Creiamo il "pacchetto" da spedire, identico al nostro EventoRequest (DTO)
    const payload = {
      titolo: titolo,
      dataOra: dataOraFormattata,
      durata: parseFloat(durata),
      luogo: luogo,
      tag: [materia], // Mettiamo la materia in un array come si aspetta il backend
      descrizione: descrizione,
      maxPartecipanti: parseInt(maxPartecipanti),
      partecipazioneCreatoreNecessaria: true, // Per ora lo forziamo a true
      creatoreId: creatoreId 
        };

    try {
      // chiamata al Backend 
      const response = await fetch('/eventi', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        setMessaggio('Evento creato con successo!');
        setTimeout(() => onClose && onClose(), 2000); //chiude il form dopo due secondi
        // Qua si può implementare la logica per svuotare il form o reindirizzare l'utente
      } else {
        const erroreText = await response.text();
        setMessaggio(`Errore: ${erroreText}`);
      }
    } catch (error) {
      setMessaggio('Errore di connessione al server.');
    }
  };

  // Stile grafico condiviso per tutti gli input del form
  const inputStyle = {
    width: '100%', padding: '10px 12px', fontSize: '14px', 
    border: '1px solid #ced4da', borderRadius: '50px', 
    outline: 'none', boxSizing: 'border-box', marginTop: '4px',
    fontFamily: 'inherit'
  };

  const labelStyle = {
    display: 'block', fontSize: '14px', fontWeight: '500', color: '#343a40'
  };

  // Interfaccia
 return (
    // Lo sfondo scuro semitrasparente (overlay)
    <div style={{
      position: 'fixed', top: 0, left: 0, width: '100vw', height: '100vh',
      backgroundColor: 'rgba(0, 0, 0, 0.4)', display: 'flex', 
      justifyContent: 'center', alignItems: 'center', zIndex: 1000,
      fontFamily: '"Inter", "Segoe UI", sans-serif'
    }}>
      
      <div style={{
        backgroundColor: '#ffffff', width: '100%', maxWidth: '500px',
        borderRadius: '12px', boxShadow: '0 10px 25px rgba(0,0,0,0.1)',
        display: 'flex', flexDirection: 'column', maxHeight: '90vh'
      }}>
        
        {/* Intestazione (Titolo e tasto X) */}
        <div style={{
          display: 'flex', justifyContent: 'space-between', alignItems: 'center',
          padding: '20px 24px', borderBottom: '1px solid #e9ecef'
        }}>
          <h2 style={{ margin: 0, fontSize: '20px', color: '#0f172a' }}>Crea Nuovo Evento</h2>
          <button onClick={onClose} style={{
            background: 'none', border: 'none', fontSize: '20px', cursor: 'pointer', color: '#6c757d'
          }}>✖</button>
        </div>

        {/*Corpo del form (Con barra di scorrimento se è troppo lungo) */}
        <div style={{ padding: '24px', overflowY: 'auto' }}>
          {messaggio && <div style={{ padding: '10px', background: '#d4edda', color: '#155724', borderRadius: '6px', marginBottom: '20px' }}>{messaggio}</div>}

          <form onSubmit={handleSubmit} style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
            
            {/* Singolo Campo: Titolo */}
            <div>
              <label style={labelStyle}>Titolo *</label>
              <input type="text" value={titolo} onChange={(e) => setTitolo(e.target.value)} required style={inputStyle} placeholder="Es. Sessione di Studio - Matematica"/>
            </div>

            <div>
              <label style={labelStyle}>Materia *</label>
              <select value={materia} onChange={(e) => setMateria(e.target.value)} required style={inputStyle}>
                <option value="">Seleziona una materia</option>
                <option value="Matematica">Matematica</option>
                <option value="Informatica">Informatica</option>
                <option value="Basi di Dati">Basi di Dati</option>
              </select>
            </div>

            <div>
              <label style={labelStyle}>Descrizione *</label>
              <textarea value={descrizione} onChange={(e) => setDescrizione(e.target.value)} required style={{...inputStyle, minHeight: '100px', resize: 'vertical'}} placeholder="Descrivi l'evento e cosa verrà studiato..." />
            </div>

            {/* Campi affiancati per Data e Ora (flexbox) */}
            <div style={{ display: 'flex', gap: '16px' }}>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>Data *</label>
                <input type="date" value={data} onChange={(e) => setData(e.target.value)} required style={inputStyle} />
              </div>
              <div style={{ flex: 1 }}>
                <label style={labelStyle}>Ora *</label>
                <input type="time" value={ora} onChange={(e) => setOra(e.target.value)} required style={inputStyle} />
              </div>
            </div>

            <div>
              <label style={labelStyle}>Durata (in ore) *</label>
              <input type="number" step="0.5" min="0.5" value={durata} onChange={(e) => setDurata(e.target.value)} required style={inputStyle} placeholder="Es. 2, 3.5" />
            </div>

            <div>
              <label style={labelStyle}>Luogo *</label>
              <input type="text" value={luogo} onChange={(e) => setLuogo(e.target.value)} required style={inputStyle} placeholder="Es. Biblioteca Centrale, Sala 3" />
            </div>

            <div>
              <label style={labelStyle}>Numero massimo di partecipanti *</label>
              <input type="number" min="1" value={maxPartecipanti} onChange={(e) => setMaxPartecipanti(e.target.value)} required style={inputStyle} placeholder="Es. 5"/>
            </div>

            {/* Bottoni finali */}
            <div style={{ display: 'flex', gap: '12px', marginTop: '8px' }}>
              <button type="button" onClick={onClose} style={{
                flex: 1, padding: '12px', background: 'white', color: '#495057',
                border: '1px solid #ced4da', borderRadius: '8px', fontWeight: '600', cursor: 'pointer'
              }}>Annulla</button>
              
              <button type="submit" style={{
                flex: 1, padding: '12px', background: '#0d6efd', color: 'white',
                border: 'none', borderRadius: '8px', fontWeight: '600', cursor: 'pointer'
              }}>Crea Evento</button>
            </div>

          </form>
        </div>
      </div>
    </div>
  );
}
