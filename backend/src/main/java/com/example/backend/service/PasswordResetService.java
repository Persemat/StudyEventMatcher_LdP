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
     * @param userRepository repository used to access user data
     * @param passwordResetTokenRepository repository used to manage reset tokens
     * @param passwordEncoder encoder used to securely hash passwords
     * @param emailService service used to send reset emails
     */
    public PasswordResetService(
            UserRepository userRepository,
            PasswordResetTokenRepository passwordResetTokenRepository,
            PasswordEncoder passwordEncoder,
            EmailService emailService
    ) {
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
            throw new IllegalArgumentException("Email is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("No user found with this email"));

        LocalDateTime now = LocalDateTime.now();

        // Remove expired tokens
        passwordResetTokenRepository.deleteByExpiresAtBefore(now);

        // Prevent spam requests (30s cooldown)
        PasswordResetToken lastToken = passwordResetTokenRepository
                .findTopByEmailOrderByCreatedAtDesc(email)
                .orElse(null);

        if (lastToken != null) {
            long secondsPassed = Duration
                    .between(lastToken.getCreatedAt(), now)
                    .getSeconds();

            if (secondsPassed < 30) {
                throw new IllegalArgumentException("Wait at least 30 seconds before requesting a new code");
            }
        }

        // Remove previous tokens for this email
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

        return "Password reset code sent via email";
    }

    /**
     * Resets the user's password using a valid reset code.
     *
     * Validates the code, checks expiration, and updates the password
     * after applying security rules.
     *
     * @param request contains email, reset code, and new password
     * @return confirmation message
     * @throws IllegalArgumentException if input data is invalid, the code is incorrect or expired,
     *                                  or the user cannot be found
     */
    @Transactional
    public String resetPassword(ResetPasswordRequest request) {

        String email = request.email();
        String code = request.code();
        String newPassword = request.newPassword();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (code == null || code.isBlank()) {
            throw new IllegalArgumentException("Code is required");
        }

        if (newPassword == null || newPassword.length() < 8) {
            throw new IllegalArgumentException("Password too short");
        }

        if (!newPassword.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain an uppercase letter");
        }

        if (!newPassword.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain a number");
        }

        PasswordResetToken token = passwordResetTokenRepository
                .findByEmailAndCodeAndUsedFalse(email, code)
                .orElseThrow(() -> new IllegalArgumentException("Invalid code"));

        if (token.getExpiresAt().isBefore(LocalDateTime.now())) {
            passwordResetTokenRepository.delete(token);
            throw new IllegalArgumentException("Code expired");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(token);

        return "Password updated successfully";
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