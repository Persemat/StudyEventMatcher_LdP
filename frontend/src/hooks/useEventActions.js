//useEventActions.js
import { useState } from 'react';

export const useEventActions = (refreshCallback, setEvents) => {
  const [isProcessing, setIsProcessing] = useState(false);

  // Action: BOOK
  const handleBook = async (eventId) => {
    setIsProcessing(true);
    try {
      const response = await fetch(`/api/events/${eventId}/book`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        }
      });

      if (!response.ok) {
        throw new Error("Errore nel prenotare l'evento");
      }

      console.log("✅ Prenotazione avvenuta con successo!");

      // refresh events list
      refreshCallback?.();

    } catch (error) {
      console.error("❌ Errore:", error);
      alert("Errore durante la prenotazione");
    } finally {
      setIsProcessing(false);
    }
  };

  // Action: CANCEL / LEAVE EVENT
  const handleCancelBooking = async (eventId, isCreatorRequired, isCreatedByMe) => {

    const message = isCreatedByMe
      ? (isCreatorRequired
          ? "Sei il creatore e la tua presenza è obbligatoria. L’INTERO evento verrà eliminato. Confermi?"
          : "Sei il creatore. Verrai rimosso dall’evento. Se ci sono altri partecipanti, il ruolo di creatore verrà riassegnato. Confermi?")
      : "Vuoi davvero annullare la tua partecipazione?";

    if (!window.confirm(message)) return;

    setIsProcessing(true);

    try {
      let response;

      if (isCreatedByMe) {
        response = await fetch(`/api/events/${eventId}`, {
          method: "DELETE",
          credentials: "include",
        });
      } else {
        response = await fetch(`/api/events/${eventId}/book`, {
          method: "DELETE",
          credentials: "include",
        });
      }

      const text = await response.text();

      if (!response.ok) {
        throw new Error(text || "Operazione fallita");
      }

      console.log("Operazione completata con successo!");
      refreshCallback?.();

    } catch (error) {
      console.error("Errore durante l'operazione:", error);
      alert(error.message || "Si è verificato un errore");
    } finally {
      setIsProcessing(false);
    }
  };

  const handleToggleFavorite = async (eventId) => {
    try {
      const res = await fetch(`/api/events/${eventId}/favorite`, {
        method: "POST",
        credentials: "include",
      });

      if (!res.ok) throw new Error("Errore toggle favorite");
 // ✅ aggiorna SOLO UI locale
      setEvents(prev =>
        prev.map(ev =>
          ev.id === eventId
            ? { ...ev, isFavorite: !ev.isFavorite }
            : ev
        )
      );
      // ✅ usa la callback già passata al hook
      //refreshCallback?.();
    } catch (err) {
      console.error(err);
    }
  };

  return {
    handleBook,
    handleCancelBooking,
    isProcessing,
    handleToggleFavorite
  };
};