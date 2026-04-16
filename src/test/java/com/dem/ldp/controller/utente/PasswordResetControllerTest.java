package com.dem.ldp.controller.utente;

import com.dem.ldp.model.PasswordResetToken;
import com.dem.ldp.model.Utente;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class PasswordResetControllerTest extends BasePasswordResetControllerTest {

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void forgotPasswordValida_restituisce200ESalvaToken() throws Exception {
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        String json = """
                {
                  "email": "mario@example.com"
                }
                """;

        mockMvc.perform(post("/password-reset/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Codice di recupero inviato via email")));

        assertTrue(passwordResetTokenRepository.findAll().stream()
                .anyMatch(token -> token.getEmail().equals("mario@example.com")));
    }

    @Test
    void forgotPasswordConEmailNonRegistrata_restituisce400() throws Exception {
        String json = """
                {
                  "email": "mario@example.com"
                }
                """;

        mockMvc.perform(post("/password-reset/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Nessun utente trovato con questa email")));
    }

    @Test
    void forgotPasswordRichiestaTroppoRavvicinata_restituisce400() throws Exception {
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCodice("123456");
        token.setCreatoIl(LocalDateTime.now().minusSeconds(10));
        token.setScadenza(LocalDateTime.now().plusMinutes(10));
        token.setUsato(false);
        passwordResetTokenRepository.save(token);

        String json = """
                {
                  "email": "mario@example.com"
                }
                """;

        mockMvc.perform(post("/password-reset/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Attendi almeno 30 secondi prima di richiedere un nuovo codice")));
    }

    @Test
    void resetPasswordValido_restituisce200EAggiornaPassword() throws Exception {
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCodice("123456");
        token.setCreatoIl(LocalDateTime.now().minusMinutes(1));
        token.setScadenza(LocalDateTime.now().plusMinutes(10));
        token.setUsato(false);
        passwordResetTokenRepository.save(token);

        String json = """
                {
                  "email": "mario@example.com",
                  "codice": "123456",
                  "nuovaPassword": "NuovaPass1"
                }
                """;

        mockMvc.perform(post("/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Password aggiornata con successo")));

        Utente aggiornato = utenteRepository.findByEmail("mario@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("NuovaPass1", aggiornato.getPassword()));
    }

    @Test
    void resetPasswordConCodiceNonValido_restituisce400() throws Exception {
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        String json = """
                {
                  "email": "mario@example.com",
                  "codice": "999999",
                  "nuovaPassword": "NuovaPass1"
                }
                """;

        mockMvc.perform(post("/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Codice non valido")));
    }

    @Test
    void resetPasswordConCodiceScaduto_restituisce400() throws Exception {
        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCodice("123456");
        token.setCreatoIl(LocalDateTime.now().minusMinutes(20));
        token.setScadenza(LocalDateTime.now().minusMinutes(1));
        token.setUsato(false);
        passwordResetTokenRepository.save(token);

        String json = """
                {
                  "email": "mario@example.com",
                  "codice": "123456",
                  "nuovaPassword": "NuovaPass1"
                }
                """;

        mockMvc.perform(post("/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Codice scaduto")));
    }
}