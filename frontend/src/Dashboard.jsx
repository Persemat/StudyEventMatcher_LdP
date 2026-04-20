//Dashboard
import { useState, useEffect } from "react";
import { Calendar, Users, Plus } from "lucide-react";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";

export default function Dashboard({ user }) {
  const [events, setEvents] = useState([]);
  const [loggedUser, setLoggedUser] = useState(null);
  const [loading, setLoading] = useState(true);

  const fetchEvents = async () => {
    setLoading(true);

    // Here you will implement your real fetch:
    // fetch('/api/events/recommended', { credentials: 'include' })

    // For now we simulate loading
    setLoading(false);
  };

  useEffect(() => {
    fetchEvents();
  }, []);

  // Pass fetch function to the hook so it can refresh data after actions
  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);

  // Edit event
  const handleEdit = (event) => {
    console.log("Opening edit modal for event:", event.id);
    // TODO: implement edit modal
    // pass event + onClose + onSuccess
  };

  // Loading state to avoid empty flash
  if (loading) {
    return (
      <div className="p-8 text-center text-gray-500 italic">
        The backend is generating your personalized recommendations...
      </div>
    );
  }

  return (
    <div className="space-y-8">

      <div>
        <h1 className="text-3xl font-bold text-gray-900">Dashboard</h1>
        <p className="text-gray-600 mt-1">
          Welcome {user?.name}! Discover recommended study events for you
        </p>
      </div>

      {/* Stats Cards */}
      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow p-6 border-l-4 border-blue-500">
          <div className="flex items-center justify-between">
            <div>
              <p className="text-gray-600 text-sm">Recommended for you</p>
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
          Recommended Events
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
              No recommended events available yet
            </h3>
            <p className="text-gray-400 mt-2">
              Soon you will see events tailored to your preferences here!
            </p>
          </div>
        )}
      </div>
    </div>
  );
}