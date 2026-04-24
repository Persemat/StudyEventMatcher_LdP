//EventFormalModal
import React, { useState, useEffect, useRef } from 'react';
import { X, Plus, Info } from "lucide-react";

export default function EventFormModal({ mode, event, onClose, onSuccess, availableTags = [] }) {
  const isEdit = mode === "edit";

  const initialDate = event?.dateTime ? event.dateTime.slice(0, 10) : "";
  const initialTime = event?.dateTime ? event.dateTime.slice(11, 16) : "";

  const [title, setTitle] = useState(event?.title || '');
  const [description, setDescription] = useState(event?.description || '');
  const [date, setDate] = useState(initialDate);
  const [time, setTime] = useState(initialTime);
  const [duration, setDuration] = useState(event?.duration || '');

  // Location state
  const [city, setCity] = useState("");
  const [address, setAddress] = useState("");
  const [formError, setFormError] = useState("");

  const [maxParticipants, setMaxParticipants] = useState(event?.maxParticipants || '');
  const [creatorPartecipationRequired, setCreatorPartecipationRequired] = useState(
    event?.creatorPartecipationRequired ?? true
  );

  const [tags, setTags] = useState(event?.tags || []);
  const [tagInput, setTagInput] = useState('');

  const [isInputFocused, setIsInputFocused] = useState(false);
  const [message, setMessage] = useState('');
  const [isLoading, setIsLoading] = useState(false);

  const tagInputRef = useRef(null);
  const MAX_TAGS = 3;

  // Parse location when editing
  useEffect(() => {
    if (isEdit && event) {
      if (event.location && event.location.includes("-")) {
        const [c, ...rest] = event.location.split("-");
        setCity(c.trim());
        setAddress(rest.join("-").trim());
      } else {
        setCity(event.location || "");
        setAddress("");
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
    setFormError("");

    if (!city.trim()) {
      setFormError("La città è richiesta.");
      return;
    }

    if (tags.length === 0) {
      tagInputRef.current.setCustomValidity("Completa questo campo.");
      tagInputRef.current.reportValidity();
      return;
    } else {
      tagInputRef.current.setCustomValidity("");
    }

    setIsLoading(true);
    setMessage("");

    const dateTimeFormatted = `${date}T${time}:00`;

    const finalLocation = address.trim()
      ? `${city.trim()}-${address.trim()}`
      : city.trim();

    const payload = {
        title: title,
        dateTime: dateTimeFormatted,
        duration: parseFloat(duration),
        location: finalLocation,
        tags: tags,
        description: description,
        maxParticipants: parseInt(maxParticipants),
        creatorParticipationRequired: creatorPartecipationRequired
        };

    const url = isEdit ? `/api/events/${event.id}` : `/api/events`;
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
        const errorText = await response.text();
        setMessage(`Error: ${errorText}`);
      }
    } catch (error) {
      setMessage('Server connection error.');
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
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="overflow-y-auto p-6">

          {message && (
            <div className={`p-4 mb-4 rounded-lg font-medium ${
              message.includes('Error') ? 'bg-red-50 text-red-700' : 'bg-green-50 text-green-700'
            }`}>
              {message}
            </div>
          )}

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
                value={title}
                onChange={(e) => setTitle(e.target.value)}
                required
                className="w-full px-4 py-2 border border-gray-300 rounded-lg"
              />
            </div>

            {/* Location */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Città *
                </label>
                <input
                  type="text"
                  required
                  placeholder="es: Milano"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Edificio / Aula <span className="text-gray-400">(Opzionale)</span>
                </label>
                <input
                  type="text"
                  placeholder="es: EdificioU4 - Biblioteca"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  value={address}
                  onChange={(e) => setAddress(e.target.value)}
                />
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Descrizione *</label>
              <textarea
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                required
                rows={2}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg"
              />
            </div>

            {/* Tags */}
            <div className="bg-gray-50 p-4 rounded-lg">

              <div className="flex justify-between items-center mb-2">
                <label className="block text-sm font-bold text-gray-700">
                  Tag Eventi *
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
                    className="flex-1 px-4 py-2 border border-gray-300 rounded-lg"
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
                    className="px-4 py-2 bg-blue-100 text-blue-700 rounded-lg"
                  >
                    <Plus size={20} />
                  </button>
              </div>

              <div className="flex flex-wrap gap-2">
                {tags.map((t, index) => (
                  <span key={index} className="px-3 py-1 bg-blue-600 text-white rounded-full flex items-center gap-1">
                    {t}
                    <button type="button" onClick={() => handleRemoveTag(t)}>
                      <X size={14} />
                    </button>
                  </span>
                ))}
              </div>
            </div>

            {/* Date / Time */}
            <div className="grid grid-cols-2 gap-4">

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Data *</label>
                <input
                  type="date"
                  value={date}
                  onChange={(e) => setDate(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Ora *</label>
                <input
                  type="time"
                  value={time}
                  onChange={(e) => setTime(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
              </div>
            </div>

            {/* Duration / Max */}
            <div className="grid grid-cols-2 gap-4">

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Durata (h) *</label>
                <input
                  type="number"
                  min="1"
                  value={duration}
                  onChange={(e) => setDuration(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Max Participanti *</label>
                <input
                  type="number"
                  min="2"
                  value={maxParticipants}
                  onChange={(e) => setMaxParticipants(e.target.value)}
                  required
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                />
              </div>
            </div>

            <div className="flex items-center gap-3 p-4 bg-blue-50/50 rounded-lg border border-blue-100">
              <input
                type="checkbox"
                id="creatorRequired"
                checked={creatorPartecipationRequired}
                onChange={(e) => setCreatorPartecipationRequired(e.target.checked)}
                className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
              />
              <div className="flex-1">
                <label htmlFor="creatorRequired" className="text-sm font-bold text-gray-800 cursor-pointer flex items-center gap-2">
                  La mia presenza è obbligatoria
                  <Info size={14} className="text-blue-400" />
                </label>
                <p className="text-xs text-gray-500 mt-0.5">
                  Se abilitato, l’evento verrà annullato se decidi di ritirarti.
                </p>
              </div>
            </div>



            {/* Actions */}
            <div className="flex gap-3 pt-4 border-t border-gray-100">

              <button 
                type="button" 
                onClick={onClose} 
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-50">
                Cancella
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