package com.example.backend.unit.event;

import com.example.backend.model.Event;
import com.example.backend.model.User;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class FavoriteEventTest extends BaseEventTest {

    @Test
    void toggleFavorite_shouldAddFavorite() {

        User user = new User();
        user.setId(1L);
        user.setName("Mario");
        user.setFavouriteCity("Milano");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Software Engineering Study");
        event.setFavorites(new HashSet<>());

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        eventService.toggleFavorite(10L, session);

        assertTrue(event.getFavorites().contains(user));
        assertEquals(1, event.getFavorites().size());

        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void toggleFavorite_shouldRemoveFavorite() {

        User user = new User();
        user.setId(1L);
        user.setName("Mario");
        user.setFavouriteCity("Milano");

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Database Study");
        event.setFavorites(new HashSet<>());

        event.addFavorite(user);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        eventService.toggleFavorite(10L, session);

        assertFalse(event.getFavorites().contains(user));
        assertEquals(0, event.getFavorites().size());

        verify(eventRepository, times(1)).save(event);
    }

    @Test
    void toggleFavorite_shouldFail_whenUserNotAuthenticated() {

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.toggleFavorite(10L, session));

        assertEquals("Utente non autenticato", ex.getMessage());

        verify(eventRepository, never()).findById(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void toggleFavorite_shouldFail_whenEventNotFound() {

        User user = new User();
        user.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.toggleFavorite(10L, session));

        assertEquals("Evento non trovato", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void toggleFavorite_shouldFail_whenUserNotFound() {

        Event event = new Event();
        event.setId(10L);
        event.setFavorites(new HashSet<>());

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventService.toggleFavorite(10L, session));

        assertEquals("Utente non trovato", ex.getMessage());

        verify(eventRepository, never()).save(any());
    }

    @Test
    void checkIfClosed_shouldRemoveFavoritesWhenEventHasStarted() {

        User user = new User();
        user.setId(1L);

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Started event");
        event.setDateTime(LocalDateTime.now().minusMinutes(1));
        event.setStatus("open");
        event.setFavorites(new HashSet<>());
        event.addFavorite(user);

        boolean changed = eventService.checkIfClosed(false, List.of(event));

        assertTrue(changed);
        assertEquals("closed", event.getStatus());
        assertTrue(event.getFavorites().isEmpty());
        assertFalse(user.getFavoriteEvents().contains(event));
    }
}
