package com.example.backend.integration.event;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
public class EditEventIT extends BaseEventIT {

  @Test
  void editValidEvent_returns200AndUpdatesEvent() throws Exception {

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Main Hall");
    event.setTags(List.of("Java"));
    event.setDescription("Review");
    event.setMaxParticipants(10);
    event.setCreatorParticipationRequired(true);
    event.setStatus("open");
    event.setCreator(userRepository.findById(userId).orElseThrow());
    event.addParticipant(userRepository.findById(userId).orElseThrow());
    event = eventRepository.save(event);

    String json = """
        {
          "title": "Updated Java Project",
          "dateTime": "2026-10-16T17:00:00",
          "duration": 3.0,
          "location": "Library",
          "tags": ["Java", "Backend"],
          "description": "Updated description",
          "maxParticipants": 12,
          "creatorParticipationRequired": false
        }
        """;

    mockMvc.perform(put("/events/" + event.getId())
        .sessionAttr("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isOk())
        .andExpect(content().string(
            org.hamcrest.Matchers.containsString("aggiornato con successo")));

    Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();

    assertEquals("Updated Java Project", updatedEvent.getTitle());
    assertEquals(LocalDateTime.of(2026, 10, 16, 17, 0), updatedEvent.getDateTime());
    assertEquals(3.0, updatedEvent.getDuration());
    assertEquals("Library", updatedEvent.getLocation());
    assertTrue(updatedEvent.getTags().contains("BACKEND"));
    assertEquals("Updated description", updatedEvent.getDescription());
    assertEquals(12, updatedEvent.getMaxParticipants());
    assertEquals(false, updatedEvent.isCreatorParticipationRequired());
  }

  @Test
  void editEventWithEmptyTitle_returns400() throws Exception {

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Main Hall");
    event.setTags(List.of("Java"));
    event.setDescription("Review");
    event.setMaxParticipants(10);
    event.setCreatorParticipationRequired(true);
    event.setStatus("open");
    event.setCreator(userRepository.findById(userId).orElseThrow());
    event.addParticipant(userRepository.findById(userId).orElseThrow());
    event = eventRepository.save(event);

    String json = """
        {
          "title": "",
          "dateTime": "2026-10-16T17:00:00",
          "duration": 3.0,
          "location": "Library",
          "tags": ["Java"],
          "description": "Updated description",
          "maxParticipants": 12,
          "creatorParticipationRequired": false
        }
        """;

    mockMvc.perform(put("/events/" + event.getId())
        .sessionAttr("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            org.hamcrest.Matchers.containsString("Campi obbligatori mancanti")));
  }

  @Test
  void editEventWithInvalidSession_returns401() throws Exception {

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Main Hall");
    event.setTags(List.of("Java"));
    event.setDescription("Review");
    event.setMaxParticipants(10);
    event.setCreatorParticipationRequired(true);
    event.setStatus("open");
    event.setCreator(userRepository.findById(userId).orElseThrow());
    event.addParticipant(userRepository.findById(userId).orElseThrow());
    event = eventRepository.save(event);

    String json = """
        {
          "title": "Updated Java Project",
          "dateTime": "2026-10-16T17:00:00",
          "duration": 3.0,
          "location": "Library",
          "tags": ["Java"],
          "description": "Updated description",
          "maxParticipants": 12,
          "creatorParticipationRequired": false
        }
        """;

    mockMvc.perform(put("/events/" + event.getId())
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isUnauthorized())
        .andExpect(content().string(
            org.hamcrest.Matchers.containsString("Sessione invalida o scaduta")));
  }

  @Test
  void editEventByNonCreator_returns400() throws Exception {

    User otherUser = new User("Luca", "Verdi", "luca_edit_test@example.com", "Password123",
        "Roma", List.of("Tech"), new ArrayList<>());
    otherUser = userRepository.save(otherUser);

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Main Hall");
    event.setTags(List.of("Java"));
    event.setDescription("Review");
    event.setMaxParticipants(10);
    event.setCreatorParticipationRequired(true);
    event.setStatus("open");
    event.setCreator(otherUser);
    event.addParticipant(otherUser);
    event = eventRepository.save(event);

    String json = """
        {
          "title": "Updated Java Project",
          "dateTime": "2026-10-16T17:00:00",
          "duration": 3.0,
          "location": "Library",
          "tags": ["Java"],
          "description": "Updated description",
          "maxParticipants": 12,
          "creatorParticipationRequired": false
        }
        """;

    mockMvc.perform(put("/events/" + event.getId())
        .sessionAttr("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            org.hamcrest.Matchers.containsString("Solo il creatore può modificare l'evento")));
  }

  @Test
  void editEventWithMaxParticipantsLowerThanCurrent_returns400() throws Exception {

    User otherUser = new User("Luca", "Verdi", "luca_participant_test@example.com", "Password123",
        "Roma", List.of("Social"), new ArrayList<>());
    otherUser = userRepository.save(otherUser);

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Main Hall");
    event.setTags(List.of("Java"));
    event.setDescription("Review");
    event.setMaxParticipants(10);
    event.setCreatorParticipationRequired(true);
    event.setStatus("open");
    event.setCreator(userRepository.findById(userId).orElseThrow());
    event.addParticipant(userRepository.findById(userId).orElseThrow());
    event.addParticipant(otherUser);
    event = eventRepository.save(event);

    String json = """
        {
          "title": "Updated Java Project",
          "dateTime": "2026-10-16T17:00:00",
          "duration": 3.0,
          "location": "Library",
          "tags": ["Java"],
          "description": "Updated description",
          "maxParticipants": 1,
          "creatorParticipationRequired": false
        }
        """;

    mockMvc.perform(put("/events/" + event.getId())
        .sessionAttr("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(
            org.hamcrest.Matchers
                .containsString("Il numero massimo di partecipanti non può essere minore dei partecipanti attuali")));
  }
}