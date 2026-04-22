//AllEvents
import React, { useState, useEffect, useRef } from "react";
import { Search, Filter, MapPin } from "lucide-react"; 
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";

export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchLocation, setSearchLocation] = useState(""); 
  const [selectedTags, setSelectedTags] = useState([]);
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);
  
  const dropdownRef = useRef(null);

  const canEdit = (event) => {
    const eventStart = new Date(event.dateTime);
    const in24Hours = new Date(Date.now() + 24 * 60 * 60 * 1000);
    return event.isCreatedByMe && eventStart > in24Hours;
  };


  const fetchTags = async () => {
    try {
      const res = await fetch("/api/events/tags", { credentials: "include" });
      if (res.ok) {
        const data = await res.json();
        setAvailableTags(data.filter(t => t !== "All"));
      }
    } catch (err) { console.error("Error fetch tags:", err); }
  };


  const fetchEvents = async () => {
    try {
      setLoading(true);
      setError("");
      
      const url = new URL("/api/events/search", window.location.origin);
      if (searchTerm) url.searchParams.append("search", searchTerm);
      if (searchLocation) url.searchParams.append("location", searchLocation);
      if (selectedTags.length > 0) {
        selectedTags.forEach((t) => url.searchParams.append("tags", t));
      }

      const response = await fetch(url, {
        method: "GET",
        credentials: "include",
      });

      if (!response.ok) {
        throw new Error("Error fetching events");
      }

      const data = await response.json();

      setEvents(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error("Error fetching events:", err);
      setError("Unable to load events");
      setEvents([]);
    } finally {
      setLoading(false);
    }
  };
  
  useEffect(() => { fetchTags(); }, []);

  useEffect(() => {
    const delayDebounceFn = setTimeout(() => { fetchEvents(); }, 300);
    return () => clearTimeout(delayDebounceFn);
  }, [searchTerm, selectedTags, searchLocation]);

  const { handleBook, handleCancelBooking } = useEventActions(fetchEvents);
  

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Esplora Eventi</h1>
        <p className="text-gray-600">Catalogo Gruppi di Studio</p>
      </div>

      {/* Filters */}
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row gap-4">
        <div className="relative flex-1">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Cerca per titolo o descrizione..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>



        {/* NEW: search: Città/Luogo */}
        <div className="relative md:w-64">
          <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Filtra per città..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchLocation}
            onChange={(e) => setSearchLuogo(e.target.value)}
          />
        </div>


        {/* Dropdown Tag (Multiple) */}
        <div className="relative md:w-64" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="w-full flex items-center justify-between px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <span className="text-sm font-medium text-gray-700">
              {selectedTags.length === 0 ? "Tutti i tag" : `${selectedTags.length} tag`}
            </span>
            <span className="text-gray-400 text-xs">▼</span>
          </button>
          {isDropdownOpen && (
            <div className="absolute z-20 w-full mt-2 bg-white border border-gray-200 rounded-lg shadow-xl max-h-60 overflow-y-auto p-1">
              {availableTags.map((tag) => (
                <label key={tag} className="flex items-center gap-3 px-3 py-2 hover:bg-gray-50 rounded cursor-pointer">
                  <input
                    type="checkbox"
                    checked={selectedTags.includes(tag)}
                    onChange={() => setSelectedTags(prev => prev.includes(tag) ? prev.filter(t => t !== tag) : [...prev, tag])}
                    className="rounded text-blue-600"
                  />
                  <span className="text-sm">{tag}</span>
                </label>
              ))}
            </div>
          )}
        </div>

      </div>



      {!loading && events.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => (
            <EventCard 
              key={event.id} 
              event={event} 
              onBook={() => handleBook(event.id)}
              onCancelBooking={() => handleCancelBooking(event.id, event.creatorParticipationRequired, event.isCreatedByMe)}
              onEdit={canEdit(event) ? () => setEventToEdit(event) : undefined}
              canEdit={canEdit(event)}
            />
          ))}
        </div>
      ) : (
        <div className="text-center py-20 bg-gray-50 rounded-2xl border-2 border-dashed border-gray-200">
          <p className="text-gray-500">{loading ? "Ricerca in corso..." : "Nessun evento trovato"}</p>
        </div>
      )}

      {eventToEdit && (
        <EventFormModal
          mode="edit"
          event={eventToEdit}
          onClose={() => setEventToEdit(null)}
          onSuccess={() => { setEventToEdit(null); fetchEvents(); }}
        />
      )}
    </div>
  );
}