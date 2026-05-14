package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import java.util.List;
import java.util.ArrayList;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class PersonalEventsIT extends BaseEventIT {

    @Test
    void getAllEvents_eventCreatedByMe_shouldHaveIsCreatedByMeTrue() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Database Study");
        event.setDateTime(java.time.LocalDateTime.parse("2026-04-10T15:00:00"));
        event.setDuration(2.0);
        event.setLocation("Room 2");
        event.setTags(java.util.List.of("Computer Science"));
        event.setDescription("SQL review");
        event.setStatus("open");
        event.setCreator(creator);

        eventRepository.save(event);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].title")
                        .value("Database Study"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].dateTime")
                        .exists())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].location")
                        .value("Room 2"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].tags[0]")
                        .value("Computer Science"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$[0].isCreatedByMe").value(true));
    }

    @Test
    void getAllEvents_eventCreatedByOtherUser_shouldHaveIsCreatedByMeFalse() throws Exception {
        User otherUser = new User(
                "Marco",
                "Verdi",
                "marco@example.com",
                "Pass123",
                "Roma",
                List.of("Social"),
                new ArrayList<>());
        userRepository.save(otherUser);

        Event event = new Event();
        event.setTitle("Other user event");
        event.setDateTime(java.time.LocalDateTime.parse("2026-04-12T10:00:00"));
        event.setDuration(3.0);
        event.setLocation("Library");
        event.setTags(java.util.List.of("Math"));
        event.setDescription("Exercises");
        event.setStatus("open");
        event.setCreator(otherUser);

        eventRepository.save(event);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].title")
                        .value("Other user event"))
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$[0].isCreatedByMe").value(false));
    }

    @Test
    void getAllEvents_authenticatedUserWithoutOwnEvents_shouldNotHavePersonalEvents() throws Exception {
        User otherUser = new User(
                "Marco",
                "Verdi",
                "marco2@example.com",
                "Pass123",
                "Roma",
                List.of("Math"),
                new ArrayList<>());
        userRepository.save(otherUser);

        Event event = new Event();
        event.setTitle("Other event");
        event.setDateTime(java.time.LocalDateTime.parse("2026-04-12T10:00:00"));
        event.setDuration(3.0);
        event.setLocation("Library");
        event.setTags(java.util.List.of("Math"));
        event.setDescription("Description");
        event.setStatus("open");
        event.setCreator(otherUser);

        eventRepository.save(event);

        mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/events")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers
                        .jsonPath("$[0].isCreatedByMe").value(false));
    }
}