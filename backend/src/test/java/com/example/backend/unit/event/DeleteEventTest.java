package com.example.backend.unit.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

public class DeleteEventTest extends BaseEventTest {

    @Test
    void deleteEvent_shouldDeleteEvent_whenCreatorParticipationIsRequired() {

        User creator = new User();
        creator.setId(1L);
        creator.setName("Luca");
        creator.setLastname("Rossi");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Test Event");
        event.setCreator(creator);
        event.setCreatorParticipationRequired(true);
        event.setParticipants(new HashSet<>());

        event.addParticipant(creator);

        for (long i = 2; i <= 6; i++) {
            User u = new User();
            u.setId(i);
            u.setName("User" + i);
            event.addParticipant(u);
        }

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        String result = eventService.deleteEvent(10L, session);

        assertEquals("Evento cancellato con successo", result);
        verify(eventRepository).delete(event);
        assertTrue(event.getParticipants().isEmpty());
    }

    @Test
    void deleteEvent_shouldReassignCreator_whenNoMandatoryParticipationAndHasOtherParticipants() {

        User creator = new User();
        creator.setId(1L);
        creator.setName("Luca");

        User user2 = new User();
        user2.setId(2L);
        user2.setName("Marco");

        User user3 = new User();
        user3.setId(3L);
        user3.setName("Anna");

        Event event = new Event();
        event.setId(11L);
        event.setTitle("Test Event");
        event.setCreator(creator);
        event.setCreatorParticipationRequired(false);
        event.setParticipants(new HashSet<>());

        event.addParticipant(creator);
        event.addParticipant(user2);
        event.addParticipant(user3);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(11L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String result = eventService.deleteEvent(11L, session);

        assertEquals("Creatore rimosso e ruolo riassegnato con successo", result);
        assertEquals(user2, event.getCreator());
        assertFalse(event.getParticipants().contains(creator));
        assertTrue(event.getParticipants().contains(user2));
        assertTrue(event.getParticipants().contains(user3));

        verify(eventRepository, never()).delete(any());
        verify(eventRepository).save(event);
    }

    @Test
    void deleteEvent_shouldDeleteEvent_whenNoMandatoryParticipationAndNoOtherParticipants() {

        User creator = new User();
        creator.setId(1L);
        creator.setName("Luca");

        Event event = new Event();
        event.setId(12L);
        event.setTitle("Test Event");
        event.setCreator(creator);
        event.setCreatorParticipationRequired(false);
        event.setParticipants(new HashSet<>());

        event.addParticipant(creator);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(12L)).thenReturn(Optional.of(event));

        String result = eventService.deleteEvent(12L, session);

        assertEquals("Evento cancellato con successo", result);
        verify(eventRepository).delete(event);
    }

    @Test
    void deleteEvent_shouldThrowException_whenUserIsNotCreator() {

        User creator = new User();
        creator.setId(2L);
        creator.setName("Marco");

        Event event = new Event();
        event.setId(13L);
        event.setCreator(creator);
        event.setCreatorParticipationRequired(true);
        event.setParticipants(new HashSet<>());

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(13L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.deleteEvent(13L, session));

        assertEquals("Solo il creatore può cancellare l'evento", ex.getMessage());

        verify(eventRepository, never()).delete(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void deleteEvent_shouldThrowException_whenEventNotFound() {

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.deleteEvent(99L, session));

        assertEquals("Evento non trovato", ex.getMessage());

        verify(eventRepository, never()).delete(any());
    }

    @Test
    void deleteEvent_shouldThrowException_whenSessionIsMissing() {

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.deleteEvent(10L, session));

        assertEquals("Sessione invalida o scaduta", ex.getMessage());

        verify(eventRepository, never()).findById(anyLong());
    }
}
