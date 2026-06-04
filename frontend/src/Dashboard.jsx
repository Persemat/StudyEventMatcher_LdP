import { useState, useEffect, useCallback } from "react";
import { Calendar, Plus, Users } from "lucide-react";
import { useNavigate } from "react-router-dom";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventDetails from "./EventDetails";

export default function Dashboard({ user }) {
  const navigate = useNavigate();
  const [events, setEvents] = useState([]);
  const [stats, setStats] = useState({
    available: 0,
    booked: 0,
    created: 0,
  });
  const [loading, setLoading] = useState(true);
  const [statusMessage, setStatusMessage] = useState("");
  const [selectedEventId, setSelectedEventId] = useState(null);

  const fetchDashboardStats = useCallback(async () => {
    try {
      const response = await fetch("/api/events", {
        credentials: "include",
      });

      if (!response.ok) throw new Error("Errore fetch statistiche dashboard");

      const allEvents = await response.json();
      const now = Date.now();
      const futureEvents = Array.isArray(allEvents)
        ? allEvents.filter((event) => new Date(event.dateTime).getTime() >= now)
        : [];

      setStats({
        available: futureEvents.filter((event) => {
          const currentParticipants = event.currentParticipants || 0;
          const hasSpace =
            event.maxParticipants == null || currentParticipants < event.maxParticipants;

          return (
            event.status === "open" &&
            hasSpace &&
            !event.isBooked &&
            !event.isCreatedByMe
          );
        }).length,
        booked: futureEvents.filter((event) => event.isBooked && !event.isCreatedByMe).length,
        created: futureEvents.filter((event) => event.isCreatedByMe).length,
      });
    } catch (err) {
      console.error("Errore fetch statistiche dashboard:", err);
      setStats({ available: 0, booked: 0, created: 0 });
    }
  }, []);

  const fetchEvents = useCallback(async () => {
    
    if (!user) {
      setLoading(false);
      return;
    }
    
    if (!user?.favouriteCity) {
      setStatusMessage("Imposta una città preferita nel profilo per ricevere consigli.");
      setLoading(false);
      return;
    }

    const userCityCleaned = user.favouriteCity
      .replace(/\s*\(.*?\)\s*/g, "")
      .trim()
      .toLowerCase();

    let comuniData = [];
    try {
      const responseComuni = await fetch("/data/gi_comuni.json"); 
      comuniData = await responseComuni.json();
    } catch (e) {
      setStatusMessage("Errore nel caricamento del database dei comuni.");
      setLoading(false);
      return;
    }

    const comuneTrovato = comuniData.find(
      (c) => c.denominazione_ita.toLowerCase() === userCityCleaned
    );

    if (!comuneTrovato) {
      setStatusMessage("Impossibile localizzare la tua città preferita nel database dei comuni.");
      setLoading(false);
      return;
    }

    const lat = parseFloat(comuneTrovato.lat);
    const lon = parseFloat(comuneTrovato.lon);

    try {
      setLoading(true);

      const res = await fetch(`/api/events/recommended?latitude=${lat}&longitude=${lon}`, {
        method: "GET",
        credentials: "include", 
      });

      if (!res.ok) throw new Error("Errore dal server durante il fetch");

      const data = await res.json();

      
      if (data.messageCode === "SUCCESS") {
        setEvents(data.recommendedEvents);
        setStatusMessage(""); 
      } else if (data.messageCode === "NO_TAG_MATCH") {
        setEvents([]);
        setStatusMessage("Nessun evento corrispondente ai tuoi interessi, prova a modificarli o usa la funzione di ricerca generale.");
      } else if (data.messageCode === "NO_LOCATION_MATCH") {
        setEvents([]);
        setStatusMessage("Nessun evento corrispondente ai tuoi interessi nella tua zona.");
      } else if (data.messageCode === "MISSING_COORDINATES") {
        setEvents([]);
        setStatusMessage("Errore nel passaggio delle coordinate al server.");
      }
    } catch (err) {
      console.error("Errore fetch dashboard:", err);
      setEvents([]);
      setStatusMessage("Si è verificato un errore di rete. Riprova più tardi.");
    } finally {
      setLoading(false);
    }
  }, [user]); 

  useEffect(() => {
    fetchDashboardStats();
    fetchEvents();
  }, [fetchDashboardStats, fetchEvents]);

  const { handleBook, handleCancelBooking, handleToggleFavorite } = useEventActions(fetchEvents, setEvents);

  const handleEdit = (event) => {
    console.log("Aprendo modale di modifica eventi:", event.id);
  };

  if (loading) {
    return (
      <div className="p-8 text-center text-gray-500 italic">
        Caricamento in corso...
      </div>
    );
  }

  return (
    <div className="space-y-8">
      {/* Header */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">
          Benvenuto {user?.name}! Scopri gli eventi suggeriti per te!
        </p>
      </div>

      {/* Summary cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <button
          type="button"
          onClick={() => navigate("/events")}
          className="bg-white rounded-lg shadow p-6 border border-gray-100 text-left transition-all hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-blue-200"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Eventi Disponibili</p>
              <p className="text-3xl font-bold text-gray-900">{stats.available}</p>
            </div>
            <div className="bg-blue-100 rounded-full p-3">
              <Calendar className="w-6 h-6 text-blue-600" />
            </div>
          </div>
        </button>

        <button
          type="button"
          onClick={() => navigate("/my-events?tab=booked")}
          className="bg-white rounded-lg shadow p-6 border border-gray-100 text-left transition-all hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-green-200"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Eventi Prenotati</p>
              <p className="text-3xl font-bold text-gray-900">{stats.booked}</p>
            </div>
            <div className="bg-green-100 rounded-full p-3">
              <Users className="w-6 h-6 text-green-600" />
            </div>
          </div>
        </button>

        <button
          type="button"
          onClick={() => navigate("/my-events?tab=created")}
          className="bg-white rounded-lg shadow p-6 border border-gray-100 text-left transition-all hover:-translate-y-0.5 hover:shadow-lg focus:outline-none focus:ring-2 focus:ring-purple-200"
        >
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Eventi Creati</p>
              <p className="text-3xl font-bold text-gray-900">{stats.created}</p>
            </div>
            <div className="bg-purple-100 rounded-full p-3">
              <Plus className="w-6 h-6 text-purple-600" />
            </div>
          </div>
        </button>
      </div>

      {/* Recommended events */}
      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Eventi suggeriti</h2>

        {events.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <EventCard
                key={event.id}
                event={event}
                onShowDetails={() => setSelectedEventId(event.id)}
                onBook={() => handleBook(event.id)}
                onCancelBooking={() =>
                  handleCancelBooking(
                    event.id,
                    event.creatorParticipationRequired
                  )
                }
                onToggleFavorite={() => handleToggleFavorite(event.id)}
                onEdit={() => handleEdit(event)}
              />
            ))}
          </div>
        ) : (
          <div className="bg-white rounded-lg shadow p-12 text-center border-2 border-dashed border-gray-200">
            <Calendar className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-500 mb-2">
              Non ci sono eventi da mostrare al momento
            </h3>
            {/* Shows the specific status returned by the backend. */}
            <p className="text-gray-500">
              {statusMessage}
            </p>
          </div>
        )}
      </div>

        {selectedEventId && (
          <div
            className="fixed inset-0 z-50 flex items-center justify-center"
            onClick={() => setSelectedEventId(null)}
          >
             <div
              className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto relative border-2 border-gray-200"
              onClick={(e) => e.stopPropagation()}
             >
                
                <button 
                   onClick={() => setSelectedEventId(null)}
                   className="absolute top-4 right-4 text-gray-500 hover:text-gray-800 font-bold text-xl"
                >
                   ✕
                </button>
                
                <EventDetails eventId={selectedEventId} />
      
             </div>
          </div>
        )}

    </div>
  );
}
