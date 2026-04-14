package com.dem.ldp.controller.utente;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class LoginControllerTest extends BaseUtenteControllerTest {

    @Test
    void loginValido_restituisce200() throws Exception {
        String registrazione = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/Utenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrazione))
                .andExpect(status().isOk());

        String login = """
                {
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/Utenti/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("mario@example.com")));
    }

    @Test
    void loginPasswordErrata_restituisce400() throws Exception {
        String registrazione = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/Utenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registrazione))
                .andExpect(status().isOk());

        String login = """
                {
                  "email": "mario@example.com",
                  "password": "Password999"
                }
                """;

        mockMvc.perform(post("/Utenti/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Credenziali non valide")));
    }
}