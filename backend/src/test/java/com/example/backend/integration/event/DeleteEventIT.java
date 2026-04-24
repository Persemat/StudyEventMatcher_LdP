package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
class DeleteEventIT extends BaseEventIT {

    @Test
    void deleteEvent_whenCreatorIsRequired_shouldDeleteEvent() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Event with required creator");
        event.setDateTime(LocalDateTime.parse("2026-04-20T15:00:00"));
        event.setDuration(2.0);
        event.setLocation("Room 1");
        event.setTags(List.of("Java"));
        event.setDescription("Description");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setName("User" + i);
            user.setLastname("Test");
            user.setEmail("user" + i + "@example.com");
            user.setPassword("Pass123");
            userRepository.save(user);
            event.addParticipant(user);
        }

        eventRepository.save(event);
        long eventId = event.getId();

        mockMvc.perform(delete("/events/" + eventId)
                        .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Event deleted successfully")));

        assertFalse(eventRepository.findById(eventId).isPresent());
    }

    @Test
    void deleteEvent_whenCreatorNotRequiredWithOthers_shouldReassignCreator() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        User user1 = new User();
        user1.setName("Marco");
        user1.setLastname("Verdi");
        user1.setEmail("marco@example.com");
        user1.setPassword("Pass123");
        userRepository.save(user1);

        User user2 = new User();
        user2.setName("Anna");
        user2.setLastname("Bianchi");
        user2.setEmail("anna@example.com");
        user2.setPassword("Pass123");
        userRepository.save(user2);

        Event event = new Event();
        event.setTitle("Event without required creator");
        event.setDateTime(LocalDateTime.parse("2026-04-20T15:00:00"));
        event.setDuration(2.0);
        event.setLocation("Library");
        event.setTags(List.of("Database"));
        event.setDescription("Description");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);
        event.addParticipant(user1);
        event.addParticipant(user2);

        eventRepository.save(event);
        long eventId = event.getId();

        mockMvc.perform(delete("/events/" + eventId)
                        .sessionAttr("userId", userId))
                .andExpect(status().isOk());

        Event updated = eventRepository.findById(eventId).orElseThrow();

        assertTrue(eventRepository.findById(eventId).isPresent());
        assertEquals(user1.getId(), updated.getCreator().getId());
        assertFalse(updated.getParticipants().stream().anyMatch(u -> u.getId().equals(userId)));
        assertTrue(updated.getParticipants().stream().anyMatch(u -> u.getId().equals(user1.getId())));
        assertTrue(updated.getParticipants().stream().anyMatch(u -> u.getId().equals(user2.getId())));
    }

    @Test
    void deleteEvent_whenCreatorNotRequiredAndNoOtherParticipants_shouldDeleteEvent() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Event to delete");
        event.setDateTime(LocalDateTime.parse("2026-04-20T15:00:00"));
        event.setDuration(2.0);
        event.setLocation("Room 3");
        event.setTags(List.of("React"));
        event.setDescription("Description");
        event.setMaxParticipants(5);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        eventRepository.save(event);
        long eventId = event.getId();

        mockMvc.perform(delete("/events/" + eventId)
                        .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Event deleted successfully")));

        assertFalse(eventRepository.findById(eventId).isPresent());
    }

    @Test
    void deleteEvent_whenUserNotCreator_shouldReturnError() throws Exception {
        User creator = new User();
        creator.setName("Marco");
        creator.setLastname("Verdi");
        creator.setEmail("marco2@example.com");
        creator.setPassword("Pass123");
        userRepository.save(creator);

        Event event = new Event();
        event.setTitle("Other user event");
        event.setDateTime(LocalDateTime.parse("2026-04-20T15:00:00"));
        event.setDuration(2.0);
        event.setLocation("Room 5");
        event.setTags(List.of("Java"));
        event.setDescription("Description");
        event.setMaxParticipants(8);
        event.setCreatorParticipationRequired(true);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        eventRepository.save(event);
        long eventId = event.getId();

        mockMvc.perform(delete("/events/" + eventId)
                        .sessionAttr("userId", userId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Only the creator can delete the event")));

        assertTrue(eventRepository.findById(eventId).isPresent());
    }

    @Test
    void deleteEvent_whenUserNotAuthenticated_shouldReturn401() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Protected event");
        event.setDateTime(LocalDateTime.parse("2026-04-20T15:00:00"));
        event.setDuration(2.0);
        event.setLocation("Room 6");
        event.setTags(List.of("Security"));
        event.setDescription("Description");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        eventRepository.save(event);
        long eventId = event.getId();

        mockMvc.perform(delete("/events/" + eventId))
                .andExpect(status().isUnauthorized());
    }
}