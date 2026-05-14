package com.example.backend.integration.event;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.hasItems;
import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class SearchEventsIT extends BaseEventIT {

    @Test
    void searchEvents_byStructuredMicroTag_shouldReturnStructuredTags() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();
        createEvent("SQL study", List.of(new EventTag("Informatica e Tecnologie", "SQL")), creator);
        createEvent("Physics study", List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")), creator);

        mockMvc.perform(get("/events/search")
                .queryParam("tags", "Informatica e Tecnologie > SQL")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("SQL study"))
                .andExpect(jsonPath("$[0].tags[0].macroTag").value("Informatica e Tecnologie"))
                .andExpect(jsonPath("$[0].tags[0].microTag").value("SQL"))
                .andExpect(content().string(not(containsString("Physics study"))));
    }

    @Test
    void searchEvents_byMultipleStructuredTags_shouldApplyOrLogic() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();
        createEvent("Full match", List.of(
                new EventTag("Informatica e Tecnologie", "SQL"),
                new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")), creator);
        createEvent("Only SQL", List.of(new EventTag("Informatica e Tecnologie", "SQL")), creator);
        createEvent("Only physics", List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")), creator);
        createEvent("Only chemistry", List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Chimica")), creator);

        mockMvc.perform(get("/events/search")
                .queryParam("tags", "Informatica e Tecnologie > SQL")
                .queryParam("tags", "Scienze Matematiche Fisiche e Naturali > Fisica")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[*].title", hasItems("Full match", "Only SQL", "Only physics")))
                .andExpect(content().string(not(containsString("Only chemistry"))));
    }

    @Test
    void searchEvents_byMacroAndMicroOfSameEvent_shouldNotDuplicateResults() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();
        createEvent("SQL study", List.of(new EventTag("Informatica e Tecnologie", "SQL")), creator);
        createEvent("Physics study", List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")), creator);

        mockMvc.perform(get("/events/search")
                .queryParam("tags", "Informatica e Tecnologie")
                .queryParam("tags", "Informatica e Tecnologie > SQL")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].title").value("SQL study"))
                .andExpect(content().string(not(containsString("Physics study"))));
    }

    @Test
    void searchEvents_byMacroOnlyTag_shouldReturnNullMicroTag() throws Exception {
        User creator = userRepository.findById(userId).orElseThrow();
        createEvent("Macro only study", List.of(new EventTag("Informatica e Tecnologie", null)), creator);
        createEvent("Micro study", List.of(new EventTag("Informatica e Tecnologie", "SQL")), creator);

        mockMvc.perform(get("/events/search")
                .queryParam("tags", "Informatica e Tecnologie")
                .sessionAttr("userId", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].title", hasItems("Macro only study", "Micro study")))
                .andExpect(jsonPath("$[*].tags[*].macroTag", hasItems("Informatica e Tecnologie")))
                .andExpect(jsonPath("$[?(@.title == 'Macro only study')].tags[0].microTag", hasItems(nullValue())));
    }

    private void createEvent(String title, List<EventTag> tags, User creator) {
        Event event = new Event();
        event.setTitle(title);
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setDuration(2.0);
        event.setLocation("Milano (MI)-Aula Test");
        event.setTags(tags);
        event.setDescription("Descrizione test");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creator);
        eventRepository.save(event);
    }
}
