package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.backend.model.Event;
import com.example.backend.model.User;


public class CreateBookingTest extends BaseEventTest {

    @Test
    void testBookEvent_Success() {
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

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        eventService.bookEvent(10L, session);

        assertEquals(2, event.getParticipants().size());
        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void testBookEvent_UnauthenticatedUser() {
        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.bookEvent(10L, session);
        });

        assertEquals("Sessione invalida o scaduta", ex.getMessage());
        verify(eventRepository, never()).findById(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testBookEvent_UserNotFound() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.bookEvent(10L, session);
        });

        assertEquals("Utente non trovato", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testBookEvent_EventNotFound() {
        User user = new User();
        user.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.bookEvent(10L, session);
        });

        assertEquals("Evento non trovato", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testBookEvent_AlreadyBooked() {
        User user = new User();
        user.setId(1L);
        user.setName("Mario");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Java Study");
        event.setMaxParticipants(5);
        event.setStatus("open");
        event.setParticipants(new HashSet<>());
        event.addParticipant(user);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.bookEvent(10L, session);
        });

        assertEquals("Sei già prenotato per questo evento", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testBookEvent_EventFull() {
        User user = new User();
        user.setId(1L);
        user.setName("Mario");

        User creator = new User();
        creator.setId(2L);
        creator.setName("Luca");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Database Project");
        event.setMaxParticipants(1);
        event.setStatus("open");
        event.setParticipants(new HashSet<>());
        event.addParticipant(creator);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.bookEvent(10L, session);
        });

        assertEquals("L'evento è pieno", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testBookEvent_EventClosed() {
        User user = new User();
        user.setId(1L);
        user.setName("Mario");

        User creator = new User();
        creator.setId(2L);
        creator.setName("Luca");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Network Study");
        event.setMaxParticipants(5);
        event.setStatus("closed");
        event.setParticipants(new HashSet<>());
        event.addParticipant(creator);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.bookEvent(10L, session);
        });

        assertEquals("Le prenotazioni non sono disponibili per questo evento", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }
}
