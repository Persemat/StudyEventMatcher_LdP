package com.example.backend.unit.user;

import com.example.backend.dto.ForgotPasswordRequest;
import com.example.backend.dto.ResetPasswordRequest;
import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

public class PasswordResetTest extends BasePasswordResetTest {

        @Test
        void forgotPassword_shouldThrowException_whenEmailIsEmpty() {
                ForgotPasswordRequest request = new ForgotPasswordRequest("");

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> passwordResetService.forgotPassword(request));

                assertEquals("Email richiesta", ex.getMessage());
        }

        @Test
        void forgotPassword_shouldThrowException_whenEmailNotFound() {
                ForgotPasswordRequest request = new ForgotPasswordRequest("mario@example.com");

                when(userRepository.findByEmail("mario@example.com"))
                                .thenReturn(Optional.empty());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> passwordResetService.forgotPassword(request));

                assertEquals("Non è stato trovato nessun utente associato all'email", ex.getMessage());
        }

        @Test
        void forgotPassword_shouldSendCode_andSaveToken_whenValid() {
                ForgotPasswordRequest request = new ForgotPasswordRequest("mario@example.com");

                User user = new User("Mario", "Rossi", "mario@example.com", "HASH", "Milano", List.of(), List.of());

                when(userRepository.findByEmail("mario@example.com"))
                                .thenReturn(Optional.of(user));

                when(passwordResetTokenRepository.findTopByEmailOrderByCreatedAtDesc("mario@example.com"))
                                .thenReturn(Optional.empty());

                String result = passwordResetService.forgotPassword(request);

                assertEquals("Codice di reset della password inviato per email", result);

                verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
                verify(emailService).sendPasswordResetCode(eq("mario@example.com"), any(String.class));
        }

        @Test
        void forgotPassword_shouldThrowException_whenRequestedTooSoon() {
                ForgotPasswordRequest request = new ForgotPasswordRequest("mario@example.com");

                User user = new User("Mario", "Rossi", "mario@example.com", "HASH", "Milano", List.of(), List.of());

                PasswordResetToken token = new PasswordResetToken();
                token.setEmail("mario@example.com");
                token.setCreatedAt(LocalDateTime.now().minusSeconds(10));
                token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
                token.setUsed(false);

                when(userRepository.findByEmail("mario@example.com"))
                                .thenReturn(Optional.of(user));

                when(passwordResetTokenRepository.findTopByEmailOrderByCreatedAtDesc("mario@example.com"))
                                .thenReturn(Optional.of(token));

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> passwordResetService.forgotPassword(request));

                assertEquals("Aspetta almeno 30 secondi prima di richiedere un nuovo codice", ex.getMessage());
        }

        @Test
        void resetPassword_shouldThrowException_whenCodeIsInvalid() {
                ResetPasswordRequest request = new ResetPasswordRequest(
                                "mario@example.com",
                                "123456",
                                "NewPass1");

                when(passwordResetTokenRepository.findByEmailAndCodeAndUsedFalse("mario@example.com", "123456"))
                                .thenReturn(Optional.empty());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> passwordResetService.resetPassword(request));

                assertEquals("Codice non valido", ex.getMessage());
        }

        @Test
        void resetPassword_shouldThrowException_whenCodeIsExpired() {
                ResetPasswordRequest request = new ResetPasswordRequest(
                                "mario@example.com",
                                "123456",
                                "NewPass1");

                PasswordResetToken token = new PasswordResetToken();
                token.setEmail("mario@example.com");
                token.setCode("123456");
                token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
                token.setUsed(false);

                when(passwordResetTokenRepository.findByEmailAndCodeAndUsedFalse("mario@example.com", "123456"))
                                .thenReturn(Optional.of(token));

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> passwordResetService.resetPassword(request));

                assertEquals("Codice scaduto", ex.getMessage());
        }
}