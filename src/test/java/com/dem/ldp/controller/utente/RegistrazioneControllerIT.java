package com.dem.ldp.controller.utente;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RegistrazioneControllerTest extends BaseUtenteControllerTest {

    @Test
    void registrazioneValida_restituisce200ESalvaUtente() throws Exception {
        String json = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/Utenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("mario@example.com")));

        // questo resta UGUALE a prima
        assertTrue(utenteRepository.findByEmail("mario@example.com").isPresent());
    }

    @Test
    void registrazioneEmailGiaPresente_restituisce400() throws Exception {
        String json = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        mockMvc.perform(post("/Utenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk());

        mockMvc.perform(post("/Utenti")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(containsString("Email già registrata")));
    }
}