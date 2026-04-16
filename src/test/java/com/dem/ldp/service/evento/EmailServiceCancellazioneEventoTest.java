package com.dem.ldp.service.evento;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.service.EmailService;

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

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
class EmailServiceCancellazioneEventoTest {

    @Mock
    private JavaMailSender mailSender;

    private EmailService emailService;

    @BeforeEach
    void setUp() {
        emailService = new EmailService(mailSender);
        ReflectionTestUtils.setField(emailService, "fromEmail", "noreply@test.com");
    }

    @Test
    void inviaMailCancellazioneEvento_inviaMessaggioCorretto() {
        Evento evento = new Evento();
        evento.setTitolo("Analisi 1");
        evento.setDataOra(LocalDateTime.of(2026, 4, 20, 15, 30));

        emailService.inviaMailCancellazioneEvento("mario@example.com", evento);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();

        assertEquals("noreply@test.com", message.getFrom());
        assertEquals("mario@example.com", message.getTo()[0]);
        assertEquals("Evento cancellato - StudyTogether", message.getSubject());

        String testo = message.getText();
        assertTrue(testo.contains("Analisi 1"));
        assertTrue(testo.contains("20/04/2026"));
        assertTrue(testo.contains("15:30"));
        assertTrue(testo.contains("è stato cancellato"));
    }

    @Test
    void inviaMailCancellazioneEventoAiPartecipanti_inviaMailATuttiTranneCreatore() {
        Utente creatore = new Utente();
        creatore.setId(1L);
        creatore.setNome("Luca");
        creatore.setEmail("luca@example.com");

        Utente partecipante1 = new Utente();
        partecipante1.setId(2L);
        partecipante1.setNome("Anna");
        partecipante1.setEmail("anna@example.com");

        Utente partecipante2 = new Utente();
        partecipante2.setId(3L);
        partecipante2.setNome("Marco");
        partecipante2.setEmail("marco@example.com");

        Evento evento = new Evento();
        evento.setTitolo("Basi di dati");
        evento.setDataOra(LocalDateTime.of(2026, 4, 21, 10, 0));
        evento.setCreatore(creatore);
        evento.setPartecipanti(new HashSet<>());

        evento.aggiungiPartecipante(creatore);
        evento.aggiungiPartecipante(partecipante1);
        evento.aggiungiPartecipante(partecipante2);

        emailService.inviaMailCancellazioneEventoAiPartecipanti(evento);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(2)).send(captor.capture());

        var messaggi = captor.getAllValues();

        boolean inviataAdAnna = messaggi.stream()
                .anyMatch(m -> m.getTo() != null && m.getTo()[0].equals("anna@example.com"));

        boolean inviataAMarco = messaggi.stream()
                .anyMatch(m -> m.getTo() != null && m.getTo()[0].equals("marco@example.com"));

        boolean inviataALuca = messaggi.stream()
                .anyMatch(m -> m.getTo() != null && m.getTo()[0].equals("luca@example.com"));

        assertTrue(inviataAdAnna);
        assertTrue(inviataAMarco);
        assertEquals(false, inviataALuca);
    }

    @Test
    void inviaMailCancellazioneEventoAiPartecipanti_nonInviaMailSeEmailVuotaONulla() {
        Utente creatore = new Utente();
        creatore.setId(1L);
        creatore.setEmail("creatore@example.com");

        Utente partecipante1 = new Utente();
        partecipante1.setId(2L);
        partecipante1.setEmail(null);

        Utente partecipante2 = new Utente();
        partecipante2.setId(3L);
        partecipante2.setEmail("");

        Utente partecipante3 = new Utente();
        partecipante3.setId(4L);
        partecipante3.setEmail("valida@example.com");

        Evento evento = new Evento();
        evento.setTitolo("Programmazione");
        evento.setDataOra(LocalDateTime.of(2026, 4, 22, 9, 0));
        evento.setCreatore(creatore);
        evento.setPartecipanti(new HashSet<>());

        evento.aggiungiPartecipante(creatore);
        evento.aggiungiPartecipante(partecipante1);
        evento.aggiungiPartecipante(partecipante2);
        evento.aggiungiPartecipante(partecipante3);

        emailService.inviaMailCancellazioneEventoAiPartecipanti(evento);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertEquals("valida@example.com", message.getTo()[0]);
    }
}