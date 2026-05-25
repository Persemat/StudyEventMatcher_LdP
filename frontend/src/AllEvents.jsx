import React, { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { Search, MapPin, Calendar, X } from "lucide-react";
import EventCard from "./EventCard";
import EventDetails from "./EventDetails";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";
import { loadCityOptions, loadTagOptions } from "./catalogs";

export default function AllEvents({ user }) {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState([]);
  const [cityOptions, setCityOptions] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchLocation, setSearchLocation] = useState("");
  const [searchData, setSearchData] = useState("");
  const [selectedTags, setSelectedTags] = useState([]);
  const [tagSearchTerm, setTagSearchTerm] = useState("");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [isLocationInputFocused, setIsLocationInputFocused] = useState(false);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [eventToEdit, setEventToEdit] = useState(null);
  const [selectedEventId, setSelectedEventId] = useState(null);
  const [distanceKm, setDistanceKm] = useState(25);
  const [isCityInputFocused, setIsCityInputFocused] = useState(false);
  const [hideOverlapping, setHideOverlapping] = useState(false);
  const [sortBy, setSortBy] = useState("date");
  
  const dropdownRef = useRef(null);
  const locationInputRef = useRef(null);
  const today = new Date().toISOString().split('T')[0];

  const cityInputRef = useRef(null);
  const now = useMemo(() => Date.now(), []);

  // Dynamically filters dropdown tags based on the typed text.
  const filteredTags = useMemo(() => {
    const query = tagSearchTerm.trim().toLowerCase();
    if (!query) return availableTags;
    return availableTags.filter((tag) => tag.label.toLowerCase().includes(query));
  }, [availableTags, tagSearchTerm]);

  // Suggests matching cities from the shared city catalog while the location filter is typed.
  const locationSuggestions = useMemo(() => {
    const query = searchLocation.trim().toLowerCase();
    if (!query) return [];
    return cityOptions
      .filter((option) => option.name.toLowerCase().startsWith(query))
      .slice(0, 30);
  }, [cityOptions, searchLocation]);

  // Loads the tag catalog used by both filters and the edit modal.
  const fetchTags = async () => {
    try {
      const data = await loadTagOptions();
      setAvailableTags(data);
    } catch (err) {
      console.error("Errore fetch tags:", err);
    }
  };

  // Loads the same city catalog used by create/edit event location autocomplete.
  const fetchCities = async () => {
    try {
      const data = await loadCityOptions();
      setCityOptions(data);
    } catch (err) {
      console.error("Errore fetch comuni:", err);
    }
  };

  const citySuggestions = useMemo(() => {
    const query = searchLocation.trim().toLowerCase();
    if (!query) return [];
    return cityOptions
      .filter((option) =>
        option.name.toLowerCase().startsWith(query) ||
        option.label.toLowerCase().startsWith(query)
      )
      .slice(0, 30);
  }, [searchLocation, cityOptions]);

  const selectedSearchCity = useMemo(
    () => cityOptions.find((option) => option.label === searchLocation.trim()),
    [searchLocation, cityOptions]
  );

  // Falls back to the profile city when distance sorting has no search city.
  const favouriteCity = useMemo(
    () => cityOptions.find((option) => option.label === user?.favouriteCity),
    [cityOptions, user?.favouriteCity]
  );

  const distanceSortCity = selectedSearchCity || favouriteCity;

  // Uses the same Haversine formula as the backend distance filter.
  const calculateDistanceKm = useCallback((event) => {
    if (!distanceSortCity || event.latitude == null || event.longitude == null) {
      return Number.POSITIVE_INFINITY;
    }

    const earthRadiusKm = 6371;
    const latDistance = (event.latitude - distanceSortCity.latitude) * Math.PI / 180;
    const lonDistance = (event.longitude - distanceSortCity.longitude) * Math.PI / 180;
    const firstLatitude = distanceSortCity.latitude * Math.PI / 180;
    const secondLatitude = event.latitude * Math.PI / 180;

    const a =
      Math.sin(latDistance / 2) * Math.sin(latDistance / 2) +
      Math.cos(firstLatitude) *
        Math.cos(secondLatitude) *
        Math.sin(lonDistance / 2) *
        Math.sin(lonDistance / 2);
    const c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

    return earthRadiusKm * c;
  }, [distanceSortCity]);

  // Sorts the already filtered results without triggering another API call.
  const sortedEvents = useMemo(() => {
    const orderedEvents = [...events];

    if (sortBy === "alphabetical") {
      return orderedEvents.sort((first, second) =>
        (first.title || "").localeCompare(second.title || "", "it", { sensitivity: "base" })
      );
    }

    if (sortBy === "distance") {
      return orderedEvents.sort((first, second) => {
        const distanceDifference = calculateDistanceKm(first) - calculateDistanceKm(second);

        if (distanceDifference !== 0) return distanceDifference;

        return new Date(first.dateTime).getTime() - new Date(second.dateTime).getTime();
      });
    }

    return orderedEvents.sort(
      (first, second) => new Date(first.dateTime).getTime() - new Date(second.dateTime).getTime()
    );
  }, [events, sortBy, calculateDistanceKm]);

  const fetchEvents = useCallback(async () => {
    try {
      setLoading(true);
      setError("");
      const url = new URL("/api/events/search", window.location.origin);
      if (searchTerm) url.searchParams.append("search", searchTerm);
      if (searchLocation) url.searchParams.append("location", searchLocation);
      if (selectedSearchCity) {
        url.searchParams.append("distanceKm", distanceKm);
        url.searchParams.append("latitude", selectedSearchCity.latitude);
        url.searchParams.append("longitude", selectedSearchCity.longitude);
      }
      if (searchData) url.searchParams.append("date", searchData);
      if (hideOverlapping) url.searchParams.append("hideOverlapping", "true");
      if (selectedTags.length > 0) {
        selectedTags.forEach((tag) => url.searchParams.append("tags", tag.label));
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

  }, [searchTerm, searchLocation, searchData, selectedTags, distanceKm, selectedSearchCity, hideOverlapping]);

  useEffect(() => {
    fetchTags();
    fetchCities();
  }, []);

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

      <div className="space-y-2">
        <div className="flex flex-col items-end gap-2 sm:flex-row sm:justify-end">
          <label className="flex h-9 items-center gap-2 text-sm font-medium text-gray-700">
            <span>Ordina per</span>
            <select
              value={sortBy}
              onChange={(e) => setSortBy(e.target.value)}
              className="h-9 rounded-lg border border-gray-200 bg-white px-3 text-sm text-gray-700 outline-none transition-colors hover:bg-gray-50 focus:border-blue-500 focus:ring-2 focus:ring-blue-100"
            >
              <option value="date">Data</option>
              <option value="alphabetical">Alfabetico</option>
              <option value="distance">Distanza</option>
            </select>
          </label>

          <label
            className={`flex h-9 items-center gap-2 px-3 rounded-lg border text-sm font-medium cursor-pointer transition-colors ${
              hideOverlapping
                ? "bg-blue-50 text-blue-700 border-blue-200"
                : "bg-white text-gray-700 border-gray-200 hover:bg-gray-50"
            }`}
          >
            <input
              type="checkbox"
              checked={hideOverlapping}
              onChange={(e) => setHideOverlapping(e.target.checked)}
              className="h-4 w-4 rounded border-gray-300 text-blue-600 focus:ring-blue-500"
            />
            <span>Compatibili con i miei orari</span>
          </label>
        </div>

        <div className="bg-white p-4 rounded-xl shadow-sm border border-gray-100 flex flex-col md:flex-row flex-wrap gap-4">
        <div className="relative flex-1 min-w-[200px]">
          <Search className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            type="text"
            placeholder="Cerca per titolo o descrizione..."
            className="w-full pl-10 pr-10 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchTerm}
            onChange={(e) => setSearchTerm(e.target.value)}
          />
          {searchTerm && (
              <button
                type="button"
                onClick={() => setSearchTerm("")}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-700"
              aria-label="Cancella ricerca"
            >
              <X size={16} />
            </button>
          )}
        </div>

        <div className="relative md:w-48">
          <MapPin className="absolute left-3 top-1/2 -translate-y-1/2 text-gray-400" size={18} />
          <input
            ref={cityInputRef}
            type="text"
            placeholder="Filtra per città..."
            className="w-full pl-10 pr-10 py-2 border border-gray-200 rounded-lg focus:outline-none focus:ring-2 focus:ring-blue-500"
            value={searchLocation}
            onChange={(e) => setSearchLocation(e.target.value)}
            onFocus={() => setIsCityInputFocused(true)}
            onBlur={() => setIsCityInputFocused(false)}
          />
          {searchLocation && (
            <button
              type="button"
              onMouseDown={(e) => e.preventDefault()}
              onClick={() => {
                setSearchLocation("");
                setIsCityInputFocused(false);
                cityInputRef.current?.focus();
              }}
              className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-gray-700"
              aria-label="Cancella filtro città"
            >
              <X size={16} />
            </button>
          )}
          {isCityInputFocused && citySuggestions.length > 0 && (
            <ul className="absolute z-20 w-full bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-48 overflow-y-auto">
              {citySuggestions.map((option) => (
                <li
                  key={option.label}
                  onMouseDown={(e) => {
                    e.preventDefault();
                    setSearchLocation(option.label);
                    setIsCityInputFocused(false);
                    cityInputRef.current?.blur();
                  }}
                  className="px-4 py-2 hover:bg-blue-50 cursor-pointer text-sm text-gray-700 transition-colors"
                >
                  {option.label}
                </li>
              ))}
            </ul>
          )}
        </div>

        <div className="md:w-48 px-1">
          <div className="flex items-center justify-between mb-1">
            <label className="text-xs font-medium text-gray-600">Distanza</label>
            <span className="text-xs font-semibold text-blue-600">{distanceKm} km</span>
          </div>
          <input
            type="range"
            min="1"
            max="100"
            step="1"
            value={distanceKm}
            onChange={(e) => setDistanceKm(Number(e.target.value))}
            disabled={!selectedSearchCity}
            className="w-full cursor-pointer disabled:opacity-50 disabled:cursor-not-allowed"
          />
          {isLocationInputFocused && locationSuggestions.length > 0 && (
            <ul className="absolute z-20 w-full bg-white border border-gray-200 rounded-lg shadow-lg mt-1 max-h-48 overflow-y-auto">
              {locationSuggestions.map((option) => (
                <li
                  key={option.label}
                  onMouseDown={(e) => {
                    e.preventDefault();
                    setSearchLocation(option.label);
                    setIsLocationInputFocused(false);
                    locationInputRef.current?.blur();
                  }}
                  className="px-4 py-2 hover:bg-blue-50 cursor-pointer text-sm text-gray-700 transition-colors"
                >
                  {option.label}
                </li>
              ))}
            </ul>
          )}
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
            className="w-full h-[42px] flex items-center justify-between px-4 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
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
      </div>

      {!loading && sortedEvents.length > 0 ? (
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          {sortedEvents.map((event) => {
            // Allows editing only for the creator and only more than 24 hours before start.
            const eventStart = new Date(event.dateTime).getTime();
            const editable = event.isCreatedByMe && eventStart > now + 24 * 60 * 60 * 1000;

            return (
              <EventCard
                key={event.id}
                event={event}
                onBook={() => handleBook(event.id)}
                onCancelBooking={() =>
                  handleCancelBooking(event.id, event.creatorParticipationRequired, event.isCreatedByMe)
                }
                onEdit={editable ? () => setEventToEdit(event) : undefined}
                canEdit={editable}
                onToggleFavorite={() => handleToggleFavorite(event.id)}
                onShowDetails={() => setSelectedEventId(event.id)}

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
          onSuccess={() => {
            setEventToEdit(null);
            fetchEvents();
          }}
          availableTags={availableTags}
        />
      )}

      {selectedEventId && (
    <div
      className="fixed inset-0 z-50 flex items-center justify-center"
      onClick={() => setSelectedEventId(null)}
    >
       <div
        className="bg-white rounded-xl shadow-2xl w-full max-w-2xl max-h-[90vh] overflow-y-auto relative border-2 border-gray-200"
        onClick={(e) => e.stopPropagation()}
       >
          
          <button 
             onClick={() => setSelectedEventId(null)}
             className="absolute top-4 right-4 text-gray-500 hover:text-gray-800 font-bold text-xl"
          >
             ✕
          </button>
          
          <EventDetails eventId={selectedEventId} />

       </div>
    </div>
  )}
    </div>
  );
}
