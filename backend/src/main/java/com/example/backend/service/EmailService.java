package com.example.backend.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.backend.model.Event;
import com.example.backend.model.User;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    // ================= PASSWORD RESET EMAIL =================
    public void sendPasswordResetCode(String recipientEmail, String code) {

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Password Reset - StudyTogether");
        message.setText(
                "You requested a password reset.\n\n" +
                "Your verification code is: " + code + "\n\n" +
                "This code will expire in 10 minutes."
        );

        mailSender.send(message);
    }

    // ================= EVENT DELETION EMAIL (TO SINGLE USER) =================
    public void sendEventCancellationEmail(String recipientEmail, Event event) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Event cancelled - StudyTogether");
        message.setText(
                "We inform you that the event '" + event.getTitle() + "' scheduled on " +
                event.getDateTime().format(dateFormatter) + " at " +
                event.getDateTime().format(timeFormatter) +
                " has been cancelled."
        );

        mailSender.send(message);
    }

    // ================= EVENT DELETION EMAIL (TO ALL PARTICIPANTS) =================
    public void sendEventCancellationEmailToParticipants(Event event) {

        User creator = event.getCreator();

        for (User participant : event.getParticipants()) {

            // skip creator
            if (creator != null &&
                    participant.getId() != null &&
                    participant.getId().equals(creator.getId())) {
                continue;
            }

            if (participant.getEmail() != null && !participant.getEmail().isBlank()) {
                sendEventCancellationEmail(participant.getEmail(), event);
            }
        }
    }
}