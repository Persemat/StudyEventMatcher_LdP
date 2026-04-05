import { Calendar, Clock, MapPin, Users, Check, X, ShieldCheck } from "lucide-react";

export default function EventCard({
  event, // Questo oggetto arriva dal tuo List<Evento> di Spring
  onBook,
  onCancelBooking,
  onEdit
}) {
  // Calcolo partecipanti (assumendo che il backend ti mandi una lista o un conteggio)
  // Se il backend non manda 'currentParticipants', per ora mettiamo 0
  const partecipantiAttuali = event.currentParticipants || 0;
  const isPieno = partecipantiAttuali >= event.maxPartecipanti;

  return (
    <div className="bg-white rounded-lg shadow hover:shadow-lg transition-shadow p-6 space-y-4 border border-gray-100">
      
      {/* 1. Titolo e Tag (Dalla List<String> tag) */}
      <div>
        <div className="flex items-start justify-between mb-2">
          <h3 className="font-bold text-lg text-gray-900">{event.titolo}</h3>
          <div className="flex gap-1">
            {event.tag && event.tag.map((t, index) => (
              <span key={index} className="bg-blue-100 text-blue-800 text-[10px] px-2 py-0.5 rounded-full uppercase font-semibold">
                {t}
              </span>
            ))}
          </div>
        </div>
        <p className="text-gray-600 text-sm line-clamp-2">
          {event.descrizione || "Nessuna descrizione fornita."}
        </p>
      </div>

      {/* 2. Dettagli (LocalDateTime dataOra, Double durata, String luogo) */}
      <div className="grid grid-cols-2 gap-y-3 gap-x-2">
        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Calendar className="w-4 h-4 text-blue-500" />
          <span>{new Date(event.dataOra).toLocaleDateString("it-IT")}</span>
        </div>

        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Clock className="w-4 h-4 text-blue-500" />
          <span>{new Date(event.dataOra).toLocaleTimeString("it-IT", {hour: '2-digit', minute:'2-digit'})}</span>
        </div>

        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <MapPin className="w-4 h-4 text-red-500" />
          <span className="line-clamp-1">{event.luogo}</span>
        </div>

        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Users className="w-4 h-4 text-green-500" />
          <span>{partecipantiAttuali}/{event.maxPartecipanti}</span>
        </div>
      </div>

      {/* 3. Info Extra (Durata e Stato) */}
      <div className="flex justify-between items-center text-xs text-gray-500 border-t pt-3">
        <span>Durata: <strong>{event.durata}h</strong></span>
        <span className={`font-bold ${event.stato === 'aperto' ? 'text-green-600' : 'text-red-600'}`}>
          ● {event.stato.toUpperCase()}
        </span>
      </div>

      {/* 4. Creatore (Dall'oggetto Utente creatore) */}
      <div className="flex items-center gap-1 text-xs text-gray-400">
        <ShieldCheck className="w-3 h-3" />
        <span>Organizzato da: {event.creatore?.nome} {event.creatore?.cognome}</span>
      </div>

      {/* 5. Azioni (Usando isCreatedByMe e isBooked arrivati dal BE) */}
      <div className="flex gap-2 pt-2">
        {event.isCreatedByMe ? (
          <div className="flex-1 flex gap-2">
            {/* Tasto Modifica */}
            <button 
              onClick={() => onEdit(event.id)} 
              className="flex-1 bg-blue-50 text-blue-700 py-2 rounded-lg text-sm font-bold hover:bg-blue-100 border border-blue-200 flex items-center justify-center gap-2 transition-colors"
            >
              <Pencil className="w-4 h-4" />
              MODIFICA
            </button>

            {/* Tasto Annulla/Abbandona per il Creatore */}
            <button 
              onClick={() => onCancelBooking(event.id)} 
              className="flex-1 bg-purple-50 text-purple-700 py-2 rounded-lg text-sm font-bold hover:bg-purple-100 border border-purple-200 flex items-center justify-center gap-2 transition-colors"
            >
              <X className="w-4 h-4" />
              {event.partecipazioneCreatoreNecessaria ? "ANNULLA" : "ABBANDONA"}
            </button>
          </div>
        ) : event.isBooked ? (
          /* Tasto per l'utente prenotato */
          <button 
            onClick={() => onCancelBooking(event.id)} 
            className="flex-1 bg-orange-100 text-orange-700 py-2 rounded-lg text-sm font-bold hover:bg-orange-200 transition-colors flex items-center justify-center gap-2"
          >
            <X className="w-4 h-4" />
            ANNULLA PRENOTAZIONE
          </button>
        ) : isPieno ? (
          /* Stato evento completo */
          <span className="flex-1 bg-gray-100 text-gray-400 py-2 rounded-lg text-sm font-bold text-center cursor-not-allowed">
            EVENTO COMPLETO
          </span>
        ) : (
          /* Tasto prenotazione standard */
          <button 
            onClick={() => onBook(event.id)} 
            className="flex-1 bg-blue-600 text-white py-2 rounded-lg text-sm font-bold hover:bg-blue-700 transition-colors flex items-center justify-center gap-2"
          >
            <Check className="w-4 h-4" />
            PRENOTA POSTO
          </button>
        )}
      </div>
    </div>
  );
}