package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.EventResponse;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;

public class SearchEventsTest extends BaseEventTest {

    private void setupMockEvent(Event event, Long id, LocalDateTime dateTime) {
        User creator = new User();
        creator.setId(99L);
        creator.setName("Admin");
        creator.setLastname("User");

        event.setId(id);
        event.setDateTime(dateTime);
        event.setCreator(creator);
        event.setParticipants(new HashSet<>());
        event.setTags(new ArrayList<>());
        event.setStatus("open");
    }

    @Test
    void testSearchForEvents_FullFilters_Success() {
        // Arrange
        String search = "Party";
        List<String> tags = List.of("Informatica e Tecnologie > SQL", "Scienze Matematiche Fisiche e Naturali > Fisica");
        String location = "Roma (RM)";
        String date = "2026-05-20";

        Event event = new Event();
        setupMockEvent(event, 1L, LocalDateTime.now().plusDays(1));
        event.setTags(List.of(
                new EventTag("Informatica e Tecnologie", "SQL"),
                new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")));
        List<Event> events = List.of(event);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(
                eq("Party"), eq(true), eq(tags), eq("Roma (RM)"), any(), any())).thenReturn(events);

        // Act
        List<EventResponse> results = eventService.searchForEvents(session, search, tags, location, date, null, null, null);

        // Assert
        assertEquals(1, results.size());
        assertEquals("Informatica e Tecnologie", results.get(0).tags().get(0).macroTag());
        assertEquals("SQL", results.get(0).tags().get(0).microTag());
        assertEquals("Scienze Matematiche Fisiche e Naturali", results.get(0).tags().get(1).macroTag());
        assertEquals("Fisica", results.get(0).tags().get(1).microTag());
        verify(eventRepository).findByDynamicFilters(eq("Party"), eq(true), eq(tags), eq("Roma (RM)"), any(), any());
    }

    @Test
    void testSearchForEvents_TagsWithAll_ShouldIgnoreTags() {
        List<String> tags = List.of("Informatica e Tecnologie > SQL", "All");

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(
                any(), eq(false), any(), any(), any(), any())).thenReturn(new ArrayList<>());

        eventService.searchForEvents(session, null, tags, null, null, null, null, null);

        verify(eventRepository).findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull());
    }

    @Test
    void testSearchForEvents_FilterPastEvents_Success() {
        // Arrange
        Event pastEvent = new Event();
        setupMockEvent(pastEvent, 1L, LocalDateTime.now().minusDays(1));

        Event futureEvent = new Event();
        setupMockEvent(futureEvent, 2L, LocalDateTime.now().plusDays(1));

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(any(), anyBoolean(), any(),
                any(), any(), any())).thenReturn(List.of(pastEvent, futureEvent));

        // Act
        List<EventResponse> results = eventService.searchForEvents(session, null, null, null, null, null, null, null);

        // Assert
        assertEquals(1, results.size());

        assertEquals(2L, results.get(0).id());
    }

    @Test
    void testSearchForEvents_BlankStrings_ShouldBeConvertedToNull() {
        // Arrange
        String search = "   ";
        String location = "";

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(
                isNull(), anyBoolean(), any(), isNull(), any(), any()))
                .thenReturn(new ArrayList<>());

        // Act
        eventService.searchForEvents(session, search, null, location, null, null, null, null);

        // Assert
        verify(eventRepository).findByDynamicFilters(isNull(), anyBoolean(),
                any(), isNull(), any(), any());
    }
}
