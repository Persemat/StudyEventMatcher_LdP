import React, { useState, useEffect, useRef } from "react";
import { Search, Filter, MapPin } from "lucide-react"; 
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";

export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchLuogo, setSearchLuogo] = useState(""); // NUOVO: Stato per il filtro città
  const [selectedTags, setSelectedTags] = useState([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);

  const dropdownRef = useRef(null);

  const canEdit = (event) => {
    const inizioEvento = new Date(event.dataOra);
    const tra24Ore = new Date(Date.now() + 24 * 60 * 60 * 1000);
    return event.isCreatedByMe && inizioEvento > tra24Ore;
  };

  const fetchTags = async () => {
    try {
      const res = await fetch("http://localhost:8080/Eventi/tags", { credentials: "include" });
      if (res.ok) {
        const data = await res.json();
        setAvailableTags(data.filter(t => t !== "Tutti"));
      }
    } catch (err) { console.error("Errore fetch tags:", err); }
  };

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError("");
      const url = new URL("http://localhost:8080/Eventi/cerca");
      
      if (searchTerm) url.searchParams.append("search", searchTerm);
      if (searchLuogo) url.searchParams.append("luogo", searchLuogo); // NUOVO: Inviato al backend
      if (selectedTags.length > 0) {
        selectedTags.forEach((t) => url.searchParams.append("tags", t));
      }

      const response = await fetch(url.toString(), {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) throw new Error("Errore nel recupero degli eventi");
      const data = await response.json();
      setEvents(Array.isArray(data) ? data : []);
    } catch (err) {
      setError("Impossibile caricare gli eventi");
      setEvents([]);
    } finally { setLoading(false); }
  };

  useEffect(() => { fetchTags(); }, []);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => { fetchEvents(); }, 300);
    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, selectedTags, searchLuogo]); // Aggiunto searchLuogo alle dipendenze

  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Esplora Eventi</h1>
        <p className="text-gray-600">Catalogo gruppi di studio</p>
      </div>

      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        {/* Ricerca Titolo */}
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Cerca per titolo o descrizione..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        {/* NUOVO: Ricerca Città/Luogo */}
        <div className="relative md:w-64">
          <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Filtra per città..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchLuogo}
            onChange={(e) => setSearchLuogo(e.target.value)}
          />
        </div>

        {/* Dropdown Tag (Multiplo) */}
        <div className="relative md:w-64" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="w-full flex items-center justify-between px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <span className="text-sm font-medium text-gray-700">
              {selectedTags.length === 0 ? "Tutti i tag" : `${selectedTags.length} tag`}
            </span>
            <span className="text-gray-400 text-xs">▼</span>
          </button>
          {isDropdownOpen && (
            <div className="absolute z-20 w-full mt-2 bg-white border border-gray-200 rounded-lg shadow-xl max-h-60 overflow-y-auto p-1">
              {availableTags.map((tag) => (
                <label key={tag} className="flex items-center gap-3 px-3 py-2 hover:bg-gray-50 rounded cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedTags.includes(tag)}
                    onChange={() => setSelectedTags(prev => prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag])}
                    className="rounded text-blue-600"
                  />
                  <span className="text-sm">{tag}</span>
                </label>
              ))}
            </div>
          )}
        </div>
      </div>

      {!loading && events.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard 
              key={event.id} 
              event={event} 
              onBook={() => handleBook(event.id)}
              onCancelBooking={() => handleCancelBooking(event.id, event.partecipazioneCreatoreNecessaria, event.isCreatedByMe)}
              onEdit={canEdit(event) ? () => setEventToEdit(event) : undefined}
              canEdit={canEdit(event)}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
          <p className="text-gray-500">{loading ? "Ricerca in corso..." : "Nessun evento trovato"}</p>
        </div>
      )}

      {eventToEdit && (
        <EventFormModal
          mode="edit"
          event={eventToEdit}
          onClose={() => setEventToEdit(null)}
          onSuccess={() => { setEventToEdit(null); fetchEvents(); }}
        />
      )}
    </div>
  );
}