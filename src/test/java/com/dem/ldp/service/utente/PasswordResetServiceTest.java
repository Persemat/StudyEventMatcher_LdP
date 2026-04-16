package com.dem.ldp.service.utente;

import com.dem.ldp.dto.ForgotPasswordRequest;
import com.dem.ldp.dto.ResetPasswordRequest;
import com.dem.ldp.model.PasswordResetToken;
import com.dem.ldp.model.Utente;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class PasswordResetServiceTest extends BasePasswordResetServiceTest {

    @Test
    void forgotPasswordConEmailVuota_lanciaEccezione() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.forgotPassword(request)
        );

        assertEquals("Email obbligatoria", ex.getMessage());
    }

    @Test
    void forgotPasswordConEmailNonRegistrata_lanciaEccezione() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("mario@example.com");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.forgotPassword(request)
        );

        assertEquals("Nessun utente trovato con questa email", ex.getMessage());
    }

    @Test
    void forgotPasswordValida_inviaCodiceESalvaToken() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("mario@example.com");
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", "HASH");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(utente));
        when(passwordResetTokenRepository.findTopByEmailOrderByCreatoIlDesc("mario@example.com"))
                .thenReturn(Optional.empty());

        String risultato = passwordResetService.forgotPassword(request);

        assertEquals("Codice di recupero inviato via email", risultato);
        verify(passwordResetTokenRepository).save(any(PasswordResetToken.class));
        verify(emailService).inviaCodiceRecuperoPassword(eq("mario@example.com"), any(String.class));
    }

    @Test
    void forgotPasswordRichiestaTroppoRavvicinata_lanciaEccezione() {
        ForgotPasswordRequest request = new ForgotPasswordRequest("mario@example.com");
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", "HASH");

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCreatoIl(LocalDateTime.now().minusSeconds(10));
        token.setScadenza(LocalDateTime.now().plusMinutes(10));
        token.setUsato(false);

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(utente));
        when(passwordResetTokenRepository.findTopByEmailOrderByCreatoIlDesc("mario@example.com"))
                .thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.forgotPassword(request)
        );

        assertEquals("Attendi almeno 30 secondi prima di richiedere un nuovo codice", ex.getMessage());
    }

    @Test
    void resetPasswordConCodiceNonValido_lanciaEccezione() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "mario@example.com",
                "123456",
                "NuovaPass1"
        );

        when(passwordResetTokenRepository.findByEmailAndCodiceAndUsatoFalse("mario@example.com", "123456"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals("Codice non valido", ex.getMessage());
    }

    @Test
    void resetPasswordConCodiceScaduto_lanciaEccezione() {
        ResetPasswordRequest request = new ResetPasswordRequest(
                "mario@example.com",
                "123456",
                "NuovaPass1"
        );

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCodice("123456");
        token.setScadenza(LocalDateTime.now().minusMinutes(1));
        token.setUsato(false);

        when(passwordResetTokenRepository.findByEmailAndCodiceAndUsatoFalse("mario@example.com", "123456"))
                .thenReturn(Optional.of(token));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> passwordResetService.resetPassword(request)
        );

        assertEquals("Codice scaduto", ex.getMessage());
    }
}