package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.EventResponse;
import com.example.backend.model.Event;
import com.example.backend.model.User;

public class EventViewTest extends BaseEventTest {

    @Test
    void getAllEvents_shouldMapDataCorrectly() {

        User creator = new User();
        creator.setId(1L);
        creator.setName("Mario");
        creator.setLastname("Rossi");

        Event event = new Event();
        event.setTitle("Analytical Review");
        event.setDateTime(LocalDateTime.parse("2026-04-10T10:30:00"));
        event.setLocation("Milano (MI)-Aula 4");
        event.setDuration(2.0);
        event.setTags(List.of(new com.example.backend.model.EventTag("Scienze Matematiche Fisiche e Naturali", "Matematica")));
        event.setDescription("Exam preparation");
        event.setStatus("open");
        event.setCreator(creator);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<EventResponse> result = eventService.getAllEvents(session);

        assertEquals(1, result.size());
        assertEquals("Analytical Review", result.get(0).title());
        assertEquals("Milano (MI)-Aula 4", result.get(0).location());
        assertEquals("Mario", result.get(0).creator().name());
        assertTrue(result.get(0).isCreatedByMe());
    }

    @Test
    void getAllEvents_shouldReturnEmptyList_whenNoEvents() {

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findAll()).thenReturn(List.of());

        List<EventResponse> result = eventService.getAllEvents(session);

        assertTrue(result.isEmpty());
    }
}