import { useState } from 'react';

export const useEventActions = (refreshCallback) => {
  const [isProcessing, setIsProcessing] = useState(false);

  // Azione: PRENOTA
  const handleBook = async (eventId) => {
    setIsProcessing(true);
    console.log("📡 Backend: Chiamata POST /Eventi/" + eventId + "/prenota");
    // TODO: implementare fetch vera
    // if (res.ok) refreshCallback?.();
    setIsProcessing(false);
  };

  // Azione: ABBANDONA/ANNULLA PRENOTAZIONE
  const handleCancelBooking = async (eventId, isCreatorRequired) => {
    // 1. MESSAGGIO DI CONFERMA (Logica UI pronta)
    const message = isCreatorRequired 
      ? "Sei il creatore e la tua presenza è obbligatoria. L'INTERO evento verrà eliminato. Confermi?"
      : "Vuoi davvero annullare la tua partecipazione?";

    if (!window.confirm(message)) return;

    setIsProcessing(true);
    
    // 2. SIMULAZIONE CHIAMATA (Logica di business pronta)
    console.log("--- SIMULAZIONE BACKEND ---");
    if (isCreatorRequired) {
      console.log(`📡 Azione: DELETE /Eventi/${eventId} (Eliminazione totale)`);
    } else {
      console.log(`📡 Azione: DELETE /Eventi/${eventId}/prenota (Solo abbandono)`);
    }

    // Simuliamo un ritardo del server di 1 secondo
    setTimeout(() => {
      console.log("✅ Simulazione completata con successo!");
      setIsProcessing(false);
      
      // Opzionale: chiamiamo il refresh per vedere se la UI reagisce
      // refreshCallback?.(); 
    }, 1000);
  };

  return {
    handleBook,
    handleCancelBooking,
    isProcessing
  };
};