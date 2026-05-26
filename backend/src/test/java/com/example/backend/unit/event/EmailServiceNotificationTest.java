package com.example.backend.unit.event;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.model.UserSettings;
import com.example.backend.repository.UserSettingsRepository;
import com.example.backend.service.EmailService;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public class EmailServiceNotificationTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private UserSettingsRepository settingsRepository;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender, settingsRepository);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
    }

    @Test
    @DisplayName("Cancellazione: Invia mail solo ai partecipanti con notifiche attive, escludendo il creatore")
    void sendEventCancellationEmailToParticipants_ShouldFilterCorrectly() {
        // 1. SETUP
        User creator = user(1L, "creator@example.com");
        User activeParticipant = user(2L, "active@example.com");
        User inactiveParticipant = user(3L, "inactive@example.com");

        Event event = createTestEvent(creator, Set.of(creator, activeParticipant, inactiveParticipant));

        when(settingsRepository.findById("active@example.com"))
                .thenReturn(Optional.of(new UserSettings("active@example.com", true, true, true)));

        when(settingsRepository.findById("inactive@example.com"))
                .thenReturn(Optional.of(new UserSettings("inactive@example.com", true, false, true)));

        emailService.sendEventCancellationEmailToParticipants(event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertEquals("active@example.com", sentMessage.getTo()[0]);
        assertEquals("Evento cancellato - StudyTogether", sentMessage.getSubject());
    }

    @Test
    @DisplayName("Modifica: Invia mail solo ai partecipanti con notifiche attive, escludendo il creatore")
    void sendEventUpdateEmailToParticipants_ShouldFilterCorrectly() {
        // 1. SETUP
        User creator = user(1L, "creator@example.com");
        User activeParticipant = user(2L, "active@example.com");
        User inactiveParticipant = user(3L, "inactive@example.com");

        Event event = createTestEvent(creator, Set.of(creator, activeParticipant, inactiveParticipant));

        when(settingsRepository.findById("active@example.com"))
                .thenReturn(Optional.of(new UserSettings("active@example.com", true, true, true)));

        when(settingsRepository.findById("inactive@example.com"))
                .thenReturn(Optional.of(new UserSettings("inactive@example.com", false, true, true)));

        emailService.sendEventUpdateEmailToParticipants(event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();

        assertEquals("active@example.com", sentMessage.getTo()[0]);
        assertEquals("Evento aggiornato - StudyTogether", sentMessage.getSubject());
    }

    private Event createTestEvent(User creator, Set<User> participants) {
        Event event = new Event();
        event.setTitle("Evento di Test");
        event.setDateTime(LocalDateTime.now().plusDays(10));
        event.setCreator(creator);
        event.setParticipants(new HashSet<>(participants));
        return event;
    }

    private User user(Long id, String email) {
        User user = new User();
        user.setId(id);
        user.setEmail(email);
        user.setName("Name_" + id);
        return user;
    }
}