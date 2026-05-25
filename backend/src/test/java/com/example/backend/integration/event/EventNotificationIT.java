package com.example.backend.integration.event;

import com.example.backend.dto.EventRequest;
import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.model.UserSettings;
import com.example.backend.repository.UserSettingsRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class EventNotificationIT extends BaseEventIT {

        @Autowired
        private UserSettingsRepository settingsRepository;

        private ObjectMapper objectMapper = new ObjectMapper()
                        .registerModule(new JavaTimeModule())
                        .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

        @MockitoBean
        private JavaMailSender mailSender;

        @Test
        void deleteEvent_ShouldSendEmail_WhenNotificationIsActive() throws Exception {
                User creator = userRepository.findById(userId).orElseThrow();
                User participant = new User("Anna", "Verdi", "anna@example.com", "Pass123", "Roma", List.of(),
                                List.of());
                userRepository.save(participant);

                UserSettings settings = new UserSettings("anna@example.com", true, true, true);
                settingsRepository.save(settings);

                Event event = createTestEvent(creator, participant);

                mockMvc.perform(delete("/events/" + event.getId())
                                .sessionAttr("userId", userId))
                                .andExpect(status().isOk());

                verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }

        @Test
        void deleteEvent_ShouldNOTSendEmail_WhenNotificationIsInactive() throws Exception {
                User creator = userRepository.findById(userId).orElseThrow();
                User participant = new User("Marco", "Neri", "marco@example.com", "Pass123", "Milano", List.of(),
                                List.of());
                userRepository.save(participant);

                UserSettings settings = new UserSettings("marco@example.com", true, false, true);
                settingsRepository.save(settings);

                Event event = createTestEvent(creator, participant);

                mockMvc.perform(delete("/events/" + event.getId())
                                .sessionAttr("userId", userId))
                                .andExpect(status().isOk());

                verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        @Test
        void updateEvent_ShouldSendEmail_WhenNotificationIsActive() throws Exception {
                User creator = userRepository.findById(userId).orElseThrow();
                User participant = new User("Giulia", "Bianchi", "giulia@example.com", "Pass123", "Torino", List.of(),
                                List.of());
                userRepository.save(participant);

                UserSettings settings = new UserSettings("giulia@example.com", true, true, true);
                settingsRepository.save(settings);

                Event event = createTestEvent(creator, participant);

                EventRequest updateRequest = new EventRequest(
                                "Titolo Aggiornato e Modificato",
                                event.getDateTime(),
                                event.getDuration(),
                                event.getLocation(),
                                event.getLatitude(),
                                event.getLongitude(),
                                event.getTags(),
                                "Descrizione",
                                event.getMaxParticipants(),
                                event.isCreatorParticipationRequired());

                mockMvc.perform(put("/events/" + event.getId())
                                .sessionAttr("userId", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk());
                verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
        }

        @Test
        void updateEvent_ShouldNOTSendEmail_WhenNotificationIsInactive() throws Exception {
                User creator = userRepository.findById(userId).orElseThrow();
                User participant = new User("Luigi", "Gialli", "luigi@example.com", "Pass123", "Napoli", List.of(),
                                List.of());
                userRepository.save(participant);

                UserSettings settings = new UserSettings("luigi@example.com", false, true, true);
                settingsRepository.save(settings);

                Event event = createTestEvent(creator, participant);

                EventRequest updateRequest = new EventRequest(
                                "Titolo Super Aggiornato",
                                event.getDateTime(),
                                event.getDuration(),
                                event.getLocation(),
                                event.getLatitude(),
                                event.getLongitude(),
                                event.getTags(),
                                "Descrizione",
                                event.getMaxParticipants(),
                                event.isCreatorParticipationRequired());

                mockMvc.perform(put("/events/" + event.getId())
                                .sessionAttr("userId", userId)
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(objectMapper.writeValueAsString(updateRequest)))
                                .andExpect(status().isOk());

                verify(mailSender, never()).send(any(SimpleMailMessage.class));
        }

        private Event createTestEvent(User creator, User participant) {
                Event event = new Event();
                event.setTitle("Evento Test Originale");

                event.setDateTime(LocalDateTime.now().plusDays(10));

                event.setDuration(2.0);
                event.setLocation("Città X");
                event.setMaxParticipants(10);
                event.setCreatorParticipationRequired(true);
                event.setCreator(creator);

                event.setTags(List.of(new EventTag("MacroTest", "MicroTest")));

                event.addParticipant(creator);
                event.addParticipant(participant);

                return eventRepository.save(event);
        }
}