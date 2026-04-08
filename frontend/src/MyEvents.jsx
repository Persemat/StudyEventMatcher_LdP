import React, { useState } from "react";
import { Calendar, Plus } from "lucide-react";

import EventCard from "./EventCard"; 
import CreaEvento from "./creaEvento"; 
import ModalCreateEvent from "./ModalCreateEvent";

// Mock temporaneo (in futuro userai una fetch)
const mockData = [
  { id: 1, titolo: "Studio Basi di Dati", descrizione: "Ripasso SQL", dataOra: "2026-04-10T15:00:00", luogo: "Aula 2", maxPartecipanti: 5, currentParticipants: 2, isCreatedByMe: true, isBooked: false, tag: ["Informatica"], partecipazioneCreatoreNecessaria: false },
  { id: 2, titolo: "Analisi 1", descrizione: "Esercizi limiti", dataOra: "2026-04-12T10:00:00", luogo: "Biblioteca", maxPartecipanti: 10, currentParticipants: 8, isCreatedByMe: false, isBooked: true, tag: ["Matematica"], partecipazioneCreatoreNecessaria: true }
];

export default function MyEvents() {
  const [events, setEvents] = useState(mockData);
  const [activeTab, setActiveTab] = useState("created"); // Rimosso il tipo TS
  const [showCreateModal, setShowCreateModal] = useState(false);

  // Filtriamo gli eventi in base al flag
  const createdEvents = events.filter((e) => e.isCreatedByMe);
  const bookedEvents = events.filter((e) => e.isBooked);

  const fetchEventi = () => {
    console.log("📡 [fetchEventi] Chiamata simulata! Il modale o la pagina hanno chiesto i dati.");
    console.log("🛠️ In attesa che Spring Boot sia pronto per l'endpoint /eventi/miei-eventi...");
    
    // DA IMPLEMENTARE
  };

  const handleBookEvent = (eventId) => {
    setEvents((prev) =>
      prev.map((ev) =>
        ev.id === eventId ? { ...ev, isBooked: true, currentParticipants: ev.currentParticipants + 1 } : ev
      )
    );
  };

  const handleCancelBooking = (eventId) => {
    setEvents((prev) =>
      prev.map((ev) =>
        ev.id === eventId ? { ...ev, isBooked: false, currentParticipants: Math.max(0, ev.currentParticipants - 1) } : ev
      )
    );
  };

  const handleDeleteEvent = (eventId) => {
    if (window.confirm("Sei sicuro di voler eliminare questo evento?")) {
      setEvents((prev) => prev.filter((e) => e.id !== eventId));
    }
  };

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">I Miei Eventi</h1>
          <p className="text-gray-600 mt-1">Gestisci le tue attività di studio</p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex items-center gap-2"
        >
          <Plus className="w-5 h-5" /> Crea Evento
        </button>
      </div>

      {/* Tabs */}
      <div className="bg-white rounded-xl shadow-sm border border-gray-100 overflow-hidden">
        <div className="flex">
          <button
            onClick={() => setActiveTab("created")}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all ${
              activeTab === "created"
                ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/30"
                : "text-gray-500 hover:bg-gray-50"
            }`}
          >
            Creati da me ({createdEvents.length})
          </button>
          <button
            onClick={() => setActiveTab("booked")}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all ${
              activeTab === "booked"
                ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/30"
                : "text-gray-500 hover:bg-gray-50"
            }`}
          >
            A cui partecipo ({bookedEvents.length})
          </button>
        </div>
      </div>

      {/* Grid Content */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
        {(activeTab === "created" ? createdEvents : bookedEvents).map((event) => (
          <EventCard
            key={event.id}
            event={event}
            onBook={handleBookEvent}
            onCancelBooking={handleCancelBooking}
            onDelete={handleDeleteEvent} // Nota: EventCard deve gestire questa prop
          />
        ))}

        {/* Empty State */}
        {((activeTab === "created" && createdEvents.length === 0) ||
          (activeTab === "booked" && bookedEvents.length === 0)) && (
          <div className="col-span-full bg-white rounded-xl shadow-sm border-2 border-dashed border-gray-200 p-12 text-center">
            <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
            <p className="text-gray-500 font-medium">Nessun evento in questa categoria</p>
          </div>
        )}
      </div>

      {/* Modale CreaEvento */}
      {showCreateModal && (
        <ModalCreateEvent 
        onClose={() => setShowCreateModal(false)} 
        onSuccess={() => fetchEventi()} // <--- DA IMPLEMENTARE
        />
      )}
    </div>
  );
}