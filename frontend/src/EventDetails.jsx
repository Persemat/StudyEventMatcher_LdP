import React, { useState, useEffect, useCallback } from 'react';
import { Calendar, MapPin, Users, Info, Tag } from 'lucide-react';
import { useEventActions } from './hooks/useEventActions';

export default function EventDetails({ eventId }) {
  
  const [event, setEvent] = useState(null); 
  const [loading, setLoading] = useState(true); 
  const [error, setError] = useState(""); 

    
    const fetchEventDetails = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const response = await fetch(`/api/events/${eventId}`, {
        method: 'GET',
        credentials: 'include',
      });

      if (!response.ok) throw new Error("Impossibile caricare i dettagli.");
      
      const data = await response.json();
      setEvent(data);
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  }, [eventId]);

  
  const { handleBook, handleCancelBooking, isProcessing } = useEventActions(fetchEventDetails);

  useEffect(() => {
    if (eventId) fetchEventDetails();
  }, [eventId, fetchEventDetails]);

  if (loading && !event) return <div className="p-8 text-center text-gray-500">Caricamento...</div>;
  if (error) return <div className="p-8 text-center text-red-500 bg-red-50">{error}</div>;
  if (!event) return null;

  const availableSeats = event.maxParticipants - event.currentParticipants;
  const shouldDeleteEvent = event.isCreatedByMe && event.creatorParticipationRequired;
  const formattedDate = new Date(event.dateTime).toLocaleString('it-IT', {
    weekday: 'long', year: 'numeric', month: 'long', day: 'numeric',
    hour: '2-digit', minute: '2-digit'
  });

  return (
    <div className="p-6 space-y-6 bg-white">
      {/* Title and Status */}
      <div className="border-b pb-4">
        <h2 className="text-3xl font-bold text-gray-900 mb-2">{event.title}</h2>
        <span className={`px-3 py-1 rounded-full text-sm font-semibold ${
          event.status === 'open' ? 'bg-green-100 text-green-800' : 'bg-red-100 text-red-800'
        }`}>
          {event.status === 'open' ? 'Aperto' : 'Chiuso'}
        </span>
      </div>

      {/* Grid with main information */}
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 text-gray-700">
        <div className="flex items-center gap-3">
          <Calendar className="text-blue-500" size={20} />
          <span>{formattedDate} (Durata: {event.duration}h)</span>
        </div>
        
        <div className="flex items-center gap-3">
          <MapPin className="text-red-500" size={20} />
          <span>{event.location}</span>
        </div>

        <div className="flex items-center gap-3">
          <Users className="text-green-500" size={20} />
          <span>
            {availableSeats} posti disponibili su {event.maxParticipants}
          </span>
        </div>

        {event.tags && event.tags.length > 0 && (
          <div className="flex items-start gap-3">
            <Tag className="text-blue-500 mt-1" size={20} />
            <div className="flex flex-wrap gap-2">
              {event.tags.map((tag, index) => (
                <span 
                  key={index} 
                  className="bg-blue-50 text-blue-700 px-2 py-1 rounded-md text-sm font-medium border border-blue-100"
                >
                  {tag.macroTag} {tag.microTag ? `> ${tag.microTag}` : ''}
                </span>
              ))}
            </div>
          </div>
        )}
      </div>

      {/* Description section */}
      <div className="bg-gray-50 p-4 rounded-lg border border-gray-100">
        <div className="flex items-center gap-2 mb-2">
          <Info className="text-gray-500" size={18} />
          <h3 className="font-semibold text-gray-900">Descrizione dell'evento</h3>
        </div>
        <p className="text-gray-600 whitespace-pre-line break-words">
          {event.description}
        </p>
      </div>

      {/* Participants list*/}
      <div className="bg-white p-4 rounded-lg border border-gray-100 mt-4">
        <div className="flex items-center gap-2 mb-3 border-b pb-2">
          <Users className="text-gray-500" size={18} />
          <h3 className="font-semibold text-gray-900">
            Elenco Partecipanti ({event.currentParticipants})
          </h3>
        </div>
        
        {/* Number of participants check*/}
        {event.participants && event.participants.length > 0 ? (
          <ul className="list-decimal list-inside text-gray-700 space-y-1">
            {event.participants.map((participant) => (
              <li key={participant.id} className="ml-2">
                <span className="font-medium">{participant.name} {participant.lastName}</span>
                <span className="text-gray-500 text-sm ml-2">- {participant.email}</span>
              </li>
            ))}
          </ul>
        ) : (
          <p className="text-gray-500 italic ml-2">Nessuno si è ancora prenotato. Sii il primo!</p>
        )}
      </div>

     {/* Booking section */}
      <div className="mt-6 pt-4 border-t flex flex-col md:flex-row justify-between items-center gap-4">
        <div className="text-sm text-gray-500">
          Organizzato da: <span className="font-semibold text-gray-700">{event.creator.name} {event.creator.lastName}</span>
        </div>

        <div className="w-full md:w-auto">
          {event.isBooked ? (
            <div className="flex flex-col items-end gap-2">
              <p className="text-green-600 font-bold">✅ Sei prenotato</p>
              <button
                disabled={isProcessing}
                onClick={() => handleCancelBooking(event.id, event.creatorParticipationRequired, event.isCreatedByMe)}
                className={`w-full md:w-auto px-4 py-2 rounded-lg font-bold disabled:opacity-50 ${
                  shouldDeleteEvent
                    ? "bg-red-100 text-red-700 hover:bg-red-200"
                    : "bg-orange-100 text-orange-700 hover:bg-orange-200"
                }`}
              >
                {isProcessing ? "ELABORAZIONE..." : (shouldDeleteEvent ? "CANCELLA EVENTO" : "CANCELLA PRENOTAZIONE")}
              </button>
            </div>
          ) : (
            <div className="flex flex-col items-end gap-2">
              <p className="text-gray-500 italic">Non sei prenotato</p>
              <button
                disabled={isProcessing || event.status !== 'open'}
                onClick={() => handleBook(event.id)}
                className={`w-full md:w-auto px-6 py-2 rounded-lg font-bold text-white transition-colors ${
                  event.status === 'open' 
                    ? 'bg-blue-600 hover:bg-blue-700' 
                    : 'bg-gray-400 cursor-not-allowed'
                } disabled:opacity-50`}
              >
                {isProcessing ? "ELABORAZIONE..." : (event.status === 'open' ? 'PRENOTATI ORA' : 'EVENTO CHIUSO')}
              </button>
            </div>
          )}
        </div>
      </div>
    </div>
  );
}
