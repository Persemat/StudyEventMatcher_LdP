package com.dem.ldp.service;

import java.time.format.DateTimeFormatter;
import java.util.Set;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void inviaCodiceRecuperoPassword(String destinatario, String codice) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinatario);
        message.setSubject("Recupero password - StudyTogether");
        message.setText(
                "Hai richiesto il recupero della password.\n\n" +
                "Il tuo codice è: " + codice + "\n\n" +
                "Scade tra 10 minuti."
        );

        mailSender.send(message);
    }
    public void inviaMailCancellazioneEvento(String destinatario, Evento evento) {
        DateTimeFormatter dataFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter oraFormatter = DateTimeFormatter.ofPattern("HH:mm");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinatario);
        message.setSubject("Evento cancellato - StudyTogether");
        message.setText(
                "Ti informiamo che l'evento '" + evento.getTitolo() + "' del giorno " +
                evento.getDataOra().format(dataFormatter) + " alle ore " +
                evento.getDataOra().format(oraFormatter) +
                " è stato cancellato."
        );

        mailSender.send(message);
    }

    public void inviaMailCancellazioneEventoAiPartecipanti(Evento evento) {
        Utente creatore = evento.getCreatore();

        for (Utente partecipante : evento.getPartecipanti()) {
            if (creatore != null
                    && partecipante.getId() != null
                    && partecipante.getId().equals(creatore.getId())) {
                continue;
            }

            if (partecipante.getEmail() != null && !partecipante.getEmail().isBlank()) {
                inviaMailCancellazioneEvento(partecipante.getEmail(), evento);
            }
        }
    }
}