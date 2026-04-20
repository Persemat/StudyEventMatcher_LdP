package com.example.backend.unit.event;

import org.junit.jupiter.api.BeforeEach;
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
import com.example.backend.service.EmailService;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class NotificationDeleteEventTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
    }

    @Test
    void sendEventCancellationEmail_sendsCorrectMessage() {
        Event event = new Event();
        event.setTitle("Calculus 1");
        event.setDateTime(LocalDateTime.of(2026, 4, 20, 15, 30));

        emailService.sendEventCancellationEmail("mario@example.com", event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("noreply@test.com", message.getFrom());
        assertEquals("mario@example.com", message.getTo()[0]);
        assertEquals("Event cancelled - StudyTogether", message.getSubject());

        String text = message.getText();
        assertTrue(text.contains("Calculus 1"));
        assertTrue(text.contains("20/04/2026"));
        assertTrue(text.contains("15:30"));
        assertTrue(text.contains("has been cancelled"));
    }

    @Test
    void sendEventCancellationEmailToParticipants_sendsToAllExceptCreator() {
        User creator = new User();
        creator.setId(1L);
        creator.setName("Luca");
        creator.setEmail("luca@example.com");

        User participant1 = new User();
        participant1.setId(2L);
        participant1.setName("Anna");
        participant1.setEmail("anna@example.com");

        User participant2 = new User();
        participant2.setId(3L);
        participant2.setName("Marco");
        participant2.setEmail("marco@example.com");

        Event event = new Event();
        event.setTitle("Databases");
        event.setDateTime(LocalDateTime.of(2026, 4, 21, 10, 0));
        event.setCreator(creator);
        event.setParticipants(new HashSet<>());

        event.addParticipant(creator);
        event.addParticipant(participant1);
        event.addParticipant(participant2);

        emailService.sendEventCancellationEmailToParticipants(event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(captor.capture());

        var messages = captor.getAllValues();

        boolean sentToAnna = messages.stream()
                .anyMatch(m -> m.getTo() != null && m.getTo()[0].equals("anna@example.com"));

        boolean sentToMarco = messages.stream()
                .anyMatch(m -> m.getTo() != null && m.getTo()[0].equals("marco@example.com"));

        boolean sentToLuca = messages.stream()
                .anyMatch(m -> m.getTo() != null && m.getTo()[0].equals("luca@example.com"));

        assertTrue(sentToAnna);
        assertTrue(sentToMarco);
        assertEquals(false, sentToLuca);
    }

    @Test
    void sendEventCancellationEmailToParticipants_doesNotSendIfEmailIsNullOrEmpty() {
        User creator = new User();
        creator.setId(1L);
        creator.setEmail("creator@example.com");

        User participant1 = new User();
        participant1.setId(2L);
        participant1.setEmail(null);

        User participant2 = new User();
        participant2.setId(3L);
        participant2.setEmail("");

        User participant3 = new User();
        participant3.setId(4L);
        participant3.setEmail("valid@example.com");

        Event event = new Event();
        event.setTitle("Programming");
        event.setDateTime(LocalDateTime.of(2026, 4, 22, 9, 0));
        event.setCreator(creator);
        event.setParticipants(new HashSet<>());

        event.addParticipant(creator);
        event.addParticipant(participant1);
        event.addParticipant(participant2);
        event.addParticipant(participant3);

        emailService.sendEventCancellationEmailToParticipants(event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("valid@example.com", message.getTo()[0]);
    }
}
