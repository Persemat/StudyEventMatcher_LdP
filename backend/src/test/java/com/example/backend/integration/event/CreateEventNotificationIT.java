package com.example.backend.integration.event;

import com.example.backend.model.User;
import com.example.backend.model.UserSettings;
import com.example.backend.repository.UserSettingsRepository;
import com.example.backend.service.CityCoordinateService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class CreateEventNotificationIT extends BaseEventIT {

        @Autowired
        private UserSettingsRepository settingsRepository;

        @MockitoBean
        private JavaMailSender mailSender;

        @MockitoBean
        private CityCoordinateService cityCoordinateService;

        @Test
        @DisplayName("Creazione Evento - Invia raccomandazione se l'utente è compatibile e ha le notifiche ATTIVE")
        void createEvent_shouldSendRecommendationEmail_WhenNotificationIsActive() throws Exception {
                User interestedUser = new User("Giulia", "Verdi", "giulia.interessata@example.com", "Pass123",
                                "Roma", List.of("Informatica e Tecnologie"), List.of("Programmazione"));
                userRepository.save(interestedUser);

                UserSettings settings = new UserSettings(interestedUser.getEmail(), true, true, true);
                settingsRepository.save(settings);

                when(cityCoordinateService.findCoordinates(anyString()))
                                .thenReturn(Optional.of(new CityCoordinateService.Coordinates(41.9028, 12.4964)));

                String json = """
                                {
                                  "title": "Java Bootcamp Roma",
                                  "dateTime": "2026-10-15T15:00:00",
                                  "duration": 2.5,
                                  "location": "Roma (RM)-Centro",
                                  "latitude": 41.9028,
                                  "longitude": 12.4964,
                                  "tags": [
                                    { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
                                  ],
                                  "description": "Corso intensivo di Java",
                                  "maxParticipants": 10,
                                  "creatorParticipationRequired": true
                                }
                                """;

                mockMvc.perform(post("/events")
                                .sessionAttr("userId", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(content()
                                                .string(org.hamcrest.Matchers.containsString("creato con successo")));

                assertEquals(1, eventRepository.count());

                verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }

        @Test
        @DisplayName("Creazione Evento - NON invia raccomandazione se l'utente è compatibile ma ha le notifiche DISATTIVATE")
        void createEvent_shouldNOTSendRecommendationEmail_WhenNotificationIsInactive() throws Exception {
                User interestedUser = new User("Marco", "Neri", "marco.silenzioso@example.com", "Pass123",
                                "Roma", List.of("Informatica e Tecnologie"), List.of("Programmazione"));
                userRepository.save(interestedUser);

                UserSettings settings = new UserSettings(interestedUser.getEmail(), true, true, false);
                settingsRepository.save(settings);

                when(cityCoordinateService.findCoordinates(anyString()))
                                .thenReturn(Optional.of(new CityCoordinateService.Coordinates(41.9028, 12.4964)));

                String json = """
                                {
                                  "title": "Java Bootcamp Roma 2",
                                  "dateTime": "2026-11-20T10:00:00",
                                  "duration": 4.0,
                                  "location": "Roma (RM)-Eur",
                                  "latitude": 41.9028,
                                  "longitude": 12.4964,
                                  "tags": [
                                    { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
                                  ],
                                  "description": "Corso avanzato di Java",
                                  "maxParticipants": 20,
                                  "creatorParticipationRequired": true
                                }
                                """;

                mockMvc.perform(post("/events")
                                .sessionAttr("userId", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                                .andExpect(status().isOk())
                                .andExpect(content()
                                                .string(org.hamcrest.Matchers.containsString("creato con successo")));

                assertEquals(1, eventRepository.count());

                verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }
}