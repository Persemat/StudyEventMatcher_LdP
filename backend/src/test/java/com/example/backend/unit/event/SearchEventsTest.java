package com.example.backend.unit.event;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyBoolean;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.EventResponse;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.dto.EventSearchFilter;

public class SearchEventsTest extends BaseEventTest {

    private void setupMockEvent(Event event, Long id, LocalDateTime dateTime) {
        User creator = new User();
        creator.setId(99L);
        creator.setName("Admin");
        creator.setLastname("User");

        event.setId(id);
        event.setDateTime(dateTime);
        event.setCreator(creator);
        event.setParticipants(new HashSet<>());
        event.setTags(new ArrayList<>());
        event.setStatus("open");
    }

    private Event createSearchEvent(Long id, String title, LocalDateTime dateTime, double duration) {
        Event event = new Event();
        setupMockEvent(event, id, dateTime);
        event.setTitle(title);
        event.setDuration(duration);
        return event;
    }

    @Test
    void testSearchForEvents_FullFilters_Success() {
       String search = "Party";
        List<String> tags = List.of("Informatica e Tecnologie > SQL", "Scienze Matematiche Fisiche e Naturali > Fisica");
        String location = "Roma (RM)";
        String date = "2026-05-20";

        Event event = new Event();
        setupMockEvent(event, 1L, LocalDateTime.now().plusDays(1));
        event.setTags(List.of(
                new EventTag("Informatica e Tecnologie", "SQL"),
                new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")));
        List<Event> events = List.of(event);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(
                eq("Party"), eq(true), eq(tags), eq("Roma (RM)"), any(), any())).thenReturn(events);

        EventSearchFilter filter = new EventSearchFilter(search, tags, location, date, null, null, null, false);
        List<EventResponse> results = eventService.searchForEvents(session, filter);

        assertEquals(1, results.size());
        assertEquals("Informatica e Tecnologie", results.get(0).tags().get(0).macroTag());
        assertEquals("SQL", results.get(0).tags().get(0).microTag());
        assertEquals("Scienze Matematiche Fisiche e Naturali", results.get(0).tags().get(1).macroTag());
        assertEquals("Fisica", results.get(0).tags().get(1).microTag());
        verify(eventRepository).findByDynamicFilters(eq("Party"), eq(true), eq(tags), eq("Roma (RM)"), any(), any());
    }

    @Test
    void testSearchForEvents_TagsWithAll_ShouldIgnoreTags() {
        List<String> tags = List.of("Informatica e Tecnologie > SQL", "All");

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(
                any(), eq(false), any(), any(), any(), any())).thenReturn(new ArrayList<>());

        EventSearchFilter filter = new EventSearchFilter(null, tags, null, null, null, null, null, false);
        eventService.searchForEvents(session, filter);

        verify(eventRepository).findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull());}

    @Test
    void testSearchForEvents_FilterPastEvents_Success() {
        // Arrange
        Event pastEvent = new Event();
        setupMockEvent(pastEvent, 1L, LocalDateTime.now().minusDays(1));

        Event futureEvent = new Event();
        setupMockEvent(futureEvent, 2L, LocalDateTime.now().plusDays(1));

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(any(), anyBoolean(), any(),
                any(), any(), any())).thenReturn(List.of(pastEvent, futureEvent));

        EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, false);
        List<EventResponse> results = eventService.searchForEvents(session, filter);
        
        assertEquals(1, results.size());
        assertEquals(2L, results.get(0).id());
    }

    @Test
    void testSearchForEvents_BlankStrings_ShouldBeConvertedToNull() {
        // Arrange
        String search = "   ";
        String location = "";

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventRepository.findByDynamicFilters(
                isNull(), anyBoolean(), any(), isNull(), any(), any()))
                .thenReturn(new ArrayList<>());

        EventSearchFilter filter = new EventSearchFilter(search, null, location, null, null, null, null, false);
        List<EventResponse> results = eventService.searchForEvents(session, filter);

        // Assert
        verify(eventRepository).findByDynamicFilters(isNull(), anyBoolean(),
                any(), isNull(), any(), any());
    }
    @Test
void testSearchForEvents_DistanceFilter_ShouldIncludeEventsWithinRadius() {
    String location = "Milano (MI)";
    double milanLat = 45.4642;
    double milanLon = 9.1900;
    int distanceKm = 50;

    Event milanEvent = new Event();
    setupMockEvent(milanEvent, 1L, LocalDateTime.now().plusDays(1));
    milanEvent.setLatitude(45.4642);
    milanEvent.setLongitude(9.1900);

    Event monzaEvent = new Event();
    setupMockEvent(monzaEvent, 2L, LocalDateTime.now().plusDays(1));
    monzaEvent.setLatitude(45.5845);
    monzaEvent.setLongitude(9.2744);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(
            isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()
    )).thenReturn(List.of(milanEvent, monzaEvent));

    EventSearchFilter filter = new EventSearchFilter(null, null, location, null, null, null, null, false);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(2, results.size());
}

@Test
void testSearchForEvents_DistanceFilter_ShouldExcludeEventsOutsideRadius() {
    String location = "Milano (MI)";
    double milanLat = 45.4642;
    double milanLon = 9.1900;
    int distanceKm = 50;

    Event romeEvent = new Event();
    setupMockEvent(romeEvent, 1L, LocalDateTime.now().plusDays(1));
    romeEvent.setLatitude(41.9028);
    romeEvent.setLongitude(12.4964);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(
            isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()
    )).thenReturn(List.of(romeEvent));

    EventSearchFilter filter = new EventSearchFilter(null, null, location, null, milanLat, milanLon, distanceKm, false);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(0, results.size());
}

@Test
void testSearchForEvents_DistanceFilter_ShouldUpdateResultsWhenRadiusChanges() {
    String location = "Milano (MI)";
    double milanLat = 45.4642;
    double milanLon = 9.1900;

    Event milanEvent = new Event();
    setupMockEvent(milanEvent, 1L, LocalDateTime.now().plusDays(1));
    milanEvent.setLatitude(45.4642);
    milanEvent.setLongitude(9.1900);

    Event bergamoEvent = new Event();
    setupMockEvent(bergamoEvent, 2L, LocalDateTime.now().plusDays(1));
    bergamoEvent.setLatitude(45.6983);
    bergamoEvent.setLongitude(9.6773);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(
            isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()
    )).thenReturn(List.of(milanEvent, bergamoEvent));

    EventSearchFilter filter20Km = new EventSearchFilter(null, null, location, null, milanLat, milanLon, 20, false);
    List<EventResponse> results20Km = eventService.searchForEvents(session, filter20Km);

    EventSearchFilter filter100Km = new EventSearchFilter(null, null, location, null, milanLat, milanLon, 100, false);
List<EventResponse> results100Km = eventService.searchForEvents(session, filter100Km);

    assertEquals(1, results20Km.size());
    assertEquals(2, results100Km.size());
}

@Test
void testSearchForEvents_DistanceFilter_ShouldExcludeEventsWithoutCoordinates() {
    String location = "Milano (MI)";
    double milanLat = 45.4642;
    double milanLon = 9.1900;
    int distanceKm = 50;

    Event eventWithoutCoordinates = new Event();
    setupMockEvent(eventWithoutCoordinates, 1L, LocalDateTime.now().plusDays(1));
    eventWithoutCoordinates.setLatitude(null);
    eventWithoutCoordinates.setLongitude(null);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(
            isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()
    )).thenReturn(List.of(eventWithoutCoordinates));

    EventSearchFilter filter = new EventSearchFilter(null, null, location, null, milanLat, milanLon, distanceKm, false);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(0, results.size());
}

@Test
void testSearchForEvents_DistanceFilter_ShouldUseTextLocationWhenCoordinatesAreMissing() {
    String location = "Milano (MI)";

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(
            isNull(), eq(false), eq(List.of()), eq(location), isNull(), isNull()
    )).thenReturn(List.of());

    EventSearchFilter filter = new EventSearchFilter(null, null, location, null, null, null, 50, false);
    eventService.searchForEvents(session, filter);

    verify(eventRepository).findByDynamicFilters(
            isNull(),
            eq(false),
            eq(List.of()),
            eq(location),
            isNull(),
            isNull()
    );
}

@Test
void testSearchForEvents_DistanceFilter_ShouldSkipTextLocationFilterWhenCoordinatesArePresent() {
    String location = "Milano (MI)";
    double milanLat = 45.4642;
    double milanLon = 9.1900;

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(
            isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()
    )).thenReturn(List.of());

    EventSearchFilter filter = new EventSearchFilter(null, null, location, null, milanLat, milanLon, 50, false);
    eventService.searchForEvents(session, filter);

    verify(eventRepository).findByDynamicFilters(
            isNull(),
            eq(false),
            eq(List.of()),
            isNull(),
            isNull(),
            isNull()
    );
}

@Test
void testSearchForEvents_HideOverlapping_ShouldExcludeOverlappingEvents() {
    LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Event bookedEvent = createSearchEvent(10L, "Analisi 1", baseTime, 2.0);
    Event overlappingEvent = createSearchEvent(11L, "Basi di Dati", baseTime.plusHours(1), 2.0);
    Event compatibleEvent = createSearchEvent(12L, "Programmazione Java", baseTime.plusHours(4), 2.0);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()))
            .thenReturn(List.of(overlappingEvent, compatibleEvent));
    when(eventRepository.findBookedByParticipantId(1L)).thenReturn(List.of(bookedEvent));

   EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, true);
        List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(1, results.size());
    assertEquals("Programmazione Java", results.get(0).title());
}

@Test
void testSearchForEvents_HideOverlapping_ShouldKeepNonOverlappingEvents() {
    LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Event bookedEvent = createSearchEvent(10L, "Analisi 1", baseTime, 2.0);
    Event firstCompatibleEvent = createSearchEvent(11L, "Basi di Dati", baseTime.plusHours(3), 2.0);
    Event secondCompatibleEvent = createSearchEvent(12L, "Programmazione Java", baseTime.plusHours(6), 2.0);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()))
            .thenReturn(List.of(firstCompatibleEvent, secondCompatibleEvent));
    when(eventRepository.findBookedByParticipantId(1L)).thenReturn(List.of(bookedEvent));

    EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, true);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(2, results.size());
    assertEquals("Basi di Dati", results.get(0).title());
    assertEquals("Programmazione Java", results.get(1).title());
}

@Test
void testSearchForEvents_HideOverlapping_ShouldKeepAllEventsWhenUserHasNoBookings() {
    LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Event firstEvent = createSearchEvent(11L, "Basi di Dati", baseTime, 2.0);
    Event secondEvent = createSearchEvent(12L, "Programmazione Java", baseTime.plusHours(1), 2.0);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()))
            .thenReturn(List.of(firstEvent, secondEvent));
    when(eventRepository.findBookedByParticipantId(1L)).thenReturn(List.of());

    EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, true);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(2, results.size());
}

@Test
void testSearchForEvents_HideOverlapping_ShouldReturnEmptyWhenAllEventsOverlap() {
    LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Event bookedEvent = createSearchEvent(10L, "Analisi 1", baseTime, 2.0);
    Event firstOverlappingEvent = createSearchEvent(11L, "Basi di Dati", baseTime.plusMinutes(30), 1.0);
    Event secondOverlappingEvent = createSearchEvent(12L, "Programmazione Java", baseTime.plusHours(1), 2.0);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()))
            .thenReturn(List.of(firstOverlappingEvent, secondOverlappingEvent));
    when(eventRepository.findBookedByParticipantId(1L)).thenReturn(List.of(bookedEvent));

    EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, true);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(0, results.size());
}

@Test
void testSearchForEvents_HideOverlapping_ShouldKeepAdjacentEvents() {
    LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Event bookedEvent = createSearchEvent(10L, "Analisi 1", baseTime, 2.0);
    Event adjacentEvent = createSearchEvent(11L, "Basi di Dati", baseTime.plusHours(2), 1.0);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()))
            .thenReturn(List.of(adjacentEvent));
    when(eventRepository.findBookedByParticipantId(1L)).thenReturn(List.of(bookedEvent));

    EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, true);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(1, results.size());
    assertEquals("Basi di Dati", results.get(0).title());
}

@Test
void testSearchForEvents_HideOverlapping_ShouldKeepOverlappingBookedEvents() {
    LocalDateTime baseTime = LocalDateTime.now().plusDays(10).withHour(10).withMinute(0).withSecond(0).withNano(0);
    Event myEvent = createSearchEvent(10L, "Evento creato da me", baseTime, 2.0);
    Event otherBookedEvent = createSearchEvent(11L, "Evento prenotato", baseTime.plusHours(1), 2.0);

    when(session.getAttribute("userId")).thenReturn(1L);
    when(eventRepository.findByDynamicFilters(isNull(), eq(false), eq(List.of()), isNull(), isNull(), isNull()))
            .thenReturn(List.of(myEvent, otherBookedEvent));
    when(eventRepository.findBookedByParticipantId(1L)).thenReturn(List.of(myEvent, otherBookedEvent));

    EventSearchFilter filter = new EventSearchFilter(null, null, null, null, null, null, null, true);
    List<EventResponse> results = eventService.searchForEvents(session, filter);

    assertEquals(2, results.size());
    assertEquals("Evento creato da me", results.get(0).title());
    assertEquals("Evento prenotato", results.get(1).title());
}

}
