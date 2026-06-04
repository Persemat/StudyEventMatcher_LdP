package com.example.backend.unit.event;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.backend.model.Event;
import com.example.backend.model.User;

public class DeleteBookingTest extends BaseEventTest {

    @Test
    void testCancelBooking_Success() {
        User user = new User();
        user.setId(1L);
        user.setName("Mario");

        User creator = new User();
        creator.setId(2L);
        creator.setName("Luca");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Software Engineering Study");
        event.setMaxParticipants(3);
        event.setStatus("open");
        event.setParticipants(new HashSet<>());
        event.addParticipant(creator);
        event.addParticipant(user);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        eventService.cancelBooking(10L, session);

        assertEquals(1, event.getParticipants().size());
        assertFalse(event.getParticipants().contains(user));
    }

    @Test
    void testCancelBooking_UnauthenticatedUser() {
        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("Sessione invalida o scaduta", ex.getMessage());

        verify(eventRepository, never()).findById(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCancelBooking_UserNotFound() {
        when(session.getAttribute("userId")).thenReturn(1L);

        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("Utente non trovato", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCancelBooking_EventNotFound() {
        User user = new User();
        user.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("Evento non trovato", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCancelBooking_UserNotBooked() {
        User user = new User();
        user.setId(1L);
        user.setName("Mario");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Network Study");
        event.setParticipants(new HashSet<>());

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("Non ti sei prenotato per questo evento", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }
}