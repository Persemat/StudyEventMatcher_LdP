package com.example.backend.service;

import com.example.backend.dto.EventRequest;
import com.example.backend.dto.EventResponse;
import com.example.backend.dto.EventTagDto;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

/**
 * Service responsible for managing events.
 *
 * It handles event creation, editing, deletion, booking, booking cancellation,
 * tag retrieval, and event search operations.
 */
@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    /**
     * Constructs the EventService.
     *
     * @param eventRepository repository used to manage event persistence
     * @param userRepository  repository used to retrieve user data
     * @param emailService    service used to send notification emails
     */
    public EventService(EventRepository eventRepository, UserRepository userRepository, EmailService emailService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    /**
     * Creates a new event for the authenticated user.
     *
     * The creator is automatically added as a participant.
     * Tags are normalized by trimming whitespace and converting them to uppercase.
     *
     * @param request the event creation request data
     * @param session the current HTTP session containing the authenticated user ID
     * @return the saved event
     * @throws RuntimeException if the session is invalid, the user does not exist,
     *                          or an identical event already exists for the same
     *                          creator
     */
    public Event createEvent(EventRequest request, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione invalida o scaduta");
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean eventExists = eventRepository
                .findByCreatorIdAndTitleAndDateTimeAndLocation(
                        creator.getId(),
                        request.title(),
                        request.dateTime(),
                        request.location())
                .isPresent();

        if (eventExists) {
            throw new RuntimeException("Un evento identico esiste gia per questo utente");
        }

        Event newEvent = new Event();
        newEvent.setTitle(request.title());
        newEvent.setDateTime(request.dateTime());
        newEvent.setDuration(request.duration());
        newEvent.setLocation(request.location());

        if (request.tags() != null) {
            newEvent.setTags(normalizeTags(request.tags()));
        }

        newEvent.setDescription(request.description());
        newEvent.setMaxParticipants(request.maxParticipants());
        newEvent.setCreatorParticipationRequired(request.creatorParticipationRequired());
        newEvent.setStatus("open");

        newEvent.setCreator(creator);
        newEvent.addParticipant(creator);

        return eventRepository.save(newEvent);
    }

    /**
     * Updates an existing event.
     *
     * Only the event creator can edit the event. In addition, the event cannot be
     * modified within 24 hours of its scheduled start time, and the maximum number
     * of participants cannot be set below the current participant count.
     *
     * @param id      the ID of the event to edit
     * @param request the updated event data
     * @param session the current HTTP session containing the authenticated user ID
     * @return the updated and saved event
     * @throws RuntimeException if the session is invalid, the event does not exist,
     *                          the user is not the creator, the event is too close
     *                          to its start time, or the participant limit is
     *                          invalid
     */
    @Transactional
    public Event editEvent(Long id, EventRequest request, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione invalida o scaduta");
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        if (event.getCreator() == null ||
                !event.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Solo il creatore può modificare l'evento");
        }

        if (!event.getDateTime().isAfter(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("L'evento non può essere modificato entro 24 ore dall'inizio");
        }

        if (request.maxParticipants() != null &&
                request.maxParticipants() < event.getParticipants().size()) {
            throw new RuntimeException(
                    "Il numero massimo di partecipanti non può essere minore dei partecipanti attuali");
        }

        event.setTitle(request.title());
        event.setDateTime(request.dateTime());
        event.setDuration(request.duration());
        event.setLocation(request.location());
        if (request.tags() != null) {
            event.setTags(normalizeTags(request.tags()));
        }
        event.setDescription(request.description());
        event.setMaxParticipants(request.maxParticipants());
        event.setCreatorParticipationRequired(request.creatorParticipationRequired());

        return eventRepository.save(event);
    }

    /**
     * Retrieves all events and maps them to response objects for the current user.
     *
     * Events whose date and time are in the past are automatically marked as
     * closed.
     *
     * @param session the current HTTP session containing the authenticated user ID,
     *                if available
     * @return a list of event response objects
     */
    public List<EventResponse> getAllEvents(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        List<Event> events = eventRepository.findAll();

        boolean updated = checkIfClosed(false, events);

        if (updated) {
            eventRepository.saveAll(events);
        }

        return events.stream()
                .map(event -> toResponse(event, userId))
                .toList();
    }

    /**
     * Deletes an event or reassigns its creator depending on the event settings.
     *
     * If creator participation is required, the event is deleted and cancellation
     * emails are sent to participants. Otherwise, the creator is removed from the
     * participant list; if participants remain, the creator role is reassigned to
     * the participant with the smallest ID.
     *
     * @param eventId the ID of the event to delete
     * @param session the current HTTP session containing the authenticated user ID
     * @return a status message describing the result of the operation
     * @throws RuntimeException if the session is invalid, the event does not exist,
     *                          or the authenticated user is not the creator
     */
    @Transactional
    public String deleteEvent(long eventId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione invalida o scaduta");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        if (event.getCreator() == null ||
                !event.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Solo il creatore può cancellare l'evento");
        }

        User creator = event.getCreator();

        if (event.isCreatorParticipationRequired()) {
            try {
                emailService.sendEventCancellationEmailToParticipants(event);
            } catch (Exception e) {
                logger.error("Errore mandando la mail di cancellazione per questo evento {}",
                        event.getTitle(), e);
            }

            event.getParticipants().clear();
            eventRepository.delete(event);
            return "Evento cancellato con successo";
        }

        event.removeParticipant(creator);

        Set<User> remaining = event.getParticipants();

        if (remaining.isEmpty()) {
            eventRepository.delete(event);
            return "Evento cancellato con successo";
        }

        User newCreator = new ArrayList<>(remaining).stream()
                .sorted(Comparator.comparing(User::getId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Impossibile riassegnare il creatore"));

        event.setCreator(newCreator);
        eventRepository.save(event);

        return "Creatore rimosso e ruolo riassegnato con successo";
    }

    /**
     * Books the authenticated user into an event.
     *
     * The booking is rejected if the user is already booked, if the event is full,
     * or if the event is not open for bookings.
     *
     * @param eventId the ID of the event to book
     * @param session the current HTTP session containing the authenticated user ID
     * @throws RuntimeException if the user is not authenticated, the user or event
     *                          cannot be found, the user is already booked, the
     *                          event is full, or bookings are closed
     */
    @Transactional
    public void bookEvent(Long eventId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Utente non autenticato");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        boolean alreadyBooked = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (alreadyBooked) {
            throw new RuntimeException("Sei già prenotato per questo evento");
        }

        if (event.getMaxParticipants() != null &&
                event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new RuntimeException("L'evento è pieno");
        }

        if (event.getStatus() != null &&
                !"open".equalsIgnoreCase(event.getStatus())) {
            throw new RuntimeException("Le prenotazioni non sono disponibili per questo evento");
        }

        event.addParticipant(user);
        eventRepository.save(event);
    }

    /**
     * Cancels the booking of the authenticated user for a specific event.
     *
     * @param eventId the ID of the event for which the booking must be cancelled
     * @param session the current HTTP session containing the authenticated user ID
     * @throws RuntimeException if the user is not authenticated, the user or event
     *                          cannot be found, or the user is not booked for the
     *                          event
     */
    @Transactional
    public void cancelBooking(Long eventId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Utente non autenticato");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        boolean isBooked = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isBooked) {
            throw new RuntimeException("Non ti sei prenotato per questo evento");
        }

        event.removeParticipant(user);
        eventRepository.save(event);
    }

    /**
     * Retrieves all distinct event tags available in the system.
     *
     * @return a list of distinct tags
     */
    public List<String> getAllDistinctTags() {
        return eventRepository.findDistinctTags();
    }

    /**
     * Searches for events by applying optional filters such as search text,
     * tags, location, and date.
     *
     * Events found in the past are automatically marked as closed.
     * Only open events are returned in the final response.
     *
     * @param session  the current HTTP session containing the authenticated user
     *                 ID, if available
     * @param search   free-text search term
     * @param tags     list of tags to filter by
     * @param location location filter
     * @param date     date filter in ISO format
     * @return a list of matching open events mapped to response objects
     */
    public List<EventResponse> searchForEvents(HttpSession session,
            String search,
            List<String> tags,
            String location,
            String date) {

        Long userId = (Long) session.getAttribute("userId");

        String searchParam = (search == null || search.isBlank()) ? null : search;
        String locationParam = (location == null || location.isBlank()) ? null : location;

        boolean checkTags = tags != null && !tags.isEmpty() && !tags.contains("All");
        List<String> tagsParam = checkTags ? tags : List.of();
        long tagCount = checkTags ? tagsParam.size() : 0;

        LocalDateTime startDate = null;
        LocalDateTime endDate = null;

        if (date != null && !date.trim().isEmpty()) {
            try {
                LocalDate parsed = LocalDate.parse(date);
                startDate = parsed.atStartOfDay();
                endDate = parsed.plusDays(1).atStartOfDay();
            } catch (Exception e) {
                System.out.println("Invalid date format ignored: " + date);
            }
        }

        List<Event> events = eventRepository.findByDynamicFilters(
                searchParam,
                checkTags,
                tagsParam,
                tagCount,
                locationParam,
                startDate,
                endDate);

        boolean updated = checkIfClosed(false, events);

        if (updated) {
            eventRepository.saveAll(events);
        }
        LocalDateTime now = LocalDateTime.now();
        return events.stream()
                .filter(e -> e.getDateTime().isAfter(now))
                .map(event -> toResponse(event, userId))
                .toList();
    }

    /**
     * Maps an {@link Event} entity to an {@link EventResponse}.
     *
     * The response includes creator information, current participant count,
     * booking status for the current user, and whether the current user is
     * the creator of the event.
     *
     * @param event  the event entity to map
     * @param userId the ID of the current user, if available
     * @return the mapped event response
     */
    private EventResponse toResponse(Event event, Long userId) {

        User creator = event.getCreator();

        EventResponse.CreatorResponse creatorResponse = new EventResponse.CreatorResponse(
                creator.getId(),
                creator.getName(),
                creator.getLastname());

        boolean isBooked = userId != null &&
                event.getParticipants().stream()
                        .anyMatch(u -> u.getId().equals(userId));

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getDuration(),
                event.getLocation(),
                event.getTags() == null
                        ? List.of()
                        : event.getTags().stream().map(EventTagDto::from).toList(),
                event.getDescription(),
                event.getMaxParticipants(),
                event.isCreatorParticipationRequired(),
                event.getStatus(),
                creatorResponse,
                event.getParticipants().size(),
                isBooked,
                userId != null && creator.getId().equals(userId));
    }

    public boolean checkIfClosed(boolean toSave, List<Event> events) {
        LocalDateTime now = LocalDateTime.now();

        for (Event event : events) {
            String actualStatus = event.getStatus();
            int numParticipants = event.getParticipants().size();
            Integer maxParticipants = event.getMaxParticipants();

            String newStatus;

            if (event.getDateTime().isBefore(now)) {
                newStatus = "closed";
            } else if (maxParticipants != null && numParticipants >= maxParticipants) {
                newStatus = "closed";
            } else {
                newStatus = "open";
            }

            if (!newStatus.equalsIgnoreCase(actualStatus)) {
                event.setStatus(newStatus);
                toSave = true;
            }
        }

        return toSave;
    }

    private List<EventTag> normalizeTags(List<?> tags) {
        return tags.stream()
                .map(this::toEventTag)
                .filter(tag -> tag.getMacroTag() != null && !tag.getMacroTag().isBlank())
                .map(tag -> new EventTag(
                        tag.getMacroTag().trim(),
                        tag.getMicroTag() == null || tag.getMicroTag().isBlank()
                                ? null
                                : tag.getMicroTag().trim()))
                .distinct()
                .collect(Collectors.toList());
    }

    private EventTag toEventTag(Object rawTag) {
        if (rawTag == null) {
            return new EventTag(null, null);
        }

        if (rawTag instanceof EventTag eventTag) {
            return eventTag;
        }

        if (rawTag instanceof EventTagDto eventTagDto) {
            return new EventTag(eventTagDto.macroTag(), eventTagDto.microTag());
        }

        if (rawTag instanceof Map<?, ?> map) {
            Object macroTag = map.get("macroTag");
            Object microTag = map.get("microTag");
            return new EventTag(
                    macroTag == null ? null : String.valueOf(macroTag),
                    microTag == null ? null : String.valueOf(microTag));
        }

        String label = String.valueOf(rawTag).trim();
        String[] parts = label.split("\\s*>\\s*", 2);
        if (parts.length == 2) {
            return new EventTag(parts[0], parts[1]);
        }
        return new EventTag(label, null);
    }

}
