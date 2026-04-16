import React, { useState, useEffect } from "react";
import { Search, Filter } from "lucide-react";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal"


export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedTag, setSelectedTag] = useState("Tutti");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedState, setSelectedState] = useState("aperti");
  const [eventToEdit, setEventToEdit] = useState(null);

 const canEdit = (event) => {
  const inizioEvento = new Date(event.dataOra);
  const tra24Ore = new Date(Date.now() + 24 * 60 * 60 * 1000);
  return event.isCreatedByMe && inizioEvento > tra24Ore;
};
 const fetchEvents = async () => {
  try {
    setLoading(true);
    setError("");

    const response = await fetch("/Eventi", {
      method: "GET",
      credentials: "include",
    });

    if (!response.ok) {
      throw new Error("Errore nel recupero degli eventi");
    }

    const data = await response.json();
    console.log("Eventi ricevuti dal backend:", data);
    setEvents(Array.isArray(data) ? data : []);
  } catch (err) {
    console.error("Errore fetch eventi:", err);
    setError("Impossibile caricare gli eventi");
    setEvents([]);
  } finally {
    setLoading(false);
  }
};
  useEffect(() => {
    fetchEvents();
  }, []);

  const visibleEvents = events.filter((event) => {
    if (selectedState === "tutti") return true;
    return event.stato === "aperto";
  });  // Estrazione tag per il filtro
  const allTags = ["Tutti", ...new Set(visibleEvents.flatMap((e) => e.tag || []))];
  // Logica di filtraggio
  const filteredEvents = visibleEvents.filter((event) => {
    const matchesSearch =
      event.titolo.toLowerCase().includes(searchTerm.toLowerCase()) ||
      (event.descrizione && event.descrizione.toLowerCase().includes(searchTerm.toLowerCase()));
    
    const matchesTag =
      selectedTag === "Tutti" || (event.tag && event.tag.includes(selectedTag));
    
    return matchesSearch && matchesTag;
  });


//Passi la funzione all'hook così lui può richiamarla dopo un'azione (es. dopo prenota)
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

      {/* Barra Filtri */}
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
            className="border border-gray-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={selectedTag}
            onChange={(e) => setSelectedTag(e.target.value)}
          >
            {allTags.map(t => <option key={t} value={t}>{t}</option>)}
          </select>
        </div>

        <div className="flex items-center gap-2">
            <select
              className="border border-gray-200 rounded-lg px-3 py-2 bg-white focus:outline-none focus:ring-2 focus:ring-blue-500"
              value={selectedState}
              onChange={(e) => setSelectedState(e.target.value)}
            >
              <option value="aperti">Solo aperti</option>
              <option value="tutti">Tutti</option>
            </select>
          </div>
      </div>

      {/* Griglia che richiama EventCard */}
      {filteredEvents.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredEvents.map((event) => (
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
      ) : (
        <div className="text-center py-20 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
          <Search className="mx-auto text-gray-300 mb-3" size={48} />
          <p className="text-gray-500 font-medium">Nessun evento trovato</p>
        </div>
      )}
    
    {/* lista eventi */}
     {eventToEdit && (
          <EventFormModal
            mode="edit"
            event={eventToEdit}
            onClose={() => setEventToEdit(null)}
            onSuccess={() => {
              setEventToEdit(null);
              fetchEvents();
            }}
          />
        )}
    </div>
  );
}