package com.example.backend.controller;

import java.util.List;

import org.springframework.http.ResponseEntity;
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

@RestController
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    // ================= GET ALL EVENTS =================
    @GetMapping
    public ResponseEntity<?> getAllEvents(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Invalid or expired session");
        }

        return ResponseEntity.ok(eventService.getAllEvents(session));
    }

    // ================= CREATE EVENT =================
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

    // ================= BOOK EVENT =================
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

    // ================= DELETE EVENT =================
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

     // ================= EDIT EVENT =================
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

    // ================= CANCEL BOOKING =================
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

    // ================= SEARCH EVENTS =================
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

    // ================= GET ALL TAGS =================
    @GetMapping("/tags")
    public ResponseEntity<?> getAllTags() {
        return ResponseEntity.ok(eventService.getAllDistinctTags());
    }


}