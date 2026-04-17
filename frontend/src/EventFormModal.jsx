import React, { useState, useEffect, useRef } from 'react';
import { X, Plus, Info } from "lucide-react";

export default function EventFormModal({ mode, event, onClose, onSuccess, availableTags = [] }) {
  const isEdit = mode === "edit";

  const initialDate = event?.dataOra ? event.dataOra.slice(0, 10) : "";
  const initialTime = event?.dataOra ? event.dataOra.slice(11, 16) : "";
  const [titolo, setTitolo] = useState(event?.titolo || '');
  const [descrizione, setDescrizione] = useState(event?.descrizione || '');
  const [data, setData] = useState(initialDate);
  const [ora, setOra] = useState(initialTime);
  const [durata, setDurata] = useState(event?.durata || '');
  
  // NUOVI STATI PER IL LUOGO SCISSO
  const [citta, setCitta] = useState("");
  const [indirizzo, setIndirizzo] = useState("");
  const [formError, setFormError] = useState("");
  
  const [maxPartecipanti, setMaxPartecipanti] = useState(event?.maxPartecipanti || '');
  const [partecipazioneNecessaria, setPartecipazioneNecessaria] = useState(
    event?.partecipazioneCreatoreNecessaria ?? true
  );
  
  const [tags, setTags] = useState(event?.tag || []);
  const [tagInput, setTagInput] = useState('');
  
  const [isInputFocused, setIsInputFocused] = useState(false);
  const [messaggio, setMessaggio] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const tagInputRef = useRef(null);
  const MAX_TAGS = 3;

  // PARSING DEL LUOGO IN FASE DI MODIFICA
  useEffect(() => {
    if (isEdit && event) {
      if (event.luogo && event.luogo.includes("-")) {
        // Divide dal primo trattino in poi
        const [c, ...rest] = event.luogo.split("-");
        setCitta(c.trim());
        setIndirizzo(rest.join("-").trim()); 
      } else {
        // Se è un vecchio evento senza trattino, lo consideriamo come "Città" (obbligatoria)
        setCitta(event.luogo || "");
        setIndirizzo("");
      }
    }
  }, [isEdit, event]);

  const addSpecificTag = (tagToAdd) => {
    const trimmedTag = tagToAdd.trim();
    if (trimmedTag && !tags.includes(trimmedTag) && tags.length < MAX_TAGS) {
      setTags([...tags, trimmedTag]);
      setTagInput('');
      setIsInputFocused(false); 
      
      if (tagInputRef.current) {
        tagInputRef.current.setCustomValidity("");
      }
    }
  };

  const handleAddTag = (e) => {
    e.preventDefault();
    addSpecificTag(tagInput);
  };

  const handleKeyDown = (e) => {
    if (e.key === 'Enter') {
      e.preventDefault();
      addSpecificTag(tagInput);
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setTags(tags.filter(t => t !== tagToRemove));
  };

  const suggestions = availableTags.filter(
    (t) => 
      t.toLowerCase().includes(tagInput.toLowerCase()) && 
      !tags.includes(t)
  );

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError(""); // Reset errore

    // VALIDAZIONE CITTÀ (Sicurezza Extra)
    if (!citta.trim()) {
      setFormError("Attenzione: Il campo Città è obbligatorio.");
      return; 
    }

    if (tags.length === 0) {
        tagInputRef.current.setCustomValidity("Compila questo campo.");
        tagInputRef.current.reportValidity();
        return;
    } else {
        tagInputRef.current.setCustomValidity("");
    }

    setIsLoading(true);
    setMessaggio("");

    const dataOraFormattata = `${data}T${ora}:00`;
    
    // UNIONE DELLE STRINGHE PER IL DB
    const luogoFinale = indirizzo.trim() 
        ? `${citta.trim()}-${indirizzo.trim()}` 
        : citta.trim();

    const payload = {
      titolo,
      dataOra: dataOraFormattata,
      durata: parseFloat(durata),
      luogo: luogoFinale, // Spediamo la stringa unita!
      tag: tags,
      descrizione,
      maxPartecipanti: parseInt(maxPartecipanti),
      partecipazioneCreatoreNecessaria: partecipazioneNecessaria
    };

    const url = isEdit ? `/Eventi/${event.id}` : `/Eventi`;
    const method = isEdit ? "PUT" : "POST";

    try {
      const response = await fetch(url, {
        method,
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify(payload)
      });

      if (response.ok) {
        if (onSuccess) onSuccess();
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
          <h2 className="text-2xl font-bold text-gray-900">
            {isEdit ? "Modifica Evento" : "Crea Nuovo Evento"}
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600 transition-colors">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="overflow-y-auto p-6">
          {messaggio && (
            <div className={`p-4 mb-4 rounded-lg font-medium ${
              messaggio.includes('Errore') ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'
            }`}>
              {messaggio}
            </div>
          )}

          {/* Errore personalizzato di validazione form */}
          {formError && (
            <div className="p-4 mb-4 rounded-lg font-medium bg-red-50 text-red-700 border border-red-200">
              {formError}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Titolo *</label>
              <input
                type="text"
                value={titolo}
                onChange={(e) => setTitolo(e.target.value)}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
              />
            </div>

            {/* SEZIONE LUOGO SDOPPIATA */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Città *
                </label>
                <input
                  type="text"
                  required
                  placeholder="Es. Milano"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  value={citta}
                  onChange={(e) => setCitta(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Edificio / Aula <span className="text-gray-400 font-normal">(Opzionale)</span>
                </label>
                <input
                  type="text"
                  placeholder="Es. U4 - Biblioteca"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-blue-500 outline-none"
                  value={indirizzo}
                  onChange={(e) => setIndirizzo(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Descrizione *</label>
              <textarea
                value={descrizione}
                onChange={(e) => setDescrizione(e.target.value)}
                required
                rows={2}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none resize-y"
              />
            </div>

            <div className="bg-gray-50 p-4 rounded-lg border border-gray-100">
              <div className="flex justify-between items-center mb-2">
                <label className="block text-sm font-bold text-gray-700">
                  Tag dell'evento *
                </label>
                <span className={`text-xs font-bold ${tags.length === MAX_TAGS ? 'text-orange-500' : 'text-gray-500'}`}>
                  {tags.length} / {MAX_TAGS}
                </span>
              </div>

              <div className="flex gap-2 mb-3">
                <div className="relative flex-1">
                  <input
                    ref={tagInputRef}
                    type="text"
                    value={tagInput}
                    onChange={(e) => {
                      setTagInput(e.target.value);
                      e.target.setCustomValidity("");
                    }}
                    onKeyDown={handleKeyDown}
                    onFocus={() => setIsInputFocused(true)}
                    onBlur={() => setIsInputFocused(false)}
                    disabled={tags.length >= MAX_TAGS}
                    className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none bg-white focus:ring-2 focus:ring-blue-500 disabled:bg-gray-100"
                    placeholder={tags.length >= MAX_TAGS ? "Limite raggiunto" : "Aggiungi tag..."}
                  />
                  
                  {isInputFocused && suggestions.length > 0 && tags.length < MAX_TAGS && (
                    <ul className="absolute z-10 w-full bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-40 overflow-y-auto">
                      {suggestions.map((suggestion, index) => (
                        <li
                          key={index}
                          onMouseDown={(e) => {
                            e.preventDefault();
                            addSpecificTag(suggestion);
                          }}
                          className="px-4 py-2 hover:bg-blue-50 cursor-pointer text-sm text-gray-700 transition-colors"
                        >
                          {suggestion}
                        </li>
                      ))}
                    </ul>
                  )}
                </div>

                <button
                  type="button"
                  onClick={handleAddTag}
                  disabled={!tagInput.trim() || tags.length >= MAX_TAGS}
                  className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg hover:bg-blue-200 disabled:opacity-50"
                >
                  <Plus size={20} />
                </button>
              </div>

              <div className="flex flex-wrap gap-2">
                {tags.map((t, index) => (
                  <span
                    key={index}
                    className="inline-flex items-center gap-1 px-3 py-1 bg-blue-600 text-white text-sm font-semibold rounded-full"
                  >
                    {t}
                    <button type="button" onClick={() => handleRemoveTag(t)}>
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Data *</label>
                <input
                  type="date"
                  value={data}
                  onChange={(e) => setData(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Ora *</label>
                <input
                  type="time"
                  value={ora}
                  onChange={(e) => setOra(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none"
                />
              </div>
            </div>

            <div className="grid grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Durata (h) *</label>
                <input
                  type="number"
                  min="1"
                  step="0.5"
                  value={durata}
                  onChange={(e) => setDurata(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none"
                />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Max Persone *</label>
                <input
                  type="number"
                  min="2"
                  value={maxPartecipanti}
                  onChange={(e) => setMaxPartecipanti(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg outline-none"
                />
              </div>
            </div>

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
              <button
                type="button"
                onClick={onClose}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-50"
              >
                Annulla
              </button>
              <button
                type="submit"
                disabled={isLoading}
                className={`flex-1 px-4 py-2 text-white rounded-lg font-bold ${
                  isLoading ? 'bg-blue-400' : 'bg-blue-600 hover:bg-blue-700'
                }`}
              >
                {isLoading
                  ? (isEdit ? 'Salvataggio...' : 'Creazione...')
                  : (isEdit ? 'Salva modifiche' : 'Crea Evento')}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}