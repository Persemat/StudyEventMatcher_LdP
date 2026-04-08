import React, { useState } from 'react';
import { X, Plus, Info, MapPin } from "lucide-react"; // Aggiunto MapPin per l'icona se vuoi usarla

export default function ModalCreateEvent({ onClose, onSuccess }) {
  const [titolo, setTitolo] = useState('');
  const [descrizione, setDescrizione] = useState('');
  const [data, setData] = useState('');
  const [ora, setOra] = useState('');
  const [durata, setDurata] = useState('');
  const [luogo, setLuogo] = useState(''); // Lo stato c'era, mancava l'input!
  const [maxPartecipanti, setMaxPartecipanti] = useState('');
  const [partecipazioneNecessaria, setPartecipazioneNecessaria] = useState(true);
  const [tags, setTags] = useState([]); 
  const [tagInput, setTagInput] = useState('');
  const [messaggio, setMessaggio] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const MAX_TAGS = 3;

  // ... (handleAddTag, handleKeyDown, handleRemoveTag rimangono uguali) ...
  const handleAddTag = (e) => {
    e.preventDefault(); 
    const trimmedTag = tagInput.trim();
    if (trimmedTag && !tags.includes(trimmedTag) && tags.length < MAX_TAGS) {
      setTags([...tags, trimmedTag]);
      setTagInput(''); 
    }
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault(); 
      handleAddTag(e);
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setTags(tags.filter(t => t !== tagToRemove));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (tags.length === 0) {
      setMessaggio("Errore: Inserisci almeno un tag.");
      return;
    }

    setIsLoading(true);
    const dataOraFormattata = `${data}T${ora}:00`;

    const payload = {
      titolo,
      dataOra: dataOraFormattata,
      durata: parseFloat(durata),
      luogo, // Ora verrà inviato correttamente!
      tag: tags,
      descrizione,
      maxPartecipanti: parseInt(maxPartecipanti),
      partecipazioneCreatoreNecessaria: partecipazioneNecessaria 
    };

    try {
      const response = await fetch('/Eventi', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        setMessaggio('Evento creato con successo!');
        if (onSuccess) onSuccess(); 
        setTimeout(() => onClose && onClose(), 2000);
      } else {
        const erroreText = await response.text();
        setMessaggio(`Errore: ${erroreText}`);
      }
    } catch (error) {
      setMessaggio('Errore di connessione al server.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] flex flex-col">
        
        <div className="flex justify-between items-center p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900">Crea Nuovo Evento</h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="overflow-y-auto p-6">
          {messaggio && (
            <div className={`p-4 mb-6 rounded-lg font-medium ${
              messaggio.includes('Errore') ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'
            }`}>
              {messaggio}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            {/* Titolo */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Titolo *</label>
              <input type="text" value={titolo} onChange={(e) => setTitolo(e.target.value)} required className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" placeholder="Es. Sessione di Studio" />
            </div>

            {/* --- LUOGO (REINSERITO QUI) --- */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Luogo *</label>
              <input 
                type="text" 
                value={luogo} 
                onChange={(e) => setLuogo(e.target.value)} 
                required 
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none" 
                placeholder="Es. Aula Magna, Biblioteca, Teams..." 
              />
            </div>

            {/* Descrizione */}
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Descrizione *</label>
              <textarea value={descrizione} onChange={(e) => setDescrizione(e.target.value)} required rows={2} className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none resize-y" placeholder="Cosa studierete?" />
            </div>

            {/* Tag (rimane uguale) */}
            <div className="bg-gray-50 p-4 rounded-lg border border-gray-100">
              <div className="flex justify-between items-center mb-2">
                <label className="block text-sm font-bold text-gray-700">Tag dell'evento *</label>
                <span className={`text-xs font-bold ${tags.length === MAX_TAGS ? 'text-orange-500' : 'text-gray-500'}`}>{tags.length} / {MAX_TAGS}</span>
              </div>
              <div className="flex gap-2 mb-3">
                <input type="text" value={tagInput} onChange={(e) => setTagInput(e.target.value)} onKeyDown={handleKeyDown} disabled={tags.length >= MAX_TAGS} className="flex-1 px-4 py-2 border border-gray-300 rounded-lg outline-none bg-white disabled:bg-gray-100" placeholder={tags.length >= MAX_TAGS ? "Limite raggiunto" : "Aggiungi tag..."} />
                <button type="button" onClick={handleAddTag} disabled={!tagInput.trim() || tags.length >= MAX_TAGS} className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 disabled:opacity-50"><Plus size={20} /></button>
              </div>
              <div className="flex flex-wrap gap-2">
                {tags.map((t, index) => (
                  <span key={index} className="inline-flex items-center gap-1 px-3 py-1 bg-blue-600 text-white text-sm font-semibold rounded-full">
                    {t}<button type="button" onClick={() => handleRemoveTag(t)}><X size={14} /></button>
                  </span>
                ))}
              </div>
            </div>

            {/* Data e Ora */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Data *</label>
                <input type="date" value={data} onChange={(e) => setData(e.target.value)} required min={new Date().toISOString().split("T")[0]} className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Ora *</label>
                <input type="time" value={ora} onChange={(e) => setOra(e.target.value)} required className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none" />
              </div>
            </div>

            {/* Durata e Partecipanti */}
            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Durata (h) *</label>
                <input type="number" step="0.5" value={durata} onChange={(e) => setDurata(e.target.value)} required className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Max Persone *</label>
                <input type="number" value={maxPartecipanti} onChange={(e) => setMaxPartecipanti(e.target.value)} required className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none" />
              </div>
            </div>

            {/* Flag Partecipazione (rimane uguale) */}
            <div className="flex items-center gap-3 p-4 bg-blue-50/50 rounded-lg border border-blue-100">
              <input 
                type="checkbox" 
                id="creatorRequired"
                checked={partecipazioneNecessaria}
                onChange={(e) => setPartecipazioneNecessaria(e.target.checked)}
                className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
              />
              <div className="flex-1">
                <label htmlFor="creatorRequired" className="text-sm font-bold text-gray-800 cursor-pointer flex items-center gap-2">
                  La mia presenza è obbligatoria
                  <Info size={14} className="text-blue-400" />
                </label>
                <p className="text-xs text-gray-500 mt-0.5">
                  Se attivo, l'evento verrà annullato se deciderai di ritirarti.
                </p>
              </div>
            </div>

            <div className="flex gap-3 pt-4 border-t border-gray-100">
              <button type="button" onClick={onClose} className="flex-1 px-4 py-2 border border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-50">Annulla</button>
              <button type="submit" disabled={isLoading} className={`flex-1 px-4 py-2 text-white rounded-lg font-bold ${isLoading ? 'bg-blue-400' : 'bg-blue-600 hover:bg-blue-700'}`}>
                {isLoading ? 'Creazione...' : 'Crea Evento'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}