package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.EventRequest;
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

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired session");
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
     * @return a success message if the event is created, or an error response otherwise
     */
    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventRequest request, HttpSession session) {

        if (request.title() == null || request.title().isBlank() ||
                request.dateTime() == null || request.duration() == null ||
                request.location() == null || request.location().isBlank() ||
                request.tags() == null || request.tags().isEmpty()) {

            return ResponseEntity.badRequest().body("Missing required fields");
        }

        try {
            Event createdEvent = eventService.createEvent(request, session);
            return ResponseEntity.ok("Event '" + createdEvent.getTitle() + "' created successfully!");

        } catch (RuntimeException e) {
            System.out.println("⚠️ SERVICE ERROR: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(400).body("Error: " + e.getMessage());
        }
    }

    /**
     * Books the authenticated user into the specified event.
     *
     * @param id the ID of the event to book
     * @param session the current HTTP session
     * @return a success message if the booking is completed, or an error response otherwise
     */
    @PostMapping("/{id}/book")
    public ResponseEntity<?> bookEvent(@PathVariable Long id, HttpSession session) {

        try {
            eventService.bookEvent(id, session);
            return ResponseEntity.ok("Booking completed successfully");

        } catch (RuntimeException e) {
            System.out.println("⚠️ BOOKING ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Deletes an event.
     *
     * Only the authenticated creator is allowed to delete the event.
     *
     * @param id the ID of the event to delete
     * @param session the current HTTP session
     * @return a success message if the event is deleted, or an error response otherwise
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable long id, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }

        try {
            String message = eventService.deleteEvent(id, session);
            return ResponseEntity.ok(message);

        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Updates an existing event.
     *
     * Validates the required request fields before delegating to the service layer.
     *
     * @param id the ID of the event to update
     * @param request the updated event data
     * @param session the current HTTP session
     * @return a success message if the event is updated, or an error response otherwise
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id,
                                         @RequestBody EventRequest request,
                                         HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }

        if (request.title() == null || request.title().isBlank() ||
                request.dateTime() == null || request.duration() == null ||
                request.location() == null || request.location().isBlank() ||
                request.tags() == null || request.tags().isEmpty()) {

            return ResponseEntity.badRequest().body("Missing required fields");
        }

        try {
            Event updatedEvent = eventService.editEvent(id, request, session);
            return ResponseEntity.ok("Event '" + updatedEvent.getTitle() + "' successfully updated");

        } catch (RuntimeException e) {
            System.out.println("⚠️ UPDATE EVENT ERROR: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Cancels the booking of the authenticated user for the specified event.
     *
     * @param id the ID of the event for which the booking must be cancelled
     * @param session the current HTTP session
     * @return a success message if the booking is cancelled, or an error response otherwise
     */
    @DeleteMapping("/{id}/book")
    public ResponseEntity<?> cancelBooking(@PathVariable Long id, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }

        try {
            eventService.cancelBooking(id, session);
            return ResponseEntity.ok("Booking cancelled successfully");

        } catch (RuntimeException e) {
            System.out.println("⚠️ CANCEL BOOKING ERROR: " + e.getMessage());
            return ResponseEntity.badRequest().body("Error: " + e.getMessage());
        }
    }

    /**
     * Searches for events using optional filters.
     *
     * Supported filters include free-text search, tags, location, and date.
     *
     * @param session the current HTTP session
     * @param search optional free-text search term
     * @param tags optional list of tags
     * @param location optional location filter
     * @param date optional date filter
     * @return a list of matching events, or 401 if the session is invalid
     */
    @GetMapping("/search")
    public ResponseEntity<?> searchEvents(HttpSession session,
                                         @RequestParam(required = false) String search,
                                         @RequestParam(required = false) List<String> tags,
                                         @RequestParam(required = false) String location,
                                         @RequestParam(required = false) String date) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }

        return ResponseEntity.ok(
                eventService.searchForEvents(session, search, tags, location, date)
        );
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
}