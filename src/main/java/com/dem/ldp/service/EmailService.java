package com.dem.ldp.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

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
}