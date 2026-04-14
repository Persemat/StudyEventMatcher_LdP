import React, { useState, useEffect } from "react";
import { Calendar, Plus } from "lucide-react";
import { useEventActions } from "./hooks/useEventActions";
import EventCard from "./EventCard";
import ModalCreateEvent from "./ModalCreateEvent";

export default function MyEvents() {
  const [events, setEvents] = useState([]);
  const [activeTab, setActiveTab] = useState("created");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  const now = new Date();

  // Controllo se evento è futuro o passato
  const isFutureEvent = (event) => new Date(event.dataOra) >= now;
  const isPastEvent = (event) => new Date(event.dataOra) < now;

  // Solo eventi futuri creati da me
  const createdEvents = events.filter(
    (e) => e.isCreatedByMe && isFutureEvent(e)
  );

  // Solo eventi futuri a cui partecipo ma non creati da me
  const bookedEvents = events.filter(
    (e) => e.isBooked && !e.isCreatedByMe && isFutureEvent(e)
  );

  // Storico: eventi passati creati da me o a cui ho partecipato
  const historyEvents = events.filter(
    (e) => (e.isCreatedByMe || e.isBooked) && isPastEvent(e)
  );

  const fetchEvents = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await fetch("http://localhost:8080/Eventi", {
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Errore nel recupero eventi");
      }

      const data = await response.json();
      console.log("EVENTI DAL BACKEND:", data);
      setEvents(data);
    } catch (error) {
      console.error("Errore fetch eventi:", error);
      setError("Errore nel caricamento degli eventi");
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);

  const handleEdit = (event) => {
    console.log("Modifica evento ID:", event.id);
  };

  const displayedEvents =
    activeTab === "created"
      ? createdEvents
      : activeTab === "booked"
      ? bookedEvents
      : historyEvents;

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

          <button
            onClick={() => setActiveTab("history")}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all ${
              activeTab === "history"
                ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/30"
                : "text-gray-500 hover:bg-gray-50"
            }`}
          >
            Storico ({historyEvents.length})
          </button>
        </div>
      </div>

      {/* Stato caricamento / errore */}
      {loading && (
        <div className="bg-white rounded-xl shadow-sm border border-gray-100 p-8 text-center">
          <p className="text-gray-500">Caricamento eventi...</p>
        </div>
      )}

      {!loading && error && (
        <div className="bg-white rounded-xl shadow-sm border border-red-200 p-8 text-center">
          <p className="text-red-500 font-medium">{error}</p>
        </div>
      )}

      {/* Grid Content */}
      {!loading && !error && (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {displayedEvents.map((event) => (
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
              onEdit={() => handleEdit(event)}
            />
          ))}

          {displayedEvents.length === 0 && (
            <div className="col-span-full bg-white rounded-xl shadow-sm border-2 border-dashed border-gray-200 p-12 text-center">
              <Calendar className="w-12 h-12 text-gray-300 mx-auto mb-4" />
              <p className="text-gray-500 font-medium">
                Nessun evento in questa categoria
              </p>
            </div>
          )}
        </div>
      )}

      {/* Modale CreaEvento */}
      {showCreateModal && (
        <ModalCreateEvent
          onClose={() => setShowCreateModal(false)}
          onSuccess={() => fetchEvents()}
        />
      )}
    </div>
  );
}