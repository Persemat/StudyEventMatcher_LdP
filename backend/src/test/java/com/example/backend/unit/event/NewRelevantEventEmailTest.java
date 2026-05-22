package com.example.backend.unit.event;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.service.EmailService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

public class NewRelevantEventEmailTest {

    @Test
    void sendNewRelevantEventEmail_shouldUseReadableEventDetails() {
        JavaMailSender mailSender = mock(JavaMailSender.class);
        EmailService emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "test@example.com");

        Event event = new Event();
        event.setTitle("prova notifica");
        event.setDateTime(LocalDateTime.parse("2026-05-22T15:00:00"));
        event.setLocation("Milano (MI)-Aula 1");
        event.setTags(List.of(
                new EventTag("Informatica", "Machine Learning"),
                new EventTag("Informatica", "Sicurezza Informatica")));

        emailService.sendNewRelevantEventEmail("user@example.com", event);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        String text = captor.getValue().getText();
        assertNotNull(text);
        assertTrue(text.contains("Titolo: prova notifica"));
        assertTrue(text.contains("Tag di interesse: Informatica > Machine Learning, Informatica > Sicurezza Informatica"));
        assertTrue(text.contains("Luogo: Milano (MI)-Aula 1"));
        assertTrue(text.contains("Data e ora: 22/05/2026 alle 15:00"));
    }
}
