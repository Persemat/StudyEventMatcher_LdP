import { useState, useEffect } from "react";
import { Calendar, Users, Plus } from "lucide-react";
import EventCard from "./EventCard"; 
import { useEventActions } from "./hooks/useEventActions";

export default function Dashboard({user}) {
  const [events, setEvents] = useState([]);
  const [loggedUser, setLoggedUser] = useState(null);
  const [loading, setLoading] = useState(true);


  const fetchEvents = async () => {
      setLoading(true);
      // Qui andrà la tua fetch complessa: fetch('/api/eventi/consigliati', { credentials: 'include' })
      // Per ora simuliamo che la funzione sia in arrivo
      setLoading(false);
    };

useEffect(() => {
    fetchEvents();
  }, []);

//Passi la funzione all'hook così lui può richiamarla dopo un'azione (es. dopo prenota)
const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);


  // 3) Modifica evento
  const handleEdit = (event) => {
    console.log("Apertura modale di modifica per l'evento:", event.id);
    // TO-DO: implementare apertura del modale di modifica
    //        passargli l'evento e le funzioni "onClose" e "onSuccess"
  };

  

  // Gestione stato di caricamento per evitare il "flash" della lista vuota
  if (loading) {
    return <div className="p-8 text-center text-gray-500 italic">Il Backend sta elaborando i tuoi consigli personalizzati...</div>;
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">
          Benvenuto {user?.nome}! Scopri gli eventi di studio consigliati per te
        </p>
      </div>

      {/* Stats Cards: Riflettono direttamente la lunghezza della lista dal backend */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Consigliati per te</p>
              <p className="text-3xl font-bold text-gray-900">{events.length}</p>
            </div>
            <div className="bg-blue-100 rounded-full p-3">
              <Calendar className="w-6 h-6 text-blue-600" />
            </div>
          </div>
        </div>
        {/* Altre stats che popolerai quando avrai i dati reali */}
      </div>

      <div>
        <h2 className="text-2xl font-bold text-gray-900 mb-4">Eventi Consigliati</h2>
        
        {events.length > 0 ? (
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
            {events.map((event) => (
              <EventCard
                key={event.id} 
                event={event} 
                // Passiamo i riferimenti alle funzioni definite sopra
                onBook={() => handleBook(event.id)}
                onCancelBooking={() => handleCancelBooking(event.id, event.partecipazioneCreatoreNecessaria)}
                onEdit={() => handleEdit(event)}
              />
            ))}
          </div>
        ) : (
          /* Messaggio Empty State */
          <div className="bg-white rounded-lg shadow p-12 text-center border-2 border-dashed border-gray-200">
            <Calendar className="w-16 h-16 text-gray-300 mx-auto mb-4" />
            <h3 className="text-lg font-semibold text-gray-500">
              Non ci sono eventi consigliati al momento, funzionalità in arrivo!
            </h3>
            <p className="text-gray-400 mt-2">
              Presto, in questa sezione, vedrai eventi basati sulle tue preferenze!
            </p>
          </div>
        )}
      </div>
    </div>
  );
}