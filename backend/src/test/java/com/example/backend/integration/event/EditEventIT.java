package com.example.backend.integration.event;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
@Transactional
public class EditEventIT extends BaseEventIT {

  @MockitoBean
  private JavaMailSender mailSender;

  @Test
  void editValidEvent_returns200AndUpdatesEvent() throws Exception {

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Milano (MI)-Aula Magna");
    event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
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
          "location": "Milano (MI)-Biblioteca",
          "tags": [
            { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" },
            { "macroTag": "Informatica e Tecnologie", "microTag": "Sviluppo backend" }
          ],
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
    assertEquals("Milano (MI)-Biblioteca", updatedEvent.getLocation());
    assertTrue(updatedEvent.getTags().contains(
        new com.example.backend.model.EventTag("Informatica e Tecnologie", "Sviluppo backend")));
    assertEquals("Updated description", updatedEvent.getDescription());
    assertEquals(12, updatedEvent.getMaxParticipants());
    assertEquals(false, updatedEvent.isCreatorParticipationRequired());
  }

  @Test
  void editEventWithBookedUser_sendsUpdateEmailOnlyToBookedUsers() throws Exception {
    User creator = userRepository.findById(userId).orElseThrow();

    User bookedUser = new User("Anna", "Bianchi", "anna.booked@example.com", "Password123", "Milano",
        List.of("Informatica e Tecnologie"), new ArrayList<>());

    bookedUser = userRepository.save(bookedUser);

    User notBookedUser = new User("Luca", "Verdi", "luca.notbooked@example.com", "Password123",
        "Milano", List.of("Informatica e Tecnologie"), new ArrayList<>());

    notBookedUser = userRepository.save(notBookedUser);

    Event event = new Event();
    event.setTitle("Progetto Java");
    event.setDateTime(LocalDateTime.of(2026, 5, 20, 15, 0));
    event.setDuration(2.0);
    event.setLocation("Biblioteca");
    event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
    event.setDescription("Studio progetto");
    event.setMaxParticipants(10);
    event.setCreatorParticipationRequired(true);
    event.setStatus("open");
    event.setCreator(creator);
    event.addParticipant(creator);
    event.addParticipant(bookedUser);
    event = eventRepository.save(event);

    clearInvocations(mailSender);

    String json = """
        {
          "title": "Progetto Java",
          "dateTime": "2026-05-20T15:00:00",
          "duration": 2.0,
          "location": "Aula 3",
          "tags": [
            { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
          ],
          "description": "Studio progetto",
          "maxParticipants": 10,
          "creatorParticipationRequired": true
        }
        """;

    mockMvc.perform(put("/events/" + event.getId())
        .sessionAttr("userId", userId)
        .contentType(MediaType.APPLICATION_JSON)
        .content(json))
        .andExpect(status().isOk());

    org.mockito.ArgumentCaptor<SimpleMailMessage> captor = org.mockito.ArgumentCaptor.forClass(SimpleMailMessage.class);

    verify(mailSender, atLeastOnce()).send(captor.capture());

    String bookedUserEmail = bookedUser.getEmail();
    String notBookedUserEmail = notBookedUser.getEmail();

    boolean sentToBookedUser = captor.getAllValues().stream()
        .map(SimpleMailMessage::getTo)
        .filter(to -> to != null && to.length > 0)
        .anyMatch(to -> bookedUserEmail.equals(to[0]));

    boolean sentToNotBookedUser = captor.getAllValues().stream()
        .map(SimpleMailMessage::getTo)
        .filter(to -> to != null && to.length > 0)
        .anyMatch(to -> notBookedUserEmail.equals(to[0]));

    boolean messageContainsEventName = captor.getAllValues().stream()
        .map(SimpleMailMessage::getText)
        .anyMatch(text -> text != null && text.contains("L'evento Progetto Java \u00e8 stato aggiornato"));

    Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();

    assertTrue(sentToBookedUser);
    assertFalse(sentToNotBookedUser);
    assertTrue(messageContainsEventName);
    assertEquals("Aula 3", updatedEvent.getLocation());
  }

  @Test
  void editEventWithEmptyTitle_returns400() throws Exception {

    Event event = new Event();
    event.setTitle("Java Project");
    event.setDateTime(LocalDateTime.of(2026, 10, 15, 15, 0));
    event.setDuration(2.5);
    event.setLocation("Milano (MI)-Aula Magna");
    event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
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
          "location": "Milano (MI)-Biblioteca",
          "tags": [
            { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
          ],
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
    event.setLocation("Milano (MI)-Aula Magna");
    event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
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
          "location": "Milano (MI)-Biblioteca",
          "tags": [
            { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
          ],
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
    event.setLocation("Milano (MI)-Aula Magna");
    event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
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
          "location": "Milano (MI)-Biblioteca",
          "tags": [
            { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
          ],
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
    event.setLocation("Milano (MI)-Aula Magna");
    event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
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
          "location": "Milano (MI)-Biblioteca",
          "tags": [
            { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
          ],
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
