import { useState } from 'react';

export const useEventActions = (refreshCallback) => {
  const [isProcessing, setIsProcessing] = useState(false);

  // Azione: PRENOTA
  const handleBook = async (eventId) => {
    setIsProcessing(true);
    try {
      const response = await fetch(`/Eventi/${eventId}/prenota`, {
        method: "POST",
        headers: {
          "Content-Type": "application/json",
        }
      });

      if (!response.ok) {
        throw new Error("Errore nella prenotazione");
      }

      console.log("✅ Prenotazione effettuata con successo!");

      // aggiorna lista eventi
      refreshCallback?.();

    } catch (error) {
      console.error("❌ Errore:", error);
      alert("Errore durante la prenotazione");
    } finally {
      setIsProcessing(false);
    }
};

  // Azione: ABBANDONA/ANNULLA PRENOTAZIONE
  const handleCancelBooking = async (eventId, isCreatorRequired, isCreatedByMe) => {
    // 1. MESSAGGIO DI CONFERMA (Logica UI pronta)
    const message = isCreatedByMe
      ? (isCreatorRequired
          ? "Sei il creatore e la tua presenza è obbligatoria. L'INTERO evento verrà eliminato. Confermi?"
          : "Sei il creatore. Verrai rimosso dall'evento. Se ci sono altri iscritti, il ruolo di creatore verrà riassegnato. Confermi?")
      : "Vuoi davvero annullare la tua partecipazione?";

    if (!window.confirm(message)) return;

    setIsProcessing(true);

    try {
      let response;

      if (isCreatedByMe) {
        response = await fetch(`/Eventi/${eventId}`, {
          method: "DELETE",
          credentials: "include",
        });
      } else {
        response = await fetch(`/Eventi/${eventId}/prenota`, {
          method: "DELETE",
          credentials: "include",
        });
      }

      const text = await response.text();

      if (!response.ok) {
        throw new Error(text || "Operazione non riuscita");
      }

      console.log("✅ Operazione completata con successo!");
      refreshCallback?.();
    } catch (error) {
      console.error("Errore durante l'operazione:", error);
      alert(error.message || "Si è verificato un errore");
    } finally {
      setIsProcessing(false);
    }
  };

  return {
    handleBook,
    handleCancelBooking,
    isProcessing
  };
};