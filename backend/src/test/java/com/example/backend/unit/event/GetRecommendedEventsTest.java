package com.example.backend.unit.event;

import com.example.backend.dto.RecommendationResponse;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetRecommendedEventsTest extends BaseEventTest {

    private User testUser;
    private final Long USER_ID = 1L;
    private final Double USER_LAT = 45.0;
    private final Double USER_LON = 9.0;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(USER_ID);
        testUser.setMacroTags(new ArrayList<>());
        testUser.setMicroTags(new ArrayList<>());
    }

    @Test
    void getRecommendedEvents_shouldReturnMissingCoordinates() {
        when(session.getAttribute("userId")).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        RecommendationResponse response = eventService.getRecommendedEvents(session, null, USER_LON);

        assertEquals("MISSING_COORDINATES", response.messageCode());
        assertTrue(response.recommendedEvents().isEmpty());
        verify(eventRepository, never()).findAll();
    }

    @Test
    void getRecommendedEvents_shouldReturnNoTagMatch() {
        testUser.setMacroTags(List.of("Cucina"));

        when(session.getAttribute("userId")).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        Event wrongEvent = createBaseEvent(2L, "Corso Java", 45.01, 9.01);
        wrongEvent.setTags(List.of(new EventTag("Informatica", "Java")));

        when(eventRepository.findAll()).thenReturn(List.of(wrongEvent));

        RecommendationResponse response = eventService.getRecommendedEvents(session, USER_LAT, USER_LON);

        assertEquals("NO_TAG_MATCH", response.messageCode());
        assertTrue(response.recommendedEvents().isEmpty());
    }

    @Test
    void getRecommendedEvents_shouldReturnNoLocationMatch() {
        testUser.setMacroTags(List.of("Informatica"));

        when(session.getAttribute("userId")).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        Event farEvent = createBaseEvent(2L, "Evento Lontano", 48.0, 9.0);
        farEvent.setTags(List.of(new EventTag("Informatica", null)));

        when(eventRepository.findAll()).thenReturn(List.of(farEvent));

        RecommendationResponse response = eventService.getRecommendedEvents(session, USER_LAT, USER_LON);

        assertEquals("NO_LOCATION_MATCH", response.messageCode());
        assertTrue(response.recommendedEvents().isEmpty());
    }

    @Test
    void getRecommendedEvents_shouldCalculateScoresAndSortCorrectly() {
        testUser.setMacroTags(List.of("Informatica"));
        testUser.setMicroTags(List.of("React"));

        when(session.getAttribute("userId")).thenReturn(USER_ID);
        when(userRepository.findById(USER_ID)).thenReturn(Optional.of(testUser));

        // EVENTO A: Lontano (~40km, lat 45.36) ma matcha il MICRO tag React (+300 punti
        // base)
        Event eventA = createBaseEvent(2L, "Evento A (React)", 45.36, 9.0);
        eventA.setTags(List.of(new EventTag("Informatica", "React")));

        // EVENTO B: Vicinissimo (~1km, lat 45.009) ma matcha solo il MACRO tag (+100
        // punti base)
        Event eventB = createBaseEvent(3L, "Evento B (Informatica Gen)", 45.009, 9.0);
        eventB.setTags(List.of(new EventTag("Informatica", "Java")));

        // EVENTO C: Creato dall'utente stesso (USER_ID = 1L). Deve essere filtrato via
        // dallo Step 1.
        Event eventC = createBaseEvent(USER_ID, "Mio Evento", 45.0, 9.0);
        eventC.setTags(List.of(new EventTag("Informatica", "React")));

        when(eventRepository.findAll()).thenReturn(List.of(eventA, eventB, eventC));

        RecommendationResponse response = eventService.getRecommendedEvents(session, USER_LAT, USER_LON);

        assertEquals("SUCCESS", response.messageCode());
        assertEquals(2, response.recommendedEvents().size(), "L'evento C (creato dall'utente) deve essere scartato");

        // L'evento A deve essere il primo perché il micro tag (+300) batte il macro tag
        // (+100) più la vicinanza
        assertEquals("Evento A (React)", response.recommendedEvents().get(0).title());
        assertEquals("Evento B (Informatica Gen)", response.recommendedEvents().get(1).title());
    }

    private Event createBaseEvent(Long creatorId, String title, Double lat, Double lon) {
        User creator = new User();
        creator.setId(creatorId);
        creator.setName("Test Creator");
        creator.setLastname("Test Lastname");

        Event event = new Event();
        event.setId((long) (Math.random() * 1000));
        event.setTitle(title);
        event.setCreator(creator);
        event.setLatitude(lat);
        event.setLongitude(lon);
        event.setStatus("open");
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setParticipants(new HashSet<>());
        return event;
    }
}