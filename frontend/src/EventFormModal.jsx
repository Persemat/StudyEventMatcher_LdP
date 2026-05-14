import React, { useEffect, useMemo, useRef, useState } from "react";
import { X, Info, Search } from "lucide-react";
import { formatTagLabel, loadCityOptions, loadTagOptions, sameTag } from "./catalogs";

export default function EventFormModal({ mode, event, onClose, onSuccess, availableTags = [] }) {
  const isEdit = mode === "edit";

  const initialDate = event?.dateTime ? event.dateTime.slice(0, 10) : "";
  const initialTime = event?.dateTime ? event.dateTime.slice(11, 16) : "";

  const [title, setTitle] = useState(event?.title || "");
  const [description, setDescription] = useState(event?.description || "");
  const [date, setDate] = useState(initialDate);
  const [time, setTime] = useState(initialTime);
  const [duration, setDuration] = useState(event?.duration || "");

  const [city, setCity] = useState("");
  const [address, setAddress] = useState("");
  const [formError, setFormError] = useState("");

  const [maxParticipants, setMaxParticipants] = useState(event?.maxParticipants || "");
  const [creatorParticipationRequired, setCreatorParticipationRequired] = useState(
    event?.creatorParticipationRequired ?? event?.creatorPartecipationRequired ?? true
  );

  const [tags, setTags] = useState(() => (event?.tags || []).map(normalizeTag));
  const [tagInput, setTagInput] = useState("");
  const [tagOptions, setTagOptions] = useState(availableTags);
  const [cityOptions, setCityOptions] = useState([]);
  const [isTagInputFocused, setIsTagInputFocused] = useState(false);
  const [isCityInputFocused, setIsCityInputFocused] = useState(false);
  const [message, setMessage] = useState("");
  const [isLoading, setIsLoading] = useState(false);

  const tagInputRef = useRef(null);
  const cityInputRef = useRef(null);
  const MAX_TAGS = 3;

  useEffect(() => {
    let active = true;

    if (availableTags.length > 0) {
      setTagOptions(availableTags);
    } else {
      loadTagOptions()
        .then((data) => active && setTagOptions(data))
        .catch(() => active && setFormError("Impossibile caricare il catalogo tag."));
    }

    loadCityOptions()
      .then((data) => active && setCityOptions(data))
      .catch(() => active && setFormError("Impossibile caricare il catalogo comuni."));

    return () => {
      active = false;
    };
  }, [availableTags]);

  useEffect(() => {
    if (isEdit && event?.location) {
      const separatorIndex = event.location.indexOf("-");
      if (separatorIndex >= 0) {
        setCity(event.location.slice(0, separatorIndex).trim());
        setAddress(event.location.slice(separatorIndex + 1).trim());
      } else {
        setCity(event.location);
        setAddress("");
      }
    }
  }, [isEdit, event]);

  const tagSuggestions = useMemo(() => {
    const query = tagInput.trim().toLowerCase();
    const filtered = tagOptions
      .filter((option) => {
        const matches = !query || option.label.toLowerCase().includes(query);
        const alreadySelected = tags.some((tag) => sameTag(tag, option));
        return matches && !alreadySelected;
      })
      .slice(0, 60);

    return filtered.reduce((groups, option) => {
      if (!groups[option.macroTag]) groups[option.macroTag] = [];
      groups[option.macroTag].push(option);
      return groups;
    }, {});
  }, [tagInput, tagOptions, tags]);

  const citySuggestions = useMemo(() => {
    const query = city.trim().toLowerCase();
    if (!query) return [];
    return cityOptions
      .filter((option) => option.name.toLowerCase().startsWith(query))
      .slice(0, 30);
  }, [city, cityOptions]);

  const validCityLabels = useMemo(
    () => new Set(cityOptions.map((option) => option.label)),
    [cityOptions]
  );

  const addTag = (option) => {
    if (tags.length >= MAX_TAGS || tags.some((tag) => sameTag(tag, option))) {
      return;
    }
    setTags([...tags, { macroTag: option.macroTag, microTag: option.microTag || null }]);
    setTagInput("");
    setIsTagInputFocused(false);
  };

  const handleTagKeyDown = (e) => {
    if (e.key !== "Enter") return;
    e.preventDefault();
    const firstGroup = Object.values(tagSuggestions)[0];
    if (firstGroup?.[0]) {
      addTag(firstGroup[0]);
    }
  };

  const handleRemoveTag = (tagToRemove) => {
    setTags(tags.filter((tag) => !sameTag(tag, tagToRemove)));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setFormError("");

    if (!city.trim()) {
      setFormError("La città è richiesta.");
      cityInputRef.current?.focus();
      return;
    }

    if (!validCityLabels.has(city.trim())) {
      setFormError("Seleziona una città dal catalogo comuni.");
      cityInputRef.current?.focus();
      return;
    }

    if (tags.length === 0) {
      setFormError("Seleziona almeno un tag dal catalogo.");
      tagInputRef.current?.focus();
      return;
    }

    const invalidTag = tags.some(
      (tag) => !tagOptions.some((option) => sameTag(option, tag))
    );
    if (invalidTag) {
      setFormError("Uno o più tag non appartengono al catalogo.");
      tagInputRef.current?.focus();
      return;
    }

    setIsLoading(true);
    setMessage("");

    const dateTimeFormatted = `${date}T${time}:00`;
    const finalLocation = address.trim()
      ? `${city.trim()}-${address.trim()}`
      : city.trim();

    const payload = {
      title,
      dateTime: dateTimeFormatted,
      duration: parseFloat(duration),
      location: finalLocation,
      tags,
      description,
      maxParticipants: parseInt(maxParticipants),
      creatorParticipationRequired,
    };

    const url = isEdit ? `/api/events/${event.id}` : "/api/events";
    const method = isEdit ? "PUT" : "POST";

    try {
      const response = await fetch(url, {
        method,
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify(payload),
      });

      if (response.ok) {
        if (onSuccess) onSuccess();
      } else {
        const errorText = await response.text();
        setMessage(`Errore: ${errorText}`);
      }
    } catch {
      setMessage("Errore di connessione al server.");
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="fixed inset-0 bg-black/50 flex items-center justify-center z-50 p-4">
      <div className="bg-white rounded-lg shadow-xl max-w-2xl w-full max-h-[90vh] flex flex-col">
        <div className="flex justify-between items-center p-6 border-b border-gray-200">
          <h2 className="text-2xl font-bold text-gray-900">
            {isEdit ? "Modifica evento" : "Crea nuovo evento"}
          </h2>
          <button onClick={onClose} className="text-gray-400 hover:text-gray-600">
            <X className="w-6 h-6" />
          </button>
        </div>

        <div className="overflow-y-auto p-6">
          {message && (
            <div className={`p-4 mb-4 rounded-lg font-medium ${
              message.startsWith("Errore") ? "bg-red-50 text-red-700" : "bg-green-50 text-green-700"
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

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div className="relative">
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Città *
                </label>
                <input
                  ref={cityInputRef}
                  type="text"
                  required
                  placeholder="es: Milano"
                  className="w-full px-4 py-2 border border-gray-300 rounded-lg"
                  value={city}
                  onChange={(e) => setCity(e.target.value)}
                  onFocus={() => setIsCityInputFocused(true)}
                  onBlur={() => setIsCityInputFocused(false)}
                />
                {isCityInputFocused && citySuggestions.length > 0 && (
                  <ul className="absolute z-20 w-full bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-48 overflow-y-auto">
                    {citySuggestions.map((option) => (
                      <li
                        key={option.label}
                        onMouseDown={(e) => {
                          e.preventDefault();
                          setCity(option.label);
                          setIsCityInputFocused(false);
                        }}
                        className="px-4 py-2 hover:bg-blue-50 cursor-pointer text-sm text-gray-700 transition-colors"
                      >
                        {option.label}
                      </li>
                    ))}
                  </ul>
                )}
              </div>

              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">
                  Edificio / Aula <span className="text-gray-400">(Opzionale)</span>
                </label>
                <input
                  type="text"
                  placeholder="es: Edificio U4 - Biblioteca"
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

            <div className="bg-gray-50 p-4 rounded-lg">
              <div className="flex justify-between items-center mb-2">
                <label className="block text-sm font-bold text-gray-700">
                  Tag Eventi *
                </label>
                <span className={`text-xs font-bold ${tags.length === MAX_TAGS ? "text-orange-500" : "text-gray-500"}`}>
                  {tags.length} / {MAX_TAGS}
                </span>
              </div>

              <div className="relative mb-3">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
                <input
                  ref={tagInputRef}
                  type="text"
                  value={tagInput}
                  onChange={(e) => setTagInput(e.target.value)}
                  onKeyDown={handleTagKeyDown}
                  onFocus={() => setIsTagInputFocused(true)}
                  onBlur={() => setIsTagInputFocused(false)}
                  disabled={tags.length >= MAX_TAGS}
                  className="w-full pl-10 pr-4 py-2 border border-gray-300 rounded-lg"
                  placeholder={tags.length >= MAX_TAGS ? "Limite raggiunto" : "Cerca tag o macroargomento..."}
                />
                {isTagInputFocused && Object.keys(tagSuggestions).length > 0 && tags.length < MAX_TAGS && (
                  <div className="absolute z-20 w-full bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-56 overflow-y-auto">
                    {Object.entries(tagSuggestions).map(([macroTag, options]) => (
                      <div key={macroTag}>
                        <div className="px-4 py-2 text-[11px] uppercase tracking-wide text-gray-400 bg-gray-50 font-bold">
                          {macroTag}
                        </div>
                        {options.map((option) => (
                          <button
                            key={option.label}
                            type="button"
                            onMouseDown={(e) => {
                              e.preventDefault();
                              addTag(option);
                            }}
                            className="w-full text-left px-4 py-2 hover:bg-blue-50 text-sm text-gray-700 transition-colors"
                          >
                            {option.isMacro ? option.macroTag : option.microTag}
                          </button>
                        ))}
                      </div>
                    ))}
                  </div>
                )}
              </div>

              <div className="flex flex-wrap gap-2">
                {tags.map((tag) => (
                  <span key={formatTagLabel(tag)} className="px-3 py-1 bg-blue-600 text-white rounded-full flex items-center gap-1 text-xs font-semibold">
                    {formatTagLabel(tag)}
                    <button type="button" onClick={() => handleRemoveTag(tag)}>
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
                <label className="block text-sm font-medium text-gray-700 mb-1">Max partecipanti *</label>
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
                checked={creatorParticipationRequired}
                onChange={(e) => setCreatorParticipationRequired(e.target.checked)}
                className="w-5 h-5 text-blue-600 border-gray-300 rounded focus:ring-blue-500 cursor-pointer"
              />
              <div className="flex-1">
                <label htmlFor="creatorRequired" className="text-sm font-bold text-gray-800 cursor-pointer flex items-center gap-2">
                  La mia presenza è obbligatoria
                  <Info size={14} className="text-blue-400" />
                </label>
                <p className="text-xs text-gray-500 mt-0.5">
                  Se abilitato, l'evento verrà annullato se decidi di ritirarti.
                </p>
              </div>
            </div>

            <div className="flex gap-3 pt-4 border-t border-gray-100">
              <button
                type="button"
                onClick={onClose}
                className="flex-1 px-4 py-2 border border-gray-300 rounded-lg font-bold text-gray-700 hover:bg-gray-50"
              >
                Cancella
              </button>

              <button
                type="submit"
                disabled={isLoading}
                className={`flex-1 px-4 py-2 text-white rounded-lg font-bold ${
                  isLoading ? "bg-blue-400" : "bg-blue-600 hover:bg-blue-700"
                }`}
              >
                {isLoading
                  ? (isEdit ? "Salvataggio..." : "Creazione...")
                  : (isEdit ? "Salva modifiche" : "Crea evento")}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
}

function normalizeTag(tag) {
  if (!tag) return { macroTag: "", microTag: null };
  if (typeof tag === "string") {
    const parts = tag.split(/\s*>\s*/, 2);
    return { macroTag: parts[0], microTag: parts[1] || null };
  }
  return { macroTag: tag.macroTag, microTag: tag.microTag || null };
}
