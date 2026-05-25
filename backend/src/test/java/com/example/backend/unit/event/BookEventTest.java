package com.example.backend.unit.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class BookEventTest extends BaseEventTest {

    @Test
    void bookEvent_shouldSucceed() {

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
    void bookEvent_shouldFail_whenUserNotAuthenticated() {

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.bookEvent(10L, session));

        assertEquals("Sessione invalida o scaduta", ex.getMessage());

        verify(eventRepository, never()).findById(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void bookEvent_shouldFail_whenUserNotFound() {

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.bookEvent(10L, session));

        assertEquals("Utente non trovato", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void bookEvent_shouldFail_whenEventNotFound() {

        User user = new User();
        user.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.bookEvent(10L, session));

        assertEquals("Evento non trovato", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void bookEvent_shouldFail_whenAlreadyBooked() {

        User user = new User();
        user.setId(1L);

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

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.bookEvent(10L, session));

        assertEquals("Sei già prenotato per questo evento", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void bookEvent_shouldFail_whenEventFull() {

        User user = new User();
        user.setId(1L);

        User creator = new User();
        creator.setId(2L);

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

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.bookEvent(10L, session));

        assertEquals("L'evento è pieno", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void bookEvent_shouldFail_whenEventClosed() {

        User user = new User();
        user.setId(1L);

        User creator = new User();
        creator.setId(2L);

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

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.bookEvent(10L, session));

        assertEquals("Le prenotazioni non sono disponibili per questo evento", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }
}