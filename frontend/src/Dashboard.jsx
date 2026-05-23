import { useState, useEffect, useCallback } from "react";
import { Calendar } from "lucide-react";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventDetails from "./EventDetails";

export default function Dashboard({ user }) {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);
  const [statusMessage, setStatusMessage] = useState("");
  const userCityCleaned = user.favouriteCity
      .replace(/\s*\(.*?\)\s*/g, '') 
      .trim()
      .toLowerCase();
  const [selectedEventId, setSelectedEventId] = useState(null);

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

    let comuniData = [];
    try {
      const responseComuni = await fetch("/data/gi_comuni.json"); 
      comuniData = await responseComuni.json();
    } catch (e) {
      setStatusMessage("Errore nel caricamento del database dei comuni.");
      setLoading(false);
      return;
    }

    // 2. Cerchiamo la città nel JSON per ottenere le coordinate

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
        credentials: "include", // Fondamentale per far leggere la sessione a Spring Boot
      });

      if (!res.ok) throw new Error("Errore dal server durante il fetch");

      const data = await res.json();

      // 4. Gestione degli scenari di Accettazione tramite il messageCode
      if (data.messageCode === "SUCCESS") {
        setEvents(data.recommendedEvents);
        setStatusMessage(""); // Nessun errore, mostriamo le card
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
  }, [user]); // Dipendenza aggiornata: si ricarica se cambia l'utente

  useEffect(() => {
    fetchEvents();
  }, [fetchEvents]);

  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);

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
      {/* Intestazione */}
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">
          Benvenuto {user?.name}! Scopri gli eventi suggeriti per te!
        </p>
      </div>

      {/* Widget statistiche in alto */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Suggeriti per te</p>
              <p className="text-3xl font-bold text-gray-900">{events.length}</p>
            </div>
            <div className="bg-blue-100 rounded-full p-3">
              <Calendar className="w-6 h-6 text-blue-600" />
            </div>
          </div>
        </div>
      </div>

      {/* Sezione Lista Eventi */}
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
            {/* Stampiamo a schermo il messaggio specifico restituito dal Backend */}
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