//useEventActions.js
import { useState } from 'react';

export const useEventActions = (refreshCallback) => {
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
        throw new Error("Error while booking the event");
      }

      console.log("✅ Booking completed successfully!");

      // refresh events list
      refreshCallback?.();

    } catch (error) {
      console.error("❌ Error:", error);
      alert("Error during booking");
    } finally {
      setIsProcessing(false);
    }
  };

  // Action: CANCEL / LEAVE EVENT
  const handleCancelBooking = async (eventId, isCreatorRequired, isCreatedByMe) => {

    const message = isCreatedByMe
      ? (isCreatorRequired
          ? "You are the creator and your presence is required. The ENTIRE event will be deleted. Do you confirm?"
          : "You are the creator. You will be removed from the event. If there are other participants, the creator role will be reassigned. Do you confirm?")
      : "Do you really want to cancel your participation?";

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
        throw new Error(text || "Operation failed");
      }

      console.log("Operation completed successfully!");
      refreshCallback?.();

    } catch (error) {
      console.error("Error during operation:", error);
      alert(error.message || "An error occurred");
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