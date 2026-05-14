package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

@Tag("integration")
public class CreateBookingIT extends BaseEventIT {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession session;
    private User user;
    private User creator;
    private Event event;

    @BeforeEach
    void prepareDatabase() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        session = new MockHttpSession();

        user = new User(
                "Mario",
                "Rossi",
                "mario.booking@example.com",
                "Password123!",
                "Milano",
                List.of("Tech"),
                new ArrayList<>());
        user = userRepository.save(user);

        creator = new User(
                "Luca",
                "Bianchi",
                "luca.creator@example.com",
                "Password123!",
                "Roma",
                List.of("Social"),
                new ArrayList<>());
        creator = userRepository.save(creator);

        event = new Event();
        event.setTitle("Software Engineering Study");
        event.setDescription("Study session");
        event.setLocation("Room 1");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setDuration(2.0);
        event.setMaxParticipants(3);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creator);

        if (event.getParticipants() == null) {
            event.setParticipants(new HashSet<>());
        }

        event.addParticipant(creator);

        event = eventRepository.save(event);
    }

    @Test
    void bookingSuccess_shouldReturn200AndUpdateEvent() throws Exception {
        session.setAttribute("userId", user.getId());

        mockMvc.perform(
                post("/events/{id}/book", event.getId())
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(content().string("Prenotazione completata con successo"));

        mockMvc.perform(
                get("/events")
                        .session(session))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(event.getTitle()))
                .andExpect(jsonPath("$[0].isBooked").value(true))
                .andExpect(jsonPath("$[0].currentParticipants").value(2));
    }

    @Test
    void bookingUnauthenticated_shouldReturn400() throws Exception {
        mockMvc.perform(
                post("/events/{id}/book", event.getId())
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Utente non autenticato"));
    }

    @Test
    void bookingEventNotFound_shouldReturn400() throws Exception {
        session.setAttribute("userId", user.getId());

        mockMvc.perform(
                post("/events/{id}/book", 9999L)
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Evento non trovato"));
    }

    @Test
    void bookingAlreadyExists_shouldReturn400() throws Exception {
        session.setAttribute("userId", user.getId());

        event.addParticipant(user);
        eventRepository.save(event);

        mockMvc.perform(
                post("/events/{id}/book", event.getId())
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Sei già prenotato per questo evento"));
    }

    @Test
    void bookingEventFull_shouldReturn400() throws Exception {
        session.setAttribute("userId", user.getId());

        event.setMaxParticipants(1);
        eventRepository.save(event);

        mockMvc.perform(
                post("/events/{id}/book", event.getId())
                        .session(session))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: L'evento è pieno"));
    }
}