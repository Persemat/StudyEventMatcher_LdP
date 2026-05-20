import React, { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { Search, MapPin, Calendar } from "lucide-react";
import EventCard from "./EventCard";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";
import { loadTagOptions } from "./catalogs";

export default function AllEvents() {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchLocation, setSearchLocation] = useState(""); 
  const [searchData, setSearchData] = useState("");
  const [selectedTags, setSelectedTags] = useState([]);
  const [tagSearchTerm, setTagSearchTerm] = useState("");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(""); 
  const [eventToEdit, setEventToEdit] = useState(null);
  
  const dropdownRef = useRef(null);
  const today = new Date().toISOString().split('T')[0];

  const now = useMemo(() => Date.now(), []);

  // Dynamically filters dropdown tags based on the typed text.
  const filteredTags = useMemo(() => {
    const query = tagSearchTerm.trim().toLowerCase();
    if (!query) return availableTags;
    return availableTags.filter((tag) => tag.label.toLowerCase().includes(query));
  }, [availableTags, tagSearchTerm]);

  // Loads the tag catalog used by both filters and the edit modal.
  const fetchTags = async () => {
    try {
      const data = await loadTagOptions();
      setAvailableTags(data);
    } catch (err) { console.error("Errore fetch tags:", err); }
  };

  // Builds the event search query by combining text, location, date, and selected tags.
  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const url = new URL("/api/events/search", window.location.origin);
      if (searchTerm) url.searchParams.append("search", searchTerm);
      if (searchLocation) url.searchParams.append("location", searchLocation);
      if (searchData) url.searchParams.append("date", searchData);
      
      if (selectedTags.length > 0) {
        selectedTags.forEach((t) => url.searchParams.append("tags", t.label));
      }

      const response = await fetch(url, { credentials: "include" });
      if (!response.ok) throw new Error("Errore fetch eventi");

      const data = await response.json();
      setEvents(Array.isArray(data) ? data : []);
    } catch (err) {
      console.error(err);
      setError("Impossibile caricare gli eventi"); 
      setEvents([]);
    } finally {
      setLoading(false);
    }
  }, [searchTerm, searchLocation, searchData, selectedTags]);


  
  useEffect(() => { fetchTags(); }, []);

  // Adds a small debounce to avoid one API call per keystroke.
  useEffect(() => {
    const delayDebounceFn = setTimeout(() => {
      fetchEvents();
    }, 300);
    return () => clearTimeout(delayDebounceFn);
  }, [fetchEvents]);

  const { handleBook, handleCancelBooking, handleToggleFavorite } = useEventActions(fetchEvents, setEvents);

  return (
    <div className="p-6 space-y-6">
      <div>
        <h1 className="text-3xl font-bold text-gray-900">Esplora Eventi</h1>
        <p className="text-gray-600">Catalogo Gruppi di Studio</p>
      </div>

      
      {error && (
        <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-2 rounded-lg text-center">
          {error}
        </div>
      )}

      
      <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row flex-wrap gap-4">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Cerca per titolo o descrizione..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
        </div>

        <div className="relative md:w-48">
          <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Filtra per città..."
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchLocation}
            onChange={(e) => setSearchLocation(e.target.value)}
          />
        </div>

        <div className="relative md:w-48">
          <Calendar className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="date"
            min={today} 
            className="w-full pl-10 pr-4 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500 text-gray-600"
            value={searchData}
            onChange={(e) => setSearchData(e.target.value)}
          />
        </div>

        <div className="relative md:w-48" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="w-full flex items-center justify-between px-4 py-2 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <span className="text-sm font-medium text-gray-700 truncate">
              {selectedTags.length === 0 ? "Tutti i tag" : `${selectedTags.length} tag`}
            </span>
            <span className="text-gray-400 text-xs shrink-0">▼</span>
          </button>
          {isDropdownOpen && (
            <div className="absolute z-20 w-full mt-2 bg-white border border-gray-200 rounded-lg shadow-xl p-2">
              <div className="relative mb-2">
                <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={16} />
                <input
                  type="text"
                  value={tagSearchTerm}
                  onChange={(e) => setTagSearchTerm(e.target.value)}
                  placeholder="Cerca tag..."
                  className="w-full pl-9 pr-3 py-2 border border-gray-200 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-blue-500"
                />
              </div>

              <div className="max-h-52 overflow-y-auto">
                {filteredTags.length > 0 ? (
                  filteredTags.map((tag) => (
                    <label key={tag.label} className="flex items-center gap-3 px-3 py-2 hover:bg-gray-50 rounded cursor-pointer">
                      <input
                        type="checkbox"
                        checked={selectedTags.some((t) => t.label === tag.label)}
                        onChange={() => setSelectedTags(prev => prev.some((t) => t.label === tag.label) ? prev.filter(t => t.label !== tag.label) : [...prev, tag])}
                        className="rounded text-blue-600"
                      />
                      <span className="text-sm">{tag.label}</span>
                    </label>
                  ))
                ) : (
                  <p className="px-3 py-2 text-sm text-gray-500">Nessun tag trovato</p>
                )}
              </div>
            </div>
          )}
        </div>
      </div>

      {!loading && events.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {events.map((event) => {
            // Allows editing only for the creator and only more than 24 hours before start.
            const eventStart = new Date(event.dateTime).getTime();
            const editable = event.isCreatedByMe && eventStart > (now + 24 * 60 * 60 * 1000);
            
            return (
              <EventCard 
                key={event.id} 
                event={event} 
                onBook={() => handleBook(event.id)}
                onCancelBooking={() => handleCancelBooking(event.id, event.creatorParticipationRequired, event.isCreatedByMe)}
                onEdit={editable ? () => setEventToEdit(event) : undefined}
                canEdit={editable}
                onToggleFavorite={() => handleToggleFavorite(event.id)}
              />
            );
          })}
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
          availableTags={availableTags}
        />
      )}
    </div>
  );
}
