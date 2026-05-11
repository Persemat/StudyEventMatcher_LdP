package com.example.backend.integration.event;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.http.MediaType;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@Tag("integration")
public class CreateEventIT extends BaseEventIT {

    @Test
    void validEventCreation_shouldReturn200AndSaveEvent() throws Exception {
        String json = """
                {
                  "title": "Java Study Group",
                  "dateTime": "2026-10-15T15:00:00",
                  "duration": 2.5,
                  "location": "Main Hall",
                  "tags": ["Java"],
                  "description": "Revision session",
                  "maxParticipants": 10,
                  "creatorParticipationRequired": true
                }
                """;

        mockMvc.perform(post("/events")
                .sessionAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("creato con successo")));

        assertEquals(1, eventRepository.count());
    }

    @Test
    void eventCreationWithoutTitle_shouldReturn400() throws Exception {
        String json = """
                {
                  "title": "",
                  "dateTime": "2026-10-15T15:00:00",
                  "duration": 2.5,
                  "location": "Main Hall",
                  "tags": ["Java"],
                  "maxParticipants": 10
                }
                """

                ;

        mockMvc.perform(post("/events")
                .sessionAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest());

        assertEquals(0, eventRepository.count());
    }
}