import React, { useState, useEffect } from "react";
import { Search, Filter } from "lucide-react";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";

export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState(["Tutti"]); // Nuovo stato per la tendina
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedTag, setSelectedTag] = useState("Tutti");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);

  const canEdit = (event) => {
    const inizioEvento = new Date(event.dataOra);
    const tra24Ore = new Date(Date.now() + 24 * 60 * 60 * 1000);
    return event.isCreatedByMe && inizioEvento > tra24Ore;
  };

  // 1. SCARICA I TAG (Gira solo all'avvio)
  const fetchTags = async () => {
    try {
      const res = await fetch("http://localhost:8080/Eventi/tags", { credentials: "include" });
      if (res.ok) {
        const data = await res.json();
        setAvailableTags(["Tutti", ...data]);
      }
    } catch (err) {
      console.error("Errore fetch tags:", err);
    }
  };

  // 2. SCARICA GLI EVENTI FILTRATI (Chiede al backend di fare il lavoro sporco)
  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError("");

      const url = new URL("http://localhost:8080/Eventi/cerca");
      
      // Costruisce l'URL dinamicamente (es. /Eventi/cerca?tag=Matematica&search=Analisi)
      if (searchTerm) url.searchParams.append("search", searchTerm);
      if (selectedTag !== "Tutti") url.searchParams.append("tag", selectedTag);

      const response = await fetch(url.toString(), {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Errore nel recupero degli eventi");
      }

      const data = await response.json();
      setEvents(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Errore fetch eventi:", err);
      setError("Impossibile caricare gli eventi");
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  // EFFETTO 1: Al primo avvio carica i tag
  useEffect(() => {
    fetchTags();
  }, []);

  // EFFETTO 2: Ricerca Dinamica. Ogni volta che scrivi o cambi tag, richiama il backend
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchEvents();
    }, 300); // Aspetta 0.3s prima di fare la chiamata

    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, selectedTag]);

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

      {/* Barra Filtri - Pulita dal filtro stato */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        
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

        <div className="flex items-center gap-2">
          <Filter size={18} className="text-gray-500" />
          <select
            className="border border-gray-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500 cursor-pointer"
            value={selectedTag}
            onChange={(e) => setSelectedTag(e.target.value)}
          >
            {/* Usa i tag veri scaricati dal DB */}
            {availableTags.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>

      </div>

      {/* Griglia che richiama EventCard: ora si mappa direttamente 'events' */}
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
        /* Semplice feedback visivo durante il caricamento */
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
            fetchEvents(); // Ricarica gli eventi dopo la modifica
          }}
          availableTags={availableTags} // Passiamo i tag anche alla modale!
        />
      )}
    </div>
  );
}