//EventCard
import { Calendar, Clock, MapPin, Users, Check, X, ShieldCheck, Pencil } from "lucide-react";

export default function EventCard({
  event,
  onBook,
  onCancelBooking,
  onEdit
}) {
  const currentParticipants = event.currentParticipants || 0;
  const isFull = currentParticipants >= event.maxParticipants;
  const status = event.status || "unknown";
  const isClosed = status === "closed";
  const eventStart = new Date(event.dateTime);
  const in24Hours = new Date(Date.now() + 24 * 60 * 60 * 1000);

  const canEdit = event.isCreatedByMe && eventStart > in24Hours;

  return (
    <div
      className={`bg-white rounded-lg shadow transition-shadow p-6 space-y-4 border border-gray-100 ${
        isClosed ? "opacity-60 pointer-events-none" : "hover:shadow-lg"
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
                  className="bg-blue-100 text-blue-800 text-[10px] px-2 py-0.5 rounded-full uppercase font-semibold"
                >
                  {tag}
                </span>
              ))}
          </div>
        </div>

        <p className="text-gray-600 text-sm line-clamp-2">
          {event.description || "No description provided."}
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
          Duration: <strong>{event.duration}h</strong>
        </span>

        <span
          className={`font-bold ${
            status === "open"
              ? "text-green-600"
              : status === "closed"
              ? "text-red-600"
              : "text-gray-400"
          }`}
        >
          ● {status.toUpperCase()}
        </span>
      </div>

      {/* Creator */}
      <div className="flex items-center gap-1 text-xs text-gray-400">
        <ShieldCheck className="w-3 h-3" />
        <span>
          Organized by: {event.creator?.name} {event.creator?.lastName}
        </span>
      </div>

      {/* Actions */}
      <div className="flex gap-2 pt-2">
        {event.isCreatedByMe ? (
          <div className="flex-1 flex gap-2">
            {/* Edit button */}
            <button
              disabled={!canEdit}
              onClick={canEdit ? () => onEdit(event.id) : undefined}
              className={`flex-1 py-2 rounded-lg text-sm font-bold border flex items-center justify-center gap-2 transition-colors ${
                !canEdit
                  ? "bg-gray-200 text-gray-400 border-gray-200 cursor-not-allowed opacity-60"
                  : "bg-blue-50 text-blue-700 hover:bg-blue-100 border-blue-200"
              }`}
              title={
                !canEdit
                  ? "You cannot edit an event within 24 hours of its start"
                  : ""
              }
            >
              <Pencil className="w-4 h-4" />
              EDIT
            </button>

            {/* Cancel/leave for creator */}
            <button
              disabled={isClosed}
              onClick={onCancelBooking}
              className={`flex-1 py-2 rounded-lg text-sm font-bold border flex items-center justify-center gap-2 transition-colors ${
                isClosed
                  ? "bg-gray-200 text-gray-400 border-gray-200 cursor-not-allowed"
                  : "bg-purple-50 text-purple-700 hover:bg-purple-100 border-purple-200"
              }`}
            >
              <X className="w-4 h-4" />
              {event.creatorParticipationRequired ? "CANCEL" : "LEAVE"}
            </button>
          </div>
        ) : event.isBooked ? (
          /* booked user */
          <button
            disabled={isClosed}
            onClick={onCancelBooking}
            className={`flex-1 py-2 rounded-lg text-sm font-bold transition-colors flex items-center justify-center gap-2 ${
              isClosed
                ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                : "bg-orange-100 text-orange-700 hover:bg-orange-200"
            }`}
          >
            <X className="w-4 h-4" />
            CANCEL BOOKING
          </button>
        ) : isFull ? (
          /* full event */
          <span className="flex-1 bg-gray-100 text-gray-400 py-2 rounded-lg text-sm font-bold text-center cursor-not-allowed">
            EVENT FULL
          </span>
        ) : (
          /* book button */
          <button
            disabled={isClosed}
            onClick={() => onBook(event.id)}
            className={`flex-1 py-2 rounded-lg text-sm font-bold transition-colors flex items-center justify-center gap-2 ${
              isClosed
                ? "bg-gray-200 text-gray-400 cursor-not-allowed"
                : "bg-blue-600 text-white hover:bg-blue-700"
            }`}
          >
            <Check className="w-4 h-4" />
            BOOK SEAT
          </button>
        )}
      </div>
    </div>
  );
}