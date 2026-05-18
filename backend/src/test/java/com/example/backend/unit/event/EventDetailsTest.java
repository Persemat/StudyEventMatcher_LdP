package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.EventResponse;
import com.example.backend.model.Event;
import com.example.backend.model.User;

public class EventDetailsTest extends BaseEventTest {

    @Test
    void getEventDetails_Success() {
        
        Long mockUserId = 1L;
        Long mockEventId = 10L;

        when(session.getAttribute("userId")).thenReturn(mockUserId);

        User creator = new User();
        creator.setId(mockUserId);
        creator.setName("Mario");
        creator.setLastname("Rossi");

        Event mockEvent = new Event();
        mockEvent.setId(mockEventId);
        mockEvent.setTitle("React Workshop");
        mockEvent.setDescription("Detailed description of the event");
        mockEvent.setLocation("Main Library");
        mockEvent.setDateTime(LocalDateTime.now().plusDays(5));
        mockEvent.setDuration(2.0);
        mockEvent.setMaxParticipants(20);
        mockEvent.setStatus("open");
        mockEvent.setCreator(creator);
        mockEvent.addParticipant(creator);

        when(eventRepository.findById(mockEventId)).thenReturn(Optional.of(mockEvent));

        EventResponse response = eventService.getEventDetails(mockEventId, session);

        assertNotNull(response, "The response should not be null");
        assertEquals("React Workshop", response.title(), "The title should match");
        assertEquals("Detailed description of the event", response.description(), "The description should match");
        assertEquals(1, response.currentParticipants(), "There should be exactly 1 participant");
        assertEquals(1, response.participants().size(), "The participants list in the response should have 1 element");
        assertTrue(response.isBooked(), "The user should be recognized as booked");
        assertTrue(response.isCreatedByMe(), "The user should be recognized as the creator");

        verify(eventRepository, times(1)).findById(mockEventId);
    }

    @Test
    void getEventDetails_ThrowsException_WhenNotAuthenticated() {
        
        Long mockEventId = 10L;

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.getEventDetails(mockEventId, session);
        });

        assertEquals("Sessione invalida o scaduta", exception.getMessage());

        verify(eventRepository, never()).findById(any());
    }

    @Test
    void getEventDetails_ThrowsException_WhenEventNotFound() {
        
        Long mockUserId = 1L;
        Long mockEventId = 99L;

        when(session.getAttribute("userId")).thenReturn(mockUserId);

        when(eventRepository.findById(mockEventId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            eventService.getEventDetails(mockEventId, session);
        });

        assertEquals("Evento non trovato", exception.getMessage());
        
        verify(eventRepository, times(1)).findById(mockEventId);
    }
}
