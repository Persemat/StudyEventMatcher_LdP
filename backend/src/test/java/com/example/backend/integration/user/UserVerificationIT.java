package com.example.backend.integration.user;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;

import com.example.backend.model.UserVerification;
import com.example.backend.repository.UserVerificationRepository;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@Tag("integration")
// FORZIAMO LA FEATURE ATTIVA SOLO PER QUESTO FILE DI TEST
@TestPropertySource(properties = "app.security.require-email-verification=true")
public class UserVerificationIT extends BaseUserIT {

        @Autowired
        private UserVerificationRepository verificationRepository;

        @Test
        void testFullVerificationFlow_EndToEnd_Success() throws Exception {
                String email = "mario.endtoend@example.com";

                // 1. REGISTRAZIONE: L'utente si registra normalmente
                String registrationJson = """
                                {
                                  "name": "Mario",
                                  "lastname": "Rossi",
                                  "email": "%s",
                                  "password": "Password123!",
                                  "favouriteCity": "Milano",
                                  "macroTags": ["Informatica"],
                                  "microTags": []
                                }
                                """.formatted(email);

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registrationJson))
                                .andExpect(status().isOk());

                // Recuperiamo il token Sidecar generato in automatico dal DB
                UserVerification verification = verificationRepository.findById(email)
                                .orElseThrow(() -> new AssertionError("Record Sidecar non creato nel database"));

                assertFalse(verification.isVerified(), "L'utente dovrebbe nascere NON verificato");
                String token = verification.getToken();

                // 2. TENTATIVO DI LOGIN ANTE-VERIFICA: Deve essere bloccato
                String loginJson = """
                                {
                                  "email": "%s",
                                  "password": "Password123!"
                                }
                                """.formatted(email);

                mockMvc.perform(post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString(
                                                "Devi confermare la tua email prima di poter accedere")));

                // 3. CLIC SUL LINK DI VERIFICA (Caso di successo): Verifica il testo dentro la
                // pagina HTML di risposta
                mockMvc.perform(get("/user/verify")
                                .param("token", token))
                                .andExpect(status().isOk())
                                .andExpect(jsonPath("$.status").value("success"))
                                .andExpect(jsonPath("$.message").value(
                                                "'Email verificata con successo! Verrai reindirizzato al login...'"));

                // Verifichiamo sul DB che lo stato sia cambiato in TRUE
                UserVerification verificationPost = verificationRepository.findById(email).orElseThrow();
                assertTrue(verificationPost.isVerified(), "Lo stato nel DB dovrebbe essere ora verificato (true)");

                // 4. LOGIN POST-VERIFICA: Ora deve funzionare ed entrare con successo
                mockMvc.perform(post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(loginJson))
                                .andExpect(status().isOk());
        }

        @Test
        void testVerification_ShouldFail_WhenTokenIsAlreadyUsed() throws Exception {
                String email = "mario.riutilizzato@example.com";

                // 1. REGISTRAZIONE
                String registrationJson = """
                                {
                                  "name": "Mario",
                                  "lastname": "Rossi",
                                  "email": "%s",
                                  "password": "Password123!",
                                  "favouriteCity": "Milano",
                                  "macroTags": ["Informatica"],
                                  "microTags": []
                                }
                                """.formatted(email);

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registrationJson))
                                .andExpect(status().isOk());

                // Recuperiamo il record dal DB
                UserVerification verification = verificationRepository.findById(email).orElseThrow();
                String token = verification.getToken();

                // 2. PRIMO UTILIZZO (Successo)
                mockMvc.perform(get("/user/verify")
                                .param("token", token))
                                .andExpect(status().isOk());

                // 3. SECONDO UTILIZZO (Fallimento): Ci clicco di nuovo, intercettiamo il blocco
                // HTML d'errore
                // Il test cerca il titolo "Errore di verifica" nell'HTML generato dal catch del
                // tuo controller
                mockMvc.perform(get("/user/verify")
                                .param("token", token))
                                .andExpect(status().isBadRequest())
                                .andExpect(jsonPath("$.status").value("error"))
                                .andExpect(jsonPath("$.message").value("Token di verifica non valido o scaduto."));
        }
}