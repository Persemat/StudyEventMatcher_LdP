import React, { useState } from "react";
import { Calendar, Clock, MapPin, Users, Check, X, ShieldCheck, Pencil } from "lucide-react";
import { formatTagLabel } from "./catalogs";

export default function EventCard({
  event,
  onBook,
  onCancelBooking,
  onEdit
}) {
  const currentParticipants = event.currentParticipants || 0;
  const isFull = event.maxParticipants != null && currentParticipants >= event.maxParticipants;
  const status = event.status || "unknown";
  const isClosed = status === "closed" || isFull;
  const eventStart = new Date(event.dateTime);

  const [now] = useState(Date.now);
  const in24Hours = new Date(now + 24 * 60 * 60 * 1000);

  // Blocca solo chi NON è creatore e NON è prenotato.
  // Così se l'evento è pieno/chiuso, chi è già prenotato può comunque cancellare.
  const isInterationBlocked = isClosed && !event.isCreatedByMe && !event.isBooked;

  const canEdit = event.isCreatedByMe && eventStart > in24Hours;

  return (
    <div
      className={`bg-white rounded-lg shadow transition-all p-6 space-y-4 border ${
        isClosed ? "border-gray-200 bg-gray-50 opacity-75" : "border-gray-100 hover:shadow-lg"
      } ${
        isInterationBlocked ? "pointer-events-none select-none grayscale" : "pointer-events-auto"
      }`}
    >
      {/* Title and Tags */}
      <div>
        <div className="flex items-start justify-between mb-2">
          <h3 className="font-bold text-lg text-gray-900">{event.title}</h3>

          <div className="flex gap-1 flex-wrap justify-end">
            {event.tags &&
              event.tags.map((tag, index) => (
                <span
                  key={index}
                  className="bg-blue-100 text-blue-800 text-[10px] px-2 py-0.5 rounded-full font-semibold"
                >
                  {formatTagLabel(tag)}
                </span>
              ))}
          </div>
        </div>

        <p className="text-gray-600 text-sm line-clamp-2">
          {event.description || "Nessuna descrizione"}
        </p>
      </div>

      {/* Event details */}
      <div className="grid grid-cols-2 gap-y-3 gap-x-2">
        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Calendar className="w-4 h-4 text-blue-500" />
          <span>{new Date(event.dateTime).toLocaleDateString("en-GB")}</span>
        </div>

        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Clock className="w-4 h-4 text-blue-500" />
          <span>
            {new Date(event.dateTime).toLocaleTimeString("en-GB", {
              hour: "2-digit",
              minute: "2-digit",
            })}
          </span>
        </div>

        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <MapPin className="w-4 h-4 text-red-500" />
          <span className="line-clamp-1">{event.location}</span>
        </div>

        <div className="flex items-center gap-2 text-gray-600 text-sm">
          <Users className="w-4 h-4 text-green-500" />
          <span>
            {currentParticipants}/{event.maxParticipants}
          </span>
        </div>
      </div>

      {/* Extra info */}
      <div className="flex justify-between items-center text-xs text-gray-500 border-t pt-3">
        <span>
          Durata: <strong>{event.duration}h</strong>
        </span>

        <span
          className={`font-bold ${
            isClosed
              ? "text-red-600"
              : status === "open"
              ? "text-green-600"
              : "text-gray-400"
          }`}
        >
          ● {isClosed
              ? "CHIUSO"
              : status === "open"
              ? "APERTO"
              : "SCONOSCIUTO"}
        </span>
      </div>

      {/* Creator */}
      <div className="flex items-center gap-1 text-xs text-gray-400">
        <ShieldCheck className="w-3 h-3" />
        <span>
          Organizzata da: {event.creator?.name} {event.creator?.lastName}
        </span>
      </div>

      {/* Actions */}
      <div className="flex gap-2 pt-2">
        {event.isCreatedByMe ? (
          <div className="flex-1 flex gap-2">
            {/* Edit button */}
            <button
              disabled={!canEdit}
              onClick={() => onEdit(event.id)}
              className={`flex-1 py-2 rounded-lg text-sm font-bold border flex items-center justify-center gap-2 transition-colors ${
                !canEdit
                  ? "bg-gray-200 text-gray-400 border-gray-200 cursor-not-allowed"
                  : "bg-blue-50 text-blue-700 hover:bg-blue-100 border-blue-200"
              }`}
              title={
                !canEdit
                  ? "Non puoi modificare gli eventi entro 24h dall'inizio"
                  : ""
              }
            >
              <Pencil className="w-4 h-4" /> MODIFICA
            </button>

            {/* Cancel/leave for creator */}
            <button
              onClick={onCancelBooking}
              className="flex-1 py-2 rounded-lg text-sm font-bold border flex items-center justify-center gap-2 transition-colors bg-purple-50 text-purple-700 hover:bg-purple-100 border-purple-200"
            >
              <X className="w-4 h-4" />
              {event.creatorParticipationRequired ? "CANCELLA" : "ABBANDONA"}
            </button>
          </div>
        ) : event.isBooked ? (
          /* booked user */
          <button
            onClick={onCancelBooking}
            className="flex-1 py-2 rounded-lg text-sm font-bold transition-colors flex items-center justify-center gap-2 bg-orange-100 text-orange-700 hover:bg-orange-200"
          >
            <X className="w-4 h-4" />
            CANCELLA PRENOTAZIONE
          </button>
        ) : isClosed ? (
          /* full/closed event */
          <span className="flex-1 bg-gray-100 text-gray-400 py-2 rounded-lg text-sm font-bold flex items-center justify-center cursor-not-allowed">
            {isFull ? "EVENTO AL COMPLETO" : "CHIUSO"}
          </span>
        ) : (
          /* book button */
          <button
            onClick={() => onBook(event.id)}
            className="flex-1 py-2 rounded-lg text-sm font-bold transition-colors flex items-center justify-center gap-2 bg-blue-600 text-white hover:bg-blue-700"
          >
            <Check className="w-4 h-4" />
            PRENOTA
          </button>
        )}
      </div>
    </div>
  );
}
