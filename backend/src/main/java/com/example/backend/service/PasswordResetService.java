package com.example.backend.service;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Random;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.example.backend.dto.ForgotPasswordRequest;
import com.example.backend.dto.ResetPasswordRequest;
import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;

import jakarta.transaction.Transactional;

/**
 * Service responsible for handling password reset operations.
 *
 * It manages:
 * - generation and validation of reset codes
 * - sending reset emails
 * - updating user passwords securely
 */
@Service
public class PasswordResetService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    /**
     * Constructs the PasswordResetService.
     *
     * @param userRepository               repository used to access user data
     * @param passwordResetTokenRepository repository used to manage reset tokens
     * @param passwordEncoder              encoder used to securely hash passwords
     * @param emailService                 service used to send reset emails
     */
    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * Handles the "forgot password" process.
     *
     * Generates a reset code, stores it with expiration, and sends it via email.
     * Includes protection against spam requests by enforcing a cooldown period.
     *
     * @param request contains the user's email address
     * @return confirmation message
     * @throws IllegalArgumentException if the email is invalid, not found,
     *                                  or if a request is made too soon
     */
    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {

        String email = request.email();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email richiesta");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(
                        () -> new IllegalArgumentException("Non è stato trovato nessun utente associato all'email"));

        LocalDateTime now = LocalDateTime.now();

        passwordResetTokenRepository.deleteByExpiresAtBefore(now);

        PasswordResetToken lastToken = passwordResetTokenRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElse(null);

        if (lastToken != null) {
            long secondsPassed = Duration
                    .between(lastToken.getCreatedAt(), now)
                    .getSeconds();

            if (secondsPassed < 30) {
                throw new IllegalArgumentException("Aspetta almeno 30 secondi prima di richiedere un nuovo codice");
            }
        }

        passwordResetTokenRepository.deleteByEmail(email);

        String code = generateResetCode();

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(user.getEmail());
        token.setCode(code);
        token.setCreatedAt(now);
        token.setExpiresAt(now.plusMinutes(10));
        token.setUsed(false);

        passwordResetTokenRepository.save(token);

        emailService.sendPasswordResetCode(user.getEmail(), code);

        return "Codice di reset della password inviato per email";
    }

    /**
     * Resets the user's password using a valid reset code.
     * <p>
     * Validates the verification code, checks for token expiration, and updates the
     * user's
     * password. The new password must satisfy specific security criteria, including
     * a minimum
     * length of 8 characters, at least one uppercase letter, and at least one
     * numeric digit.
     *
     * @param request contains email, reset code, and new password
     * @return confirmation message
     * @throws IllegalArgumentException if input fields are blank, the code is
     *                                  incorrect,
     *                                  the token is expired, the user does not
     *                                  exist,
     *                                  or if the new password fails complexity
     *                                  validation rules
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {

        String email = request.email();
        String code = request.code();
        String newPassword = request.newPassword();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obbligatoria");
        }

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Il codice è richiesto");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password troppo corta");
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno una maiuscola");
        }

        if (!newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La password deve contenere almeno un numero");
        }

        PasswordResetToken token = passwordResetTokenRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new IllegalArgumentException("Codice non valido"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            throw new IllegalArgumentException("Codice scaduto");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(token);

        return "Password aggiornata con successo";
    }

    /**
     * Generates a random 6-digit numeric reset code.
     *
     * @return the generated reset code as a string
     */
    private String generateResetCode() {
        int number = 100000 + new Random().nextInt(900000);
        return String.valueOf(number);
    }
}
