import React, { useState, useEffect } from "react";
import { Search, Filter } from "lucide-react";
import EventCard from "./EventCard"; // Assicurati che il file si chiami così
import { useEventActions } from "./hooks/useEventActions";



// Mock temporaneo (in futuro useremo una fetch)
// TO DO: 
// Rimuovere mockData una volta implementata la fetch
// const mockData = [
//   { 
//     id: 1, 
//     titolo: "Ripasso Analisi 1", 
//     descrizione: "Prepariamoci per lo scritto di lunedì!", 
//     dataOra: "2026-04-10T10:30:00", 
//     luogo: "Aula 4, Edificio A",
//     durata: 2.0,
//     tag: ["Matematica", "Analisi"],
//     maxPartecipanti: 10,
//     currentParticipants: 3,
//     isBooked: false,
//     isCreatedByMe: false, // Creato da qualcun altro
//     stato: "aperto",
//     creatore: { nome: "Mario", cognome: "Rossi" },
//     partecipazioneCreatoreNecessaria: true
//   },
//   { 
//     id: 2, 
//     titolo: "Progetto Backend Spring", 
//     descrizione: "Sistemiamo le pipeline e la sicurezza.", 
//     dataOra: "2026-04-12T15:00:00", 
//     luogo: "Laboratorio Informatica",
//     durata: 3.5,
//     tag: ["Informatica", "Java"],
//     maxPartecipanti: 5,
//     currentParticipants: 4,
//     isBooked: true,       // Già prenotato
//     isCreatedByMe: false,
//     stato: "aperto",
//     creatore: { nome: "Luigi", cognome: "Verdi" },
//     partecipazioneCreatoreNecessaria: false
//   },
//   { 
//     id: 3, 
//     titolo: "Workshop React", 
//     descrizione: "Creazione componenti modulari.", 
//     dataOra: "2026-04-15T09:00:00", 
//     luogo: "Online - Meet",
//     durata: 1.5,
//     tag: ["Informatica", "Frontend"],
//     maxPartecipanti: 20,
//     currentParticipants: 1,
//     isBooked: false,
//     isCreatedByMe: true,  // Creato da ME
//     stato: "aperto",
//     creatore: { nome: "Tu", cognome: "Utente" },
//     partecipazioneCreatoreNecessaria: true
//   }
// ];



export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [selectedTag, setSelectedTag] = useState("Tutti");
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");



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

  // Estrazione tag per il filtro
  const allTags = ["Tutti", ...new Set(events.flatMap((e) => e.tag || []))];

  // Logica di filtraggio
  const filteredEvents = events.filter((event) => {
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
    console.log("Modifica evento ID:", event.id);
    // Qui andrà la logica per aprire la modale di modifica
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
      </div>

      {/* Griglia che richiama EventCard */}
      {filteredEvents.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {filteredEvents.map((event) => (
            <EventCard 
              key={event.id} 
              event={event} 
              onBook={() => handleBook(event.id)}
              onCancelBooking={() => handleCancelBooking(event.id, event.partecipazioneCreatoreNecessaria)}
              onEdit={() => handleEdit(event)}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
          <Search className="mx-auto text-gray-300 mb-3" size={48} />
          <p className="text-gray-500 font-medium">Nessun evento trovato</p>
        </div>
      )}
    </div>
  );
}