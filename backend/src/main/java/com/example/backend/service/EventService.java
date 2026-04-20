package com.example.backend.service;

import com.example.backend.dto.EventRequest;
import com.example.backend.dto.EventResponse;
import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;

@Service
public class EventService {
    private static final Logger logger = LoggerFactory.getLogger(EventService.class);

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public EventService(EventRepository eventRepository, UserRepository userRepository, EmailService emailService) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.emailService = emailService;
        
    }

    // ================= CREATE EVENT =================
    public Event createEvent(EventRequest request, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Invalid or expired session");
        }

        User creator = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean eventExists = eventRepository
                .findByCreatorIdAndTitleAndDateTimeAndLocation(
                        creator.getId(),
                        request.title(),
                        request.dateTime(),
                        request.location()
                )
                .isPresent();

        if (eventExists) {
            throw new RuntimeException("An identical event already exists for this user");
        }

        Event newEvent = new Event();
        newEvent.setTitle(request.title());
        newEvent.setDateTime(request.dateTime());
        newEvent.setDuration(request.duration());
        newEvent.setLocation(request.location());

        if (request.tags() != null) {
            List<String> normalizedTags = request.tags().stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .map(tag -> tag.trim().toUpperCase())
                    .collect(Collectors.toList());

            newEvent.setTags(normalizedTags);
        }

        newEvent.setDescription(request.description());
        newEvent.setMaxParticipants(request.maxParticipants());
        newEvent.setCreatorParticipationRequired(request.creatorParticipationRequired());
        newEvent.setStatus("open");

        newEvent.setCreator(creator);
        newEvent.addParticipant(creator);

        return eventRepository.save(newEvent);
    }

    // ================= EDIT EVENT =================
    @Transactional
    public Event editEvent(Long id, EventRequest request, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Invalid or expired session");
        }

        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getCreator() == null ||
                !event.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can edit the event");
        }

        if (!event.getDateTime().isAfter(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("Event cannot be modified within 24 hours of start");
        }

        if (request.maxParticipants() != null &&
                request.maxParticipants() < event.getParticipants().size()) {
            throw new RuntimeException("Maximum number of participants cannot be lower than current participants");
        }

        event.setTitle(request.title());
        event.setDateTime(request.dateTime());
        event.setDuration(request.duration());
        event.setLocation(request.location());
        if (request.tags() != null) {
            List<String> normalizedTags = request.tags().stream()
                    .filter(tag -> tag != null && !tag.isBlank())
                    .map(tag -> tag.trim().toUpperCase())
                    .collect(Collectors.toList());

            event.setTags(normalizedTags);
        } 
        event.setDescription(request.description());
        event.setMaxParticipants(request.maxParticipants());
        event.setCreatorParticipationRequired(request.creatorParticipationRequired());
      

        return eventRepository.save(event);
    }

    // ================= GET ALL EVENTS =================
    public List<EventResponse> getAllEvents(HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        List<Event> events = eventRepository.findAll();

        boolean updated = false;

        for (Event event : events) {
            if (event.getDateTime().isBefore(LocalDateTime.now())
                    && "open".equalsIgnoreCase(event.getStatus())) {
                event.setStatus("closed");
                updated = true;
            }
        }

        if (updated) {
            eventRepository.saveAll(events);
        }

        return events.stream()
                .map(event -> toResponse(event, userId))
                .toList();
    }

    // ================= DELETE EVENT =================
    @Transactional
    public String deleteEvent(long eventId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Session invalid or expired");
        }

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        if (event.getCreator() == null ||
                !event.getCreator().getId().equals(userId)) {
            throw new RuntimeException("Only the creator can delete the event");
            
        }

        User creator = event.getCreator();

       if (event.isCreatorParticipationRequired()) {
            try {
                emailService.sendEventCancellationEmailToParticipants(event);
            } catch (Exception e) {
                //System.err.println("Error sending cancellation emails for event " + event.getTitle());
                logger.error("Error sending cancellation emails for event {}",
                        event.getTitle(), e);
            }

            event.getParticipants().clear();
            eventRepository.delete(event);
            return "Event deleted successfully";
        }

        event.removeParticipant(creator);

        Set<User> remaining = event.getParticipants();

        if (remaining.isEmpty()) {
            eventRepository.delete(event);
            return "Event deleted successfully";
        }

        User newCreator = new ArrayList<>(remaining).stream()
                .sorted(Comparator.comparing(User::getId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Unable to reassign creator"));

        event.setCreator(newCreator);
        eventRepository.save(event);

        return "Creator removed and role reassigned successfully";
    }

    // ================= BOOK EVENT =================
    @Transactional
    public void bookEvent(Long eventId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        boolean alreadyBooked = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (alreadyBooked) {
            throw new RuntimeException("You are already booked for this event");
        }

        if (event.getMaxParticipants() != null &&
                event.getParticipants().size() >= event.getMaxParticipants()) {
            throw new RuntimeException("Event is full");
        }

        if (event.getStatus() != null &&
                !"open".equalsIgnoreCase(event.getStatus())) {
            throw new RuntimeException("Bookings are not available for this event");
        }

        event.addParticipant(user);
        eventRepository.save(event);
    }

    // ================= CANCEL BOOKING =================
    @Transactional
    public void cancelBooking(Long eventId, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("User not authenticated");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Event not found"));

        boolean isBooked = event.getParticipants().stream()
                .anyMatch(p -> p.getId().equals(userId));

        if (!isBooked) {
            throw new RuntimeException("You are not booked for this event");
        }

        event.removeParticipant(user);
        eventRepository.save(event);
    }

    // ================= GET ALL TAGS =================
    public List<String> getAllDistinctTags() {
        return eventRepository.findDistinctTags();
    }

    // ================= SEARCH EVENTS =================
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
                endDate
        );

        boolean updated = false;

        for (Event event : events) {
            if (event.getDateTime().isBefore(LocalDateTime.now())
                    && "open".equalsIgnoreCase(event.getStatus())) {
                event.setStatus("closed");
                updated = true;
            }
        }

        if (updated) {
            eventRepository.saveAll(events);
        }

        return events.stream()
                .filter(e -> "open".equalsIgnoreCase(e.getStatus()))
                .map(event -> toResponse(event, userId))
                .toList();
    }

    // ================= MAPPER =================
    private EventResponse toResponse(Event event, Long userId) {

        User creator = event.getCreator();

        EventResponse.CreatorResponse creatorResponse =
                new EventResponse.CreatorResponse(
                        creator.getId(),
                        creator.getName(),
                        creator.getLastname()
                );

        boolean isBooked = userId != null &&
                event.getParticipants().stream()
                        .anyMatch(u -> u.getId().equals(userId));

        return new EventResponse(
                event.getId(),
                event.getTitle(),
                event.getDateTime(),
                event.getDuration(),
                event.getLocation(),
                event.getTags(),
                event.getDescription(),
                event.getMaxParticipants(),
                event.isCreatorParticipationRequired(),
                event.getStatus(),
                creatorResponse,
                event.getParticipants().size(),
                isBooked,
                userId != null && creator.getId().equals(userId)
        );
    }
}