package com.example.backend.integration.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class DeleteBookingIT extends BaseEventIT {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession session;
    private User user;
    private User creator;
    private Event event;

    @BeforeEach
    void setupDatabase() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        session = new MockHttpSession();

        user = new User();
        user.setName("Mario");
        user.setLastname("Rossi");
        user.setEmail("mario.cancel@example.com");
        user.setPassword("Password123!");
        user = userRepository.save(user);

        creator = new User();
        creator.setName("Luca");
        creator.setLastname("Bianchi");
        creator.setEmail("luca.creator.cancel@example.com");
        creator.setPassword("Password123!");
        creator = userRepository.save(creator);

        event = new Event();
        event.setTitle("Database Study");
        event.setDescription("SQL and NoSQL review");
        event.setLocation("Milano (MI)-Aula 2");
        event.setDateTime(LocalDateTime.now().plusDays(2));
        event.setDuration(2.0);
        event.setMaxParticipants(4);
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
    void cancelBookingSuccessfully() throws Exception {
        // Setup: register Mario *before* cancelling
        event.addParticipant(user);
        eventRepository.save(event);

        session.setAttribute("userId", user.getId());

        // Action: cancel booking
        mockMvc.perform(
                        delete("/events/{id}/book", event.getId())
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Prenotazione cancellata con successo"));

        // Verification: simulate React page reload and check data
        mockMvc.perform(
                        get("/events")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].title").value(event.getTitle()))
                .andExpect(jsonPath("$[0].isBooked").value(false)) // Mario should no longer be booked
                .andExpect(jsonPath("$[0].currentParticipants").value(1)); // Only creator remains
    }

    @Test
    void cancelBookingUnauthenticatedUser() throws Exception {
        // No userId set in session!

        mockMvc.perform(
                        delete("/events/{id}/book", event.getId())
                                .session(session)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Sessione invalida o scaduta"));
    }

    @Test
    void cancelBookingEventNotFound() throws Exception {
        session.setAttribute("userId", user.getId());

        mockMvc.perform(
                        delete("/events/{id}/book", 9999L)
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Evento non trovato"));
    }

    @Test
    void cancelBookingUserNotBooked() throws Exception {
        session.setAttribute("userId", user.getId());

        // In @BeforeEach Mario is not added to the event, so he is not booked!

        mockMvc.perform(
                        delete("/events/{id}/book", event.getId())
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Non ti sei prenotato per questo evento"));
    }
}