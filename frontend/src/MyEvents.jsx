import React, { useState, useEffect } from "react";
import { Calendar, Plus } from "lucide-react";
import { useEventActions } from "./hooks/useEventActions";
import EventCard from "./EventCard"; 
import EventFormModal from "./EventFormModal"

export default function MyEvents() {
  const [events, setEvents] = useState([]);
  const [activeTab, setActiveTab] = useState("created"); // Rimosso il tipo TS
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);
  // Filtriamo gli eventi in base al flag
  const createdEvents = events.filter((e) => e.isCreatedByMe);
  const bookedEvents = events.filter((e) => e.isBooked);

  const fetchEvents = async () => {
  setLoading(true);
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
    setEvents([]);
  } finally {
    setLoading(false);
  }
};

  useEffect(() => {
    fetchEvents();
  }, []);

  //Passi la funzione all'hook così lui può richiamarla dopo un'azione (es. dopo prenota)
  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);

  const handleEdit = (event) => {
    setEventToEdit(event);
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
        <EventFormModal
          mode="create"
          onClose={() => setShowCreateModal(false)}
          onSuccess={() => {
            setShowCreateModal(false);
            fetchEvents();
          }}
        />
      )}

      {/* Modale ModificaEvento */}
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