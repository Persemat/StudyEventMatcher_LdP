package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Tag("integration")
public class EventDetailsIT extends BaseEventIT {

    @Test
    void getEventDetails_shouldReturnEventDetails() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Integration Test Event");
        event.setDateTime(java.time.LocalDateTime.parse("2026-05-15T09:00:00"));
        event.setDuration(2.0);
        event.setLocation("Test City");
        event.setTags(java.util.List.of("IT", "TEST"));
        event.setDescription("Full description for integration test");
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator); 

        event = eventRepository.save(event);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events/" + event.getId())
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.title").value("Integration Test Event"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.description").value("Full description for integration test"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.location").value("Test City"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.currentParticipants").value(1))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.isBooked").value(true))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$.participants[0].name").value("Luca"));
    }

    @Test
    void getEventDetails_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events/1"))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string("Sessione invalida o scaduta"));
    }

    @Test
    void getEventDetails_notFound_shouldReturn404() throws Exception {
        Long nonExistentId = 99999L;

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events/" + nonExistentId)
                .sessionAttr("userId", userId))
                .andExpect(status().isNotFound())
                .andExpect(content().string("Errore: Evento non trovato"));
    }
}
