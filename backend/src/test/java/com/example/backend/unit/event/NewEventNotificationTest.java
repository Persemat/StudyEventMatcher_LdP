package com.example.backend.unit.event;

import com.example.backend.dto.EventRequest;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.service.CityCoordinateService;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class NewEventNotificationTest extends BaseEventTest {

    @Test
    void createEvent_shouldNotifyInterestedNearbyUser() {
        User creator = user(1L, "creator@example.com", "Milano", List.of("Informatica"), List.of("Machine Learning"));
        User interestedUser = user(2L, "interested@example.com", "Milano", List.of("Informatica"), List.of("Machine Learning"));
        EventRequest request = eventRequest("Introduzione al Machine Learning",
                List.of(new EventTag("Informatica", "Machine Learning")));

        prepareCreateEvent(creator, interestedUser);

        Event result = eventService.createEvent(request, session);

        assertNotNull(result);
        assertEquals("Introduzione al Machine Learning", result.getTitle());
        verify(emailService, times(1)).sendNewRelevantEventEmail(interestedUser.getEmail(), result);
    }

    @Test
    void createEvent_shouldNotNotifyUserWithDifferentTags() {
        User creator = user(1L, "creator@example.com", "Milano", List.of("Informatica"), List.of("Machine Learning"));
        User uninterestedUser = user(2L, "uninterested@example.com", "Milano", List.of("Giurisprudenza"), List.of("Diritto Privato"));
        EventRequest request = eventRequest("Introduzione al Machine Learning",
                List.of(new EventTag("Informatica", "Machine Learning")));

        prepareCreateEvent(creator, uninterestedUser);

        eventService.createEvent(request, session);

        verify(emailService, never()).sendNewRelevantEventEmail(eq(uninterestedUser.getEmail()), any(Event.class));
    }

    @Test
    void createEvent_shouldSendSingleEmailWhenUserMatchesMultipleTags() {
        User creator = user(1L, "creator@example.com", "Milano", List.of("Informatica"),
                List.of("Machine Learning", "Sicurezza Informatica"));
        User interestedUser = user(2L, "interested@example.com", "Milano", List.of("Informatica"),
                List.of("Machine Learning", "Sicurezza Informatica"));
        EventRequest request = eventRequest("AI e Cybersecurity",
                List.of(
                        new EventTag("Informatica", "Machine Learning"),
                        new EventTag("Informatica", "Sicurezza Informatica")));

        prepareCreateEvent(creator, interestedUser);

        Event result = eventService.createEvent(request, session);

        verify(emailService, times(1)).sendNewRelevantEventEmail(interestedUser.getEmail(), result);
    }

    @Test
    void createEvent_shouldSaveEventWhenNotificationFails() {
        User creator = user(1L, "creator@example.com", "Milano", List.of("Informatica"), List.of("Machine Learning"));
        User interestedUser = user(2L, "interested@example.com", "Milano", List.of("Informatica"), List.of("Machine Learning"));
        EventRequest request = eventRequest("Introduzione al Machine Learning",
                List.of(new EventTag("Informatica", "Machine Learning")));

        prepareCreateEvent(creator, interestedUser);
        doThrow(new RuntimeException("SMTP down"))
                .when(emailService).sendNewRelevantEventEmail(any(String.class), any(Event.class));

        Event result = eventService.createEvent(request, session);

        assertNotNull(result);
        verify(eventRepository, times(1)).save(any(Event.class));
        verify(emailService, times(1)).sendNewRelevantEventEmail(interestedUser.getEmail(), result);
    }

    private void prepareCreateEvent(User creator, User otherUser) {
        when(session.getAttribute("userId")).thenReturn(creator.getId());
        when(userRepository.findById(creator.getId())).thenReturn(Optional.of(creator));
        when(eventRepository.findByCreatorIdAndTitleAndDateTimeAndLocation(
                any(Long.class), any(String.class), any(LocalDateTime.class), any(String.class)))
                .thenReturn(Optional.empty());
        when(eventRepository.save(any(Event.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(userRepository.findAll()).thenReturn(List.of(creator, otherUser));
        lenient().when(cityCoordinateService.findCoordinates("Milano"))
                .thenReturn(Optional.of(new CityCoordinateService.Coordinates(45.4642, 9.1900)));
    }

    private EventRequest eventRequest(String title, List<EventTag> tags) {
        return new EventRequest(
                title,
                LocalDateTime.parse("2026-06-10T15:00:00"),
                2.0,
                "Milano (MI)-Aula 1",
                45.4642,
                9.1900,
                tags,
                "Evento di test",
                10,
                true
        );
    }

    private User user(Long id, String email, String favouriteCity, List<String> macroTags, List<String> microTags) {
        User user = new User("Test", "User", email, "Password123!", favouriteCity, macroTags, microTags);
        user.setId(id);
        return user;
    }
}
