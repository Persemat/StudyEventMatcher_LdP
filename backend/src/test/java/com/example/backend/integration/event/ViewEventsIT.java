package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Tag("integration")
public class ViewEventsIT extends BaseEventIT {

    @Test
    void getAllEvents_shouldReturnEventList() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("React Workshop");
        event.setDateTime(java.time.LocalDateTime.parse("2026-04-15T09:00:00"));
        event.setDuration(1.5);
        event.setLocation("Online");
        event.setTags(java.util.List.of("Frontend"));
        event.setDescription("Component-based development");
        event.setStatus("open");
        event.setCreator(creator);

        eventRepository.save(event);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].title").value("React Workshop"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].location").value("Online"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].dateTime").exists());
    }

    @Test
    void getAllEvents_emptyList_shouldReturnEmptyArray() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    void getAllEvents_unauthenticated_shouldReturn401() throws Exception {
        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events"))
                .andExpect(status().isUnauthorized());
    }
}