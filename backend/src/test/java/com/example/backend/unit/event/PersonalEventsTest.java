package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.EventResponse;
import com.example.backend.model.Event;
import com.example.backend.model.User;

public class PersonalEventsTest extends BaseEventTest {

    @Test
    void getAllEvents_authenticatedUser_withPersonalEvents_returnsMixedList() {

        User user = new User();
        user.setId(1L);
        user.setName("Mario");
        user.setLastname("Rossi");
        user.setEmail("mario@test.com");
        user.setPassword("Password123!");

        User otherUser = new User();
        otherUser.setId(2L);
        otherUser.setName("Luigi");
        otherUser.setLastname("Verdi");
        otherUser.setEmail("luigi@test.com");
        otherUser.setPassword("Password123!");

        Event myEvent = new Event();
        myEvent.setTitle("My Event");
        myEvent.setDateTime(LocalDateTime.of(2026, 4, 20, 15, 0));
        myEvent.setLocation("Room 1");
        myEvent.setDuration(2.0);
        myEvent.setTags(List.of("Java"));
        myEvent.setDescription("Description");
        myEvent.setStatus("open");
        myEvent.setCreator(user);

        Event otherEvent = new Event();
        otherEvent.setTitle("Other Event");
        otherEvent.setDateTime(LocalDateTime.of(2026, 4, 21, 16, 0));
        otherEvent.setLocation("Room 2");
        otherEvent.setDuration(1.5);
        otherEvent.setTags(List.of("React"));
        otherEvent.setDescription("Description");
        otherEvent.setStatus("open");
        otherEvent.setCreator(otherUser);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findAll()).thenReturn(List.of(myEvent, otherEvent));

        List<EventResponse> result = eventService.getAllEvents(session);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(EventResponse::isCreatedByMe));
        assertTrue(result.stream().anyMatch(e -> !e.isCreatedByMe()));
    }

    @Test
    void getAllEvents_authenticatedUser_withoutPersonalEvents_returnsOnlyOthers() {

        User otherUser = new User();
        otherUser.setId(2L);

        Event otherEvent = new Event();
        otherEvent.setTitle("Other Event");
        otherEvent.setDateTime(LocalDateTime.of(2026, 4, 21, 16, 0));
        otherEvent.setLocation("Room 2");
        otherEvent.setDuration(1.5);
        otherEvent.setTags(List.of("React"));
        otherEvent.setDescription("Description");
        otherEvent.setStatus("open");
        otherEvent.setCreator(otherUser);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findAll()).thenReturn(List.of(otherEvent));

        List<EventResponse> result = eventService.getAllEvents(session);

        assertEquals(1, result.size());
        assertTrue(result.stream().noneMatch(EventResponse::isCreatedByMe));
    }

    @Test
    void getAllEvents_shouldMapFieldsCorrectly() {

        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setTitle("Database Study");
        event.setDateTime(LocalDateTime.of(2026, 4, 10, 15, 0));
        event.setLocation("Room 2");
        event.setDuration(2.0);
        event.setTags(List.of("CS"));
        event.setDescription("SQL Review");
        event.setStatus("open");
        event.setCreator(user);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findAll()).thenReturn(List.of(event));

        List<EventResponse> result = eventService.getAllEvents(session);

        assertEquals(1, result.size());

        EventResponse response = result.get(0);
        assertNotNull(response.title());
        assertNotNull(response.dateTime());
        assertNotNull(response.location());
        assertNotNull(response.tags());
    }
}