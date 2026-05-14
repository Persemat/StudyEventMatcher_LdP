package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.EventRequest;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;

public class EditEventTest extends BaseEventTest {

    @Test
    void testEditEvent_Success() {
        EventRequest request = new EventRequest(
                "Updated Java Project",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Milano (MI)-Biblioteca",
                List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione"), new com.example.backend.model.EventTag("Informatica e Tecnologie", "Sviluppo backend")),
                "Updated description",
                12,
                false
        );

        User creator = new User();
        creator.setId(1L);

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Java Project");
        event.setDateTime(LocalDateTime.parse("2026-10-15T15:00:00"));
        event.setDuration(2.5);
        event.setLocation("Milano (MI)-Aula Magna");
        event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
        event.setDescription("Review");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setCreator(creator);
        event.setParticipants(new HashSet<>(Set.of(creator)));

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));
        when(eventRepository.save(any(Event.class))).thenAnswer(i -> i.getArgument(0));

        Event result = eventService.editEvent(10L, request, session);

        assertNotNull(result);
        assertEquals("Updated Java Project", result.getTitle());
        assertEquals(LocalDateTime.parse("2026-10-16T17:00:00"), result.getDateTime());
        assertEquals(3.0, result.getDuration());
        assertEquals("Milano (MI)-Biblioteca", result.getLocation());
        assertEquals(List.of(
                new EventTag("Informatica e Tecnologie", "Programmazione"),
                new EventTag("Informatica e Tecnologie", "Sviluppo backend")), result.getTags());
        assertEquals("Updated description", result.getDescription());
        assertEquals(12, result.getMaxParticipants());
        assertEquals(false, result.isCreatorParticipationRequired());

        verify(eventRepository, times(1)).save(any(Event.class));
    }

    @Test
    void testEditEvent_MissingSession() {
        EventRequest request = new EventRequest(
                "Updated Java Project",
                LocalDateTime.now().plusDays(2),
                3.0,
                "Milano (MI)-Biblioteca",
                List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")),
                "Updated description",
                12,
                false
        );

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.editEvent(10L, request, session);
        });

        assertEquals("Sessione invalida o scaduta", ex.getMessage());
        verify(eventRepository, never()).findById(any());
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testEditEvent_EventNotFound() {
        EventRequest request = new EventRequest(
                "Updated Java Project",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Milano (MI)-Biblioteca",
                List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")),
                "Updated description",
                12,
                false
        );

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.editEvent(10L, request, session);
        });

        assertTrue(ex.getMessage().contains("Evento non trovato"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testEditEvent_UserNotCreator() {
        EventRequest request = new EventRequest(
                "Updated Java Project",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Milano (MI)-Biblioteca",
                List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")),
                "Updated description",
                12,
                false
        );

        User creator = new User();
        creator.setId(2L);

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Java Project");
        event.setCreator(creator);
        event.setParticipants(new HashSet<>(Set.of(creator)));

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.editEvent(10L, request, session);
        });

        assertTrue(ex.getMessage().contains("Solo il creatore può modificare l'evento"));
        verify(eventRepository, never()).save(any());
    }

    @Test
    void testEditEvent_MaxParticipantsLowerThanCurrent() {
        EventRequest request = new EventRequest(
                "Updated Java Project",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Milano (MI)-Biblioteca",
                List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")),
                "Updated description",
                2,
                false
        );

        User creator = new User();
        creator.setId(1L);

        User participant1 = new User();
        participant1.setId(2L);

        User participant2 = new User();
        participant2.setId(3L);

        Event event = new Event();
        event.setId(10L);
        event.setTitle("Java Project");
        event.setDateTime(LocalDateTime.now().plusDays(3));
        event.setDuration(2.5);
        event.setLocation("Milano (MI)-Aula Magna");
        event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
        event.setDescription("Review");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setStatus("open");
        event.setCreator(creator);

        event.setParticipants(new HashSet<>());
        event.getParticipants().add(creator);
        event.getParticipants().add(participant1);
        event.getParticipants().add(participant2);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findById(10L)).thenReturn(Optional.of(event));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventService.editEvent(10L, request, session);
        });

        assertEquals(
                "Il numero massimo di partecipanti non può essere minore dei partecipanti attuali",
                ex.getMessage()
        );

        verify(eventRepository, never()).save(any());
    }
}
