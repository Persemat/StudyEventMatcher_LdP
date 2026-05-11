package com.example.backend.unit.event;

import com.example.backend.dto.EventRequest;
import com.example.backend.model.Event;
import com.example.backend.model.User;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class CreateEventTest extends BaseEventTest {

    @Test
    void createEvent_shouldSucceed() {

        EventRequest request = new EventRequest(
                "Math",
                LocalDateTime.parse("2026-10-01T15:00:00"),
                2.0,
                "Library",
                List.of("math"),
                "Revision",
                5,
                true
        );

        User creator = new User();
        creator.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(creator));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.createEvent(request, session);

        assertNotNull(result);
        assertEquals("Math", result.getTitle());
        assertEquals("open", result.getStatus());
        assertEquals(creator, result.getCreator());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void createEvent_shouldFail_whenSessionIsMissing() {

        EventRequest request = new EventRequest(
                "Math",
                LocalDateTime.now(),
                2.0,
                "Room 1",
                List.of("test"),
                "desc",
                5,
                true
        );

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.createEvent(request, session));

        assertEquals("Sessione invalida o scaduta", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void createEvent_shouldFail_whenUserNotFound() {

        EventRequest request = new EventRequest(
                "Study Session",
                LocalDateTime.parse("2026-04-10T15:00:00"),
                2.0,
                "Room 1",
                List.of("Math"),
                "Revision",
                5,
                true
        );

        when(session.getAttribute("userId")).thenReturn(99L);
        when(userRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.createEvent(request, session));

        assertTrue(ex.getMessage().contains("Utente non trovato"));

        verify(eventRepository, never()).save(any());
    }
}