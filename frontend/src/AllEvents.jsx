import React, { useState, useEffect, useRef } from "react";
import { Search, Filter, ChevronDown } from "lucide-react"; // <-- Aggiunto ChevronDown se lo hai, altrimenti usa Filter
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";

export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState([]); // Tolto "Tutti", lo gestiamo visivamente
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedTags, setSelectedTags] = useState([]);
  
  // NUOVO: Stato per gestire l'apertura della tendina
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);

  // Per chiudere la tendina se si clicca fuori
  const dropdownRef = useRef(null);
  useEffect(() => {
    const handleClickOutside = (event) => {
      if (dropdownRef.current && !dropdownRef.current.contains(event.target)) {
        setIsDropdownOpen(false);
      }
    };
    document.addEventListener("mousedown", handleClickOutside);
    return () => document.removeEventListener("mousedown", handleClickOutside);
  }, []);

  const canEdit = (event) => {
    const inizioEvento = new Date(event.dataOra);
    const tra24Ore = new Date(Date.now() + 24 * 60 * 60 * 1000);
    return event.isCreatedByMe && inizioEvento > tra24Ore;
  };

  const toggleTag = (tag) => {
    setSelectedTags((prev) => 
      prev.includes(tag) 
        ? prev.filter((t) => t !== tag) 
        : [...prev, tag]                
    );
  };

  const fetchTags = async () => {
    try {
      const res = await fetch("http://localhost:8080/Eventi/tags", { credentials: "include" });
      if (res.ok) {
        const data = await res.json();
        // Rimuoviamo eventuali "Tutti" che arrivano dal backend, non ci servono più nelle checkbox
        setAvailableTags(data.filter(t => t !== "Tutti"));
      }
    } catch (err) {
      console.error("Errore fetch tags:", err);
    }
  };

  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const url = new URL("http://localhost:8080/Eventi/cerca");
      if (searchTerm) url.searchParams.append("search", searchTerm);
      
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
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchTags();
  }, []);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchEvents();
    }, 300);
    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, selectedTags]);

  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);

  const handleEdit = (event) => {
    if (!event.isCreatedByMe) return;
    setEventToEdit(event);
  };

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Esplora Eventi</h1>
        <p className="text-gray-600">Catalogo gruppi di studio</p>
      </div>

      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        
        {/* Ricerca Testuale */}
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

        {/* CUSTOM DROPDOWN A SCELTA MULTIPLA */}
        <div className="relative w-full md:w-64" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="w-full flex items-center justify-between px-4 py-2 bg-white border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 hover:bg-gray-50 transition-colors"
          >
            <div className="flex items-center gap-2 text-gray-700">
              <Filter size={18} className="text-gray-500" />
              <span className="font-medium">
                {selectedTags.length === 0 
                  ? "Tutti i tag" 
                  : `${selectedTags.length} tag selezionat${selectedTags.length > 1 ? 'i' : 'o'}`}
              </span>
            </div>
            {/* Se non hai l'icona ChevronDown, puoi mettere un semplice ▼ */}
            <span className="text-gray-400 text-xs">▼</span>
          </button>

          {/* Il menù che appare quando clicchi */}
          {isDropdownOpen && (
            <div className="absolute z-20 w-full mt-2 bg-white border border-gray-200 rounded-lg shadow-xl max-h-60 overflow-y-auto">
              <div className="p-2 border-b border-gray-100">
                <button 
                  onClick={() => setSelectedTags([])}
                  className="w-full text-left px-2 py-1.5 text-sm text-blue-600 font-semibold hover:bg-blue-50 rounded"
                >
                  Deseleziona tutto
                </button>
              </div>
              <div className="p-1">
                {availableTags.length === 0 ? (
                  <p className="text-sm text-gray-500 p-3 text-center">Nessun tag disponibile</p>
                ) : (
                  availableTags.map((tag) => (
                    <label 
                      key={tag} 
                      className="flex items-center gap-3 px-3 py-2.5 hover:bg-gray-50 rounded-md cursor-pointer transition-colors"
                    >
                      <input
                        type="checkbox"
                        checked={selectedTags.includes(tag)}
                        onChange={() => toggleTag(tag)}
                        className="w-4 h-4 text-blue-600 rounded border-gray-300 focus:ring-blue-500 cursor-pointer"
                      />
                      <span className="text-sm text-gray-700 font-medium">{tag}</span>
                    </label>
                  ))
                )}
              </div>
            </div>
          )}
        </div>

      </div>

      {/* Griglia EventCard */}
      {!loading && events.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard 
              key={event.id} 
              event={event} 
              onBook={() => handleBook(event.id)}
              onCancelBooking={() =>
                handleCancelBooking(
                  event.id,
                  event.partecipazioneCreatoreNecessaria,
                  event.isCreatedByMe
                )
              }
              onEdit={canEdit(event) ? () => handleEdit(event) : undefined}
              canEdit={canEdit(event)}
            />
          ))}
        </div>
      ) : !loading && events.length === 0 ? (
        <div className="text-center py-20 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
          <Search className="mx-auto text-gray-300 mb-3" size={48} />
          <p className="text-gray-500 font-medium">Nessun evento trovato</p>
        </div>
      ) : (
        <div className="text-center py-10">
           <p className="text-gray-500">Ricerca in corso...</p>
        </div>
      )}
    
      {/* Modale di Modifica */}
      {eventToEdit && (
        <EventFormModal
          mode="edit"
          event={eventToEdit}
          onClose={() => setEventToEdit(null)}
          onSuccess={() => {
            setEventToEdit(null);
            fetchEvents();
          }}
          availableTags={availableTags}
        />
      )}
    </div>
  );
}