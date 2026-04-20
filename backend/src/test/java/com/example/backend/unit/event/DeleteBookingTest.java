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
        // Simulate a user booked for the event
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

        // Execute booking cancellation
        eventService.cancelBooking(10L, session);

        // Verify user has been removed from participants
        assertEquals(1, event.getParticipants().size());
        assertFalse(event.getParticipants().contains(user));
    }

    @Test
    void testCancelBooking_UnauthenticatedUser() {
        // Empty session
        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("User not authenticated", ex.getMessage());

        // Ensure DB is never accessed
        verify(eventRepository, never()).findById(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCancelBooking_UserNotFound() {
        when(session.getAttribute("userId")).thenReturn(1L);

        // DB does not find user
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("User not found", ex.getMessage());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testCancelBooking_EventNotFound() {
        User user = new User();
        user.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        // DB does not find event
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        assertEquals("Event not found", ex.getMessage());
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
        // IMPORTANT: Mario is NOT booked for this event

        when(session.getAttribute("userId")).thenReturn(1L);
        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.cancelBooking(10L, session);
        });

        // Message required by Cucumber user story
        assertEquals("You are not booked for this event", ex.getMessage());

        // Event must not be saved due to error
        verify(eventRepository, never()).save(any());
    }
}