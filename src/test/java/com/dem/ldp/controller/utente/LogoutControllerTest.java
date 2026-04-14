package com.dem.ldp.controller.utente;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

public class LogoutControllerTest extends BaseUtenteControllerTest {

    @Test
    void flussoCompleto_LoginLogoutAccessoNegato() throws Exception {
        // --- 0. PREPARAZIONE ---
        String registrazione = """
                {
                "nome": "Mario",
                "cognome": "Rossi",
                "email": "mario@example.com",
                "password": "Password123"
                }
                """;

        // Registriamo l'utente usando il mockMvc ereditato dalla base
        mockMvc.perform(post("/Utenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrazione))
                .andExpect(status().isOk());

        // --- 1. LOGIN ---
        String login = """
                {
                "email": "mario@example.com",
                "password": "Password123"
                }
                """;

        // Eseguiamo il login e "catturiamo" la sessione restituita
        MockHttpSession sessione = (MockHttpSession) mockMvc.perform(post("/Utenti/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(login))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();

        assertNotNull(sessione, "La sessione dovrebbe essere stata creata dopo il login");

        // --- 2. VERIFICA ACCESSO (OK) ---
        // Passiamo l'oggetto sessione direttamente alla richiesta
        mockMvc.perform(get("/Utenti/me")
                .session(sessione))
                .andExpect(status().isOk());

        // --- 3. LOGOUT ---
        mockMvc.perform(post("/Utenti/logout")
                .session(sessione))
                .andExpect(status().isOk());

        // --- 4. VERIFICA ACCESSO (NEGATO) ---
        // Proviamo ad accedere di nuovo con la stessa sessione: deve dare 4xx
        // (Unauthorized)
        mockMvc.perform(get("/Utenti/me")
                .session(sessione))
                .andExpect(status().is4xxClientError());

        // Controllo extra: la sessione deve risultare invalidata
        assertTrue(sessione.isInvalid(), "La sessione deve essere marcata come invalida dopo il logout");
    }
}