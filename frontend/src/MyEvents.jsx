import React, { useState, useEffect, useMemo } from "react";
import { AlertTriangle, Calendar, Plus } from "lucide-react";
import { useEventActions } from "./hooks/useEventActions";

import EventCard from "./EventCard"; 
import EventFormModal from "./EventFormModal";
import EventDetails from "./EventDetails";

export default function MyEvents() {
  const [events, setEvents] = useState([]);
  const [activeTab, setActiveTab] = useState("created");
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);
  const [selectedEventId, setSelectedEventId] = useState(null);

  const now = new Date();
  const nowTime = now.getTime();

  // Splits future and past events relative to the current time.
  const isFutureEvent = (event) => new Date(event.dateTime).getTime() >= nowTime;
  const isPastEvent = (event) => new Date(event.dateTime).getTime() < nowTime;

  // Shows only future events created by the current user.
  const createdEvents = events.filter(
    (e) => e.isCreatedByMe && isFutureEvent(e)
  );

  // Shows only future events booked by the user but created by someone else.
  const bookedEvents = events.filter(
    (e) => e.isBooked && !e.isCreatedByMe && isFutureEvent(e)
  );

  // History includes past events created or booked by the user.
  const historyEvents = events.filter(
    (e) => (e.isCreatedByMe || e.isBooked) && isPastEvent(e)
  );


  const favoriteEvents = events.filter(
    (e) => e.isFavorite
  );
  const overlappingEvents = useMemo(() => {
    const userFutureEvents = events
      .filter((event) => (event.isCreatedByMe || event.isBooked) && new Date(event.dateTime).getTime() >= nowTime)
      .sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime));

    const overlappingEventIds = new Set();
    const overlappingEventTitles = [];

    const addOverlappingEvent = (event) => {
      if (overlappingEventIds.has(event.id)) return;
      overlappingEventIds.add(event.id);
      overlappingEventTitles.push(event.title);
    };

    for (let i = 0; i < userFutureEvents.length; i += 1) {
      for (let j = i + 1; j < userFutureEvents.length; j += 1) {
        const first = userFutureEvents[i];
        const second = userFutureEvents[j];
        const firstStart = new Date(first.dateTime).getTime();
        const firstEnd = firstStart + Math.round((first.duration || 0) * 60) * 60 * 1000;
        const secondStart = new Date(second.dateTime).getTime();
        const secondEnd = secondStart + Math.round((second.duration || 0) * 60) * 60 * 1000;

        if (firstStart < secondEnd && firstEnd > secondStart) {
          addOverlappingEvent(first);
          addOverlappingEvent(second);
        }
      }
    }

    return overlappingEventTitles;
  }, [events, nowTime]);

  const formatOverlappingEventTitles = (titles) => {
    if (titles.length <= 2) return titles.join(" e ");

    return `${titles.slice(0, -1).join(", ")} e ${titles[titles.length - 1]}`;
  };

  // Reloads all visible events and refreshes the three sections.
  const fetchEvents = async () => {
    setLoading(true);
    setError("");

    try {
      const response = await fetch("/api/events", {
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Errore fetch eventi");
      }

      const data = await response.json();
      console.log("EVENTI DAL BACKEND:", data);
      setEvents(data);
    } catch (error) {
      console.error("Errore fetch eventi", error);
      setError("Errore caricamento eventi");
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  const { handleBook, handleCancelBooking, handleToggleFavorite } = useEventActions(fetchEvents, setEvents);

  // Opens the modal in edit mode with the selected event data.
  const handleEdit = (event) => {
    setEventToEdit(event);
  };

  // Chooses which event list to render based on the active tab.
 const displayedEvents =
  activeTab === "created"
    ? createdEvents
    : activeTab === "favorites"
    ? favoriteEvents
    : activeTab === "booked"
    ? bookedEvents
    : historyEvents;

  return (
    <div className="p-6 space-y-6">
      {/* Header */}
      <div className="flex justify-between items-center">
        <div>
          <h1 className="text-3xl font-bold text-gray-900">I Miei Eventi</h1>
          <p className="text-gray-600 mt-1">
            Organizza i tuoi studi
          </p>
        </div>

        <button
          onClick={() => setShowCreateModal(true)}
          className="bg-blue-600 text-white px-6 py-3 rounded-lg font-medium hover:bg-blue-700 transition-colors flex 
          items-center gap-2"
        >
          <Plus className="w-5 h-5" /> Crea Evento
        </button>
      </div>

      {overlappingEvents.length > 0 && (
        <div className="bg-yellow-50 border border-yellow-200 text-yellow-900 rounded-xl px-4 py-3 flex items-start gap-3">
          <AlertTriangle className="w-5 h-5 text-yellow-600 mt-0.5 shrink-0" />
          <div>
            <p className="font-semibold">
              Attenzione: hai {overlappingEvents.length} eventi con orari sovrapposti.
            </p>
            <p className="text-sm mt-1">
              Controlla: {formatOverlappingEventTitles(overlappingEvents) }.
            </p>
          </div>
        </div>
      )}

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
            onClick={() => setActiveTab("favorites")}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all ${
              activeTab === "favorites"
                ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/30"
                : "text-gray-500 hover:bg-gray-50"
            }`}
          >
            I miei preferiti ({favoriteEvents.length})
          </button>


          <button
            onClick={() => setActiveTab("booked")}
            className={`flex-1 px-6 py-4 font-bold text-sm transition-all ${
              activeTab === "booked"
                ? "text-blue-600 border-b-2 border-blue-600 bg-blue-50/30"
                : "text-gray-500 hover:bg-gray-50"
            }`}
          >
            Eventi a cui partecipo ({bookedEvents.length})
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
                  event.creatorParticipationRequired,
                  event.isCreatedByMe
                )}
              onEdit={() => handleEdit(event)}
              onToggleFavorite={() => handleToggleFavorite(event.id)}
              onShowDetails={() => setSelectedEventId(event.id)}
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

      {/* Create Event Modal */}
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

      {/* Edit Event Modal */}
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

      {selectedEventId && (
        <div
          className="fixed inset-0 z-50 flex items-center justify-center p-4"
          onClick={() => setSelectedEventId(null)}
        >
           
           <div
            className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto relative border-2 border-gray-200"
            onClick={(e) => e.stopPropagation()}
           >
              
              {/* Close Button */}
              <button 
                 onClick={() => setSelectedEventId(null)}
                 className="absolute top-4 right-4 text-gray-400 hover:text-gray-800 transition-colors font-bold text-xl z-10 bg-white rounded-full p-1 shadow-sm"
              >
                 ✕
              </button>
  
              {/* Render the details component */}
              <EventDetails eventId={selectedEventId} />
  
           </div>
        </div>
      )}

    </div>
  );
}
