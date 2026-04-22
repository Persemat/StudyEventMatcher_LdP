package com.example.backend.service;

import java.time.format.DateTimeFormatter;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.backend.model.Event;
import com.example.backend.model.User;

/**
 * Service responsible for sending application emails.
 *
 * It handles:
 * - password reset emails
 * - event cancellation notifications
 *
 * Uses {@link JavaMailSender} to send messages.
 */
@Service
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    /**
     * Constructs the EmailService.
     *
     * @param mailSender Spring component used to send emails
     */
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    /**
     * Sends a password reset email containing a verification code.
     *
     * @param recipientEmail the recipient's email address
     * @param code the verification code for password reset
     */
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

    /**
     * Sends an event cancellation email to a single user.
     *
     * @param recipientEmail the recipient's email address
     * @param event the cancelled event
     */
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

    /**
     * Sends event cancellation emails to all participants,
     * excluding the event creator.
     *
     * @param event the cancelled event
     */
    public void sendEventCancellationEmailToParticipants(Event event) {

        User creator = event.getCreator();

        for (User participant : event.getParticipants()) {

            // Skip creator
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