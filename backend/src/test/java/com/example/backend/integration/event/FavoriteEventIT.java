package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;
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

@Tag("integration")
public class FavoriteEventIT extends BaseEventIT {

    private MockHttpSession session;
    private User user;
    private User creator;
    private Event event;

    @BeforeEach
    void prepareDatabase() {

        eventRepository.deleteAll();
        userRepository.deleteAll();

        session = new MockHttpSession();

        user = new User();
        user.setName("Mario");
        user.setLastname("Rossi");
        user.setEmail("mario.favorite@example.com");
        user.setPassword("Password123!");
        user.setFavouriteCity("Milano");
        user = userRepository.save(user);

        creator = new User();
        creator.setName("Luca");
        creator.setLastname("Bianchi");
        creator.setEmail("luca.creator@example.com");
        creator.setPassword("Password123!");
        creator.setFavouriteCity("Milano");
        creator = userRepository.save(creator);

        event = new Event();
        event.setTitle("Backend Study Group");
        event.setDescription("Study Spring Boot");
        event.setLocation("Milano (MI)-Aula 2");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setDuration(2.0);
        event.setMaxParticipants(5);
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
    void toggleFavorite_success_shouldReturn200() throws Exception {

        session.setAttribute("userId", user.getId());

        mockMvc.perform(
                        post("/events/{id}/favorite", event.getId())
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Operazione sui preferiti completata"));

        mockMvc.perform(
                        get("/events")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].isFavorite").value(true));
    }

    @Test
    void toggleFavorite_unauthenticated_shouldReturn401() throws Exception {

        mockMvc.perform(
                        post("/events/{id}/favorite", event.getId())
                                .session(session)
                )
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Sessione invalida o scaduta"));
    }

    @Test
    void toggleFavorite_eventNotFound_shouldReturn400() throws Exception {

        session.setAttribute("userId", user.getId());

        mockMvc.perform(
                        post("/events/{id}/favorite", 9999L)
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Evento non trovato"));
    }

    @Test
    void toggleFavorite_twice_shouldToggleOnAndOff() throws Exception {

        session.setAttribute("userId", user.getId());

        // aggiunge ai preferiti
        mockMvc.perform(
                        post("/events/{id}/favorite", event.getId())
                                .session(session)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/events")
                                .session(session)
                )
                .andExpect(jsonPath("$[0].isFavorite").value(true));

        // rimuove dai preferiti
        mockMvc.perform(
                        post("/events/{id}/favorite", event.getId())
                                .session(session)
                )
                .andExpect(status().isOk());

        mockMvc.perform(
                        get("/events")
                                .session(session)
                )
                .andExpect(jsonPath("$[0].isFavorite").value(false));
    }
}