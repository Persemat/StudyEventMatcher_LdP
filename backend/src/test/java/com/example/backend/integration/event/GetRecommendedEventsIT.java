package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Tag("integration")
public class GetRecommendedEventsIT extends BaseEventIT {

    @Test
    void getRecommendedEvents_withValidCoordinates_shouldReturn200AndEvents() throws Exception {

        User user = userRepository.findById(userId).orElseThrow();
        user.setMacroTags(List.of("Informatica e Tecnologie"));
        user.setMicroTags(List.of("Programmazione"));
        userRepository.save(user);

        User creator = new User();
        creator.setName("Luigi");
        creator.setLastname("Verdi");
        creator.setEmail("luigi.creator@example.com");
        creator.setPassword("Pass123!");
        creator.setFavouriteCity("Roma");
        creator = userRepository.save(creator);

        Event event = new Event();
        event.setTitle("Corso Avanzato React");
        event.setDateTime(LocalDateTime.now().plusDays(10));
        event.setDuration(2.0);
        event.setLocation("Milano (MI)-Centro");
        event.setLatitude(45.46);
        event.setLongitude(9.19); 
        event.setStatus("open");
        event.setCreator(creator);
        event.setParticipants(new HashSet<>());
        event.setTags(List.of(new EventTag("Informatica e Tecnologie", "Programmazione")));
        eventRepository.save(event);

        mockMvc.perform(get("/events/recommended") 
                .sessionAttr("userId", userId)
                .param("latitude", "45.4642")
                .param("longitude", "9.1900")
                .contentType(MediaType.APPLICATION_JSON))
                
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.messageCode").value("SUCCESS"))
                .andExpect(jsonPath("$.recommendedEvents").isArray())
                .andExpect(jsonPath("$.recommendedEvents[0].title").value("Corso Avanzato React"));
    }

    @Test
    void getRecommendedEvents_missingCoordinates_shouldReturn200AndMissingCode() throws Exception {
        mockMvc.perform(get("/events/recommended")
                .sessionAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                
                .andExpect(jsonPath("$.messageCode").value("MISSING_COORDINATES"))
                .andExpect(jsonPath("$.recommendedEvents").isEmpty());
    }

    @Test
    void getRecommendedEvents_withoutSession_shouldReturnError() throws Exception {
        mockMvc.perform(get("/events/recommended")
                .param("latitude", "45.0")
                .param("longitude", "9.0")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isUnauthorized()); 
    }
}
