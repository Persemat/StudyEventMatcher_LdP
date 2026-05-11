import { useState, useEffect, useCallback } from "react";
import { Calendar } from "lucide-react";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";

export default function Dashboard({ user }) {
  const [events, setEvents] = useState([]);
  const [loading, setLoading] = useState(true);

  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      
      await new Promise((resolve) => setTimeout(resolve, 500));
      
      // Quando avremo il backend pronto:
      // const res = await fetch('/api/events/recommended', { credentials: 'include' });
      // const data = await res.json();
      // setEvents(data);
      
      setEvents([]); 
    } catch (err) {
      console.error("Errore fetch dashboard:", err);
    } finally {
      setLoading(false);
    }
  }, []);

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
        Il backend sta generando le tue raccomandazioni personali...
      </div>
    );
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">
          Benvenuto {user?.name}! Scopri gli eventi suggeriti per te!
        </p>
      </div>

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

      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-4">
          Eventi suggeriti
        </h2>

        {events.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <EventCard
                key={event.id}
                event={event}
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
            <h3 className="text-lg font-semibold text-gray-500">
              Non ci sono ancora eventi suggeriti per te!
            </h3>
            <p className="text-gray-400 mt-2">
              Presto vedrai eventi suggeriti per te qui!
            </p>
          </div>
        )}
      </div>
    </div>
  );
}