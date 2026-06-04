import React, { useState, useEffect, useRef, useCallback, useMemo } from "react";
import { Search, MapPin, Calendar, X, ChevronDown, ChevronRight } from "lucide-react";
import EventCard from "./EventCard";
import EventDetails from "./EventDetails";
import { useEventActions } from "./hooks/useEventActions";
import EventFormModal from "./EventFormModal";
import { loadCityOptions, loadTagOptions } from "./catalogs";

export default function AllEvents({ user }) {
  const [events, setEvents] = useState([]);
  const [availableTags, setAvailableTags] = useState([]); // Original flat tag list.
  const [cityOptions, setCityOptions] = useState([]);
  const [searchTerm, setSearchTerm] = useState("");
  const [searchLocation, setSearchLocation] = useState("");
  const [searchData, setSearchData] = useState("");
  const [selectedTags, setSelectedTags] = useState([]);
  const [tagSearchTerm, setTagSearchTerm] = useState("");
  const [isDropdownOpen, setIsDropdownOpen] = useState(false);
  const [expandedTags, setExpandedTags] = useState([]); // Expanded macro tag groups.
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

  // Builds the hierarchical tag tree from the flat catalog loaded from disk.
  const hierarchicalTags = useMemo(() => {
    const groups = {};
    availableTags.forEach(tag => {
      if (!groups[tag.macroTag]) {
        groups[tag.macroTag] = {
          label: tag.macroTag,
          macroTagObj: null,
          children: []
        };
      }
      if (tag.isMacro) {
        groups[tag.macroTag].macroTagObj = tag;
      } else {
        groups[tag.macroTag].children.push({
          label: tag.microTag,
          microTagObj: tag
        });
      }
    });
    return Object.values(groups).filter(g => g.macroTagObj);
  }, [availableTags]);

  // Filters the tag tree by the search text typed inside the dropdown.
  const filteredTagsTree = useMemo(() => {
    const query = tagSearchTerm.trim().toLowerCase();
    if (!query) return hierarchicalTags;

    return hierarchicalTags.map(parent => {
      const parentMatches = parent.label.toLowerCase().includes(query);
      const matchingChildren = parent.children.filter(c =>
        c.label.toLowerCase().includes(query)
      );

      if (parentMatches || matchingChildren.length > 0) {
        return {
          ...parent,
          // If the macro tag matches, keep all children; otherwise keep matching children only.
          children: parentMatches ? parent.children : matchingChildren
        };
      }
      return null;
    }).filter(Boolean);
  }, [hierarchicalTags, tagSearchTerm]);

  // Toggles a macro tag and all of its children at once.
  const handleMacroToggle = (parentGroup) => {
    setSelectedTags((prev) => {
      const allRelatedTags = [parentGroup.macroTagObj, ...parentGroup.children.map(c => c.microTagObj)];
      const isFullySelected = allRelatedTags.every(t => prev.some(pt => pt.label === t.label));

      if (isFullySelected) {
        // Remove the macro tag and every child tag.
        return prev.filter(pt => !allRelatedTags.some(rt => rt.label === pt.label));
      } else {
        // Add the macro tag and any missing child tags.
        const nextTags = [...prev];
        allRelatedTags.forEach(t => {
          if (!nextTags.some(pt => pt.label === t.label)) {
            nextTags.push(t);
          }
        });
        return nextTags;
      }
    });
  };

  // Toggles one micro tag without changing the rest of its group.
  const handleMicroToggle = (childObj) => {
    setSelectedTags((prev) => {
      const isSelected = prev.some((t) => t.label === childObj.label);
      if (isSelected) {
        return prev.filter((t) => t.label !== childObj.label);
      } else {
        return [...prev, childObj];
      }
    });
  };

  const toggleExpand = (tagLabel) => {
    setExpandedTags(prev => 
      prev.includes(tagLabel) 
        ? prev.filter(label => label !== tagLabel)
        : [...prev, tagLabel]
    );
  };

  // Suggests matching cities from the shared city catalog while the location filter is typed.
  const locationSuggestions = useMemo(() => {
    const query = searchLocation.trim().toLowerCase();
    if (!query) return [];
    return cityOptions
      .filter((option) => option.name.toLowerCase().startsWith(query))
      .slice(0, 30);
  }, [cityOptions, searchLocation]);

  const fetchTags = async () => {
    try {
      const data = await loadTagOptions();
      setAvailableTags(data);
    } catch (err) {
      console.error("Errore fetch tags:", err);
    }
  };

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

  const favouriteCity = useMemo(
    () => cityOptions.find((option) => option.label === user?.favouriteCity),
    [cityOptions, user?.favouriteCity]
  );

  const distanceSortCity = selectedSearchCity || favouriteCity;

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
      
      // selectedTags stores the original flat tag objects, so tag.label is the API label.
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

        {/* Hierarchical tag dropdown */}
        <div className="relative md:w-48" ref={dropdownRef}>
          <button
            type="button"
            onClick={() => setIsDropdownOpen(!isDropdownOpen)}
            className="w-full h-[42px] flex items-center justify-between px-4 bg-white border border-gray-200 rounded-lg hover:bg-gray-50 transition-colors"
          >
            <span className="text-sm font-medium text-gray-700 truncate">
              {selectedTags.length === 0 ? "Tutti i tag" : `${selectedTags.length} tag`}
            </span>
            <span className="text-gray-400 text-xs shrink-0">
              <ChevronDown size={14} />
            </span>
          </button>
          
          {isDropdownOpen && (
            <div className="absolute z-20 w-full md:w-72 mt-2 bg-white border border-gray-200 rounded-lg shadow-xl p-2 right-0 md:left-0">
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

              <div className="max-h-64 overflow-y-auto pr-1">
                {filteredTagsTree.length > 0 ? (
                  filteredTagsTree.map((parent) => {
                    const hasChildren = parent.children && parent.children.length > 0;
                    const isExpanded = expandedTags.includes(parent.label);
                    
                    // The macro checkbox is checked only when the macro tag and all children are selected.
                    const allRelatedLabels = [parent.macroTagObj.label, ...parent.children.map(c => c.microTagObj.label)];
                    const isParentChecked = allRelatedLabels.every(label => 
                      selectedTags.some(st => st.label === label)
                    );

                    return (
                      <div key={parent.label} className="mb-1">
                        {/* Macro tag row */}
                        <div className="flex items-center gap-2 px-2 py-1.5 hover:bg-gray-50 rounded">
                          <input
                            type="checkbox"
                            checked={isParentChecked}
                            onChange={() => handleMacroToggle(parent)}
                            className="rounded text-blue-600 w-4 h-4 cursor-pointer focus:ring-blue-500"
                          />
                          <span 
                            className="text-sm font-semibold text-gray-800 flex-1 cursor-pointer select-none truncate"
                            onClick={() => hasChildren && toggleExpand(parent.label)}
                          >
                            {parent.label}
                          </span>
                          {hasChildren && (
                            <button
                              type="button"
                              onClick={(e) => {
                                e.stopPropagation();
                                toggleExpand(parent.label);
                              }}
                              className="p-1 text-gray-400 hover:text-gray-700 rounded-md transition-colors"
                            >
                              {isExpanded ? <ChevronDown size={16} /> : <ChevronRight size={16} />}
                            </button>
                          )}
                        </div>

                        {/* Micro tag rows */}
                        {hasChildren && isExpanded && (
                          <div className="ml-6 pl-2 border-l-2 border-gray-100 flex flex-col gap-1 mt-1 mb-2">
                            {parent.children.map((child) => (
                              <label key={child.label} className="flex items-center gap-2 px-2 py-1.5 hover:bg-gray-50 rounded cursor-pointer">
                                <input
                                  type="checkbox"
                                  checked={selectedTags.some((t) => t.label === child.microTagObj.label)}
                                  onChange={() => handleMicroToggle(child.microTagObj)}
                                  className="rounded text-blue-600 w-3.5 h-3.5 cursor-pointer focus:ring-blue-500"
                                />
                                <span className="text-sm text-gray-600 select-none truncate">{child.label}</span>
                              </label>
                            ))}
                          </div>
                        )}
                      </div>
                    );
                  })
                ) : (
                  <p className="px-3 py-4 text-sm text-center text-gray-500">Nessun tag trovato</p>
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
          availableTags={availableTags} // EventForm expects the original flat tag list.
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
             className="absolute top-4 right-4 text-gray-500 hover:text-gray-800 font-bold text-xl z-10"
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
