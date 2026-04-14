package com.dem.ldp.controller.evento;


import org.junit.jupiter.api.Test;

import org.springframework.http.MediaType;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class CreazioneEventoControllerTest extends BaseEventoControllerTest {
     @Test
    void creazioneEventoValido_restituisce200ESalvaEvento() throws Exception {
        String json = """
                {
                  "titolo": "Gruppo di studio Java",
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java"],
                  "descrizione": "Ripasso",
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true
                }
                """;

        mockMvc.perform(post("/Eventi")
                .sessionAttr("userId", utenteId) // <--- ECCO IL TRUCCO: iniettiamo l'ID in sessione
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk()) // Verifica che torni 200
                .andExpect(content().string(org.hamcrest.Matchers.containsString("creato con successo")));

        assertEquals(1, eventoRepository.count());
    }

    @Test
    void creazioneEventoSenzaTitolo_restituisce400() throws Exception {
        String json = """
                {
                  "titolo": "",
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java"],
                  "maxPartecipanti": 10
                }
                """;

        mockMvc.perform(post("/Eventi")
                .sessionAttr("userId", utenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest()); // Verifica che torni 400

        assertEquals(0, eventoRepository.count());
    }
}
