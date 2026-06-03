package com.example.backend.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.EventRequest;
import com.example.backend.dto.EventResponse;
import com.example.backend.dto.EventSearchFilter;
import com.example.backend.dto.RecommendationResponse;
import com.example.backend.model.Event;
import com.example.backend.service.EventService;

import jakarta.servlet.http.HttpSession;

/**
 * REST controller responsible for event-related operations.
 *
 * It exposes endpoints for:
 * - retrieving events
 * - creating events
 * - editing events
 * - deleting events
 * - booking and cancelling bookings
 * - searching events
 * - retrieving available tags
 */
@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;
    private static final String USER_ID = "userId";
    private static final String ERROR = "Errore: ";
    private static final String INVALID_SESSION = "Sessione invalida o scaduta";
    private static final Logger logger = LoggerFactory.getLogger(EventController.class);

    /**
     * Constructs the EventController.
     *
     * @param eventService service used to manage event operations
     */
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    /**
     * Retrieves all events visible to the authenticated user.
     *
     * @param session the current HTTP session
     * @return a list of all events, or 401 if the session is invalid
     */
    @GetMapping
    public ResponseEntity<?> getAllEvents(HttpSession session) {

        Long userId = (Long) session.getAttribute(USER_ID);

        if (userId == null) {
            return ResponseEntity.status(401).body(INVALID_SESSION);
        }

        return ResponseEntity.ok(eventService.getAllEvents(session));
    }

    /**
     * Creates a new event.
     *
     * Validates the required request fields before delegating to the service layer.
     *
     * @param request the event creation request
     * @param session the current HTTP session
     * @return a success message if the event is created, or an error response
     *         otherwise
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request, HttpSession session) {

        if (request.title() == null || request.title().isBlank() ||
                request.dateTime() == null || request.duration() == null ||
                request.location() == null || request.location().isBlank() ||
                request.tags() == null || request.tags().isEmpty()) {

            return ResponseEntity.badRequest().body("Campi obbligatori mancanti");
        }

        try {
            Event createdEvent = eventService.createEvent(request, session);
            return ResponseEntity.ok("Evento '" + createdEvent.getTitle() + "' creato con successo!");

        } catch (RuntimeException e) {
            logger.error(" ERRORE SERVICE: {}", e.getMessage(), e);
            return ResponseEntity.status(400).body(ERROR + e.getMessage());
        }
    }

    /**
     * Books the authenticated user into the specified event.
     *
     * @param id      the ID of the event to book
     * @param session the current HTTP session
     * @return a success message if the booking is completed, or an error response
     *         otherwise
     */
    @PostMapping("/{id}/book")
    public ResponseEntity<?> bookEvent(@PathVariable Long id, HttpSession session) {

        try {
            eventService.bookEvent(id, session);
            return ResponseEntity.ok("Prenotazione completata con successo");

        } catch (RuntimeException e) {
            logger.error(" ERRORE DI PRENOTAZIONE: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ERROR + e.getMessage());
        }
    }

    /**
     * Deletes an event.
     *
     * Only the authenticated creator is allowed to delete the event.
     *
     * @param id      the ID of the event to delete
     * @param session the current HTTP session
     * @return a success message if the event is deleted, or an error response
     *         otherwise
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable long id, HttpSession session) {

        Long userId = (Long) session.getAttribute(USER_ID);

        if (userId == null) {
            return ResponseEntity.status(401).body(INVALID_SESSION);
        }

        try {
            String message = eventService.deleteEvent(id, session);
            return ResponseEntity.ok(message);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Toggles the favorite status of a specific event for the authenticated user.
     * <p>
     * If the event is already marked as a favorite, it will be removed; otherwise,
     * it will be added to the user's favorites list.
     *
     * @param id      the ID of the event to toggle as favorite
     * @param session the current HTTP session
     * @return a success message if the operation is completed, an error message
     *         or a 401 Unauthorized response if the session is invalid
     */
    @PostMapping("/{id}/favorite")
    public ResponseEntity<?> toggleFavorite(@PathVariable Long id, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Sessione invalida o scaduta");
        }

        try {
            eventService.toggleFavorite(id, session);
            return ResponseEntity.ok("Operazione sui preferiti completata");

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Errore: " + e.getMessage());
        }
    }

    /**
     * Updates an existing event.
     *
     * Validates the required request fields before delegating to the service layer.
     *
     * @param id      the ID of the event to update
     * @param request the updated event data
     * @param session the current HTTP session
     * @return a success message if the event is updated, or an error response
     *         otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id,
            @RequestBody EventRequest request,
            HttpSession session) {

        Long userId = (Long) session.getAttribute(USER_ID);

        if (userId == null) {
            return ResponseEntity.status(401).body(INVALID_SESSION);
        }

        if (request.title() == null || request.title().isBlank() ||
                request.dateTime() == null || request.duration() == null ||
                request.location() == null || request.location().isBlank() ||
                request.tags() == null || request.tags().isEmpty()) {

            return ResponseEntity.badRequest().body("Campi obbligatori mancanti");
        }

        try {
            Event updatedEvent = eventService.editEvent(id, request, session);
            return ResponseEntity.ok("Evento '" + updatedEvent.getTitle() + "' aggiornato con successo");

        } catch (RuntimeException e) {
            logger.error(" ERRORE AGGIORNAMENTO EVENTO: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ERROR + e.getMessage());
        }
    }

    /**
     * Cancels the booking of the authenticated user for the specified event.
     *
     * @param id      the ID of the event for which the booking must be cancelled
     * @param session the current HTTP session
     * @return a success message if the booking is cancelled, or an error response
     *         otherwise
     */
    @DeleteMapping("/{id}/book")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, HttpSession session) {

        Long userId = (Long) session.getAttribute(USER_ID);

        if (userId == null) {
            return ResponseEntity.status(401).body(INVALID_SESSION);
        }

        try {
            eventService.cancelBooking(id, session);
            return ResponseEntity.ok("Prenotazione cancellata con successo");

        } catch (RuntimeException e) {
            logger.error(" ERRORE CANCELLAZIONE PRENOTAZIONE: {}", e.getMessage(), e);
            return ResponseEntity.badRequest().body(ERROR + e.getMessage());
        }
    }

    /**
     * Searches for events using optional filters encapsulated in
     * {@link EventSearchFilter}.
     *
     * Supported filters include free-text search, tags, location, date,
     * coordinates, and distance.
     * Spring automatically binds the query parameters from the request URL to the
     * filter object.
     *
     * @param session the current HTTP session
     * @param filter  an object containing all optional search parameters provided
     *                by the client
     * @return a list of matching events, or 401 if the session is invalid
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(HttpSession session, @ModelAttribute EventSearchFilter filter) {

        Long userId = (Long) session.getAttribute(USER_ID);

        if (userId == null) {
            return ResponseEntity.status(401).body(INVALID_SESSION);
        }

        return ResponseEntity.ok(eventService.searchForEvents(session, filter));
    }

    /**
     * Retrieves all distinct event tags available in the system.
     *
     * @return a list of available tags
     */
    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags() {
        return ResponseEntity.ok(eventService.getAllDistinctTags());
    }

    /**
     * Retrieves the details of a specific event.
     *
     * @param id      the ID of the event to retrieve
     * @param session the current HTTP session
     * @return the event details (EventResponse), 401 if unauthorized, or 404 if not
     *         found
     */
    @GetMapping("/{id}")
    public ResponseEntity<?> getEventDetails(@PathVariable Long id, HttpSession session) {

        Long userId = (Long) session.getAttribute(USER_ID);
        if (userId == null) {
            return ResponseEntity.status(401).body(INVALID_SESSION);
        }

        try {

            EventResponse eventDetails = eventService.getEventDetails(id, session);

            return ResponseEntity.ok(eventDetails);

        } catch (RuntimeException e) {
            logger.error(" ERRORE DETTAGLI EVENTO: {}", e.getMessage(), e);
            return ResponseEntity.status(404).body(ERROR + e.getMessage());
        }
    }

    /**
     * Retrieves a personalized list of recommended events for the currently
     * authenticated user.
     * <p>
     * This endpoint uses the geographical coordinates of the user's favorite city
     * to perform distance-based filtering and scoring. If the coordinates are
     * omitted,
     * the recommendation logic may degrade or fall back to profile-only matching.
     *
     * @param session   the current HTTP session
     * @param latitude  the optional latitude of the user's preferred city (can be
     *                  null)
     * @param longitude the optional longitude of the user's preferred city (can be
     *                  null)
     * @return a {@link ResponseEntity} containing a {@link RecommendationResponse}
     *         with the events
     *         and a status message, or a 401 Unauthorized response if the session
     *         is invalid
     */
    @GetMapping("/recommended")
    public ResponseEntity<?> getRecommendedEvents(
            HttpSession session,
            @RequestParam(required = false) Double latitude,
            @RequestParam(required = false) Double longitude) {

        Long userId = (Long) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).body("Sessione invalida o scaduta");
        }

        RecommendationResponse response = eventService.getRecommendedEvents(session, latitude, longitude);
        return ResponseEntity.ok(response);

    }
}