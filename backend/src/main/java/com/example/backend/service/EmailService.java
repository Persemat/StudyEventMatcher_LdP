package com.example.backend.service;

import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;

/**
 * Service responsible for sending application emails.
 *
 * It handles:
 * - password reset emails
 * - event cancellation notifications
 * - event update notifications
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
                "Hai richiesto il reset della password.\n\n" +
                "Il tuo codice di verifica è: " + code + "\n\n" +
                "Questo codice scadrà tra 10 minuti."
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
        message.setSubject("Evento cancellato - StudyTogether");
        message.setText(
                "Ti informiamo che l'evento '" + event.getTitle() + " programmato il  " +
                event.getDateTime().format(dateFormatter) + " alle " +
                event.getDateTime().format(timeFormatter) +
                " è stato cancellato."
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

    /**
     * Sends an event update email to a single user.
     *
     * @param recipientEmail the recipient's email address
     * @param event the updated event
     */
    public void sendEventUpdateEmail(String recipientEmail, Event event) {

        DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm");

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Evento aggiornato - StudyTogether");
        message.setText(
                "L'evento " + event.getTitle() + " \u00e8 stato aggiornato.\n\n" +
                "Data: " + event.getDateTime().format(dateFormatter) + "\n" +
                "Ora: " + event.getDateTime().format(timeFormatter) + "\n" +
                "Luogo: " + event.getLocation()
        );

        mailSender.send(message);
    }

    /**
     * Sends event update emails to all booked participants,
     * excluding the event creator.
     *
     * @param event the updated event
     */
    public void sendEventUpdateEmailToParticipants(Event event) {

        User creator = event.getCreator();

        for (User participant : event.getParticipants()) {

            if (creator != null &&
                    participant.getId() != null &&
                    participant.getId().equals(creator.getId())) {
                continue;
            }

            if (participant.getEmail() != null && !participant.getEmail().isBlank()) {
                sendEventUpdateEmail(participant.getEmail(), event);
            }
        }
    }

    /**
     * Sends a notification for a newly published event matching user interests.
     *
     * @param recipientEmail the recipient's email address
     * @param event the newly published event
     */
    public void sendNewRelevantEventEmail(String recipientEmail, Event event) {

        DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy 'alle' HH:mm");
        String formattedTags = formatEventTags(event);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(recipientEmail);
        message.setSubject("Nuovo evento per te - StudyTogether");
        message.setText(
                "Abbiamo trovato un nuovo evento in linea con i tuoi interessi:\n\n" +
                "Titolo: " + event.getTitle() + "\n" +
                "Tag di interesse: " + formattedTags + "\n" +
                "Luogo: " + event.getLocation() + "\n" +
                "Data e ora: " + event.getDateTime().format(dateTimeFormatter)
        );

        mailSender.send(message);
    }

    /**
     * Formats event tags for notification emails.
     *
     * @param event event containing tags
     * @return comma-separated tag labels
     */
    private String formatEventTags(Event event) {
        List<EventTag> tags = event.getTags();

        if (tags == null || tags.isEmpty()) {
            return "Nessun tag specificato";
        }

        // Show each tag once, using the same readable label used by the UI.
        return tags.stream()
                .map(EventTag::toDisplayLabel)
                .filter(tag -> tag != null && !tag.isBlank())
                .distinct()
                .reduce((first, second) -> first + ", " + second)
                .orElse("Nessun tag specificato");
    }
}
