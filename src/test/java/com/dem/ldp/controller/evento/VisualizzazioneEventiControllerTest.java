package com.dem.ldp.controller.evento;


import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

public class VisualizzazioneEventiControllerTest extends BaseEventoControllerTest {
    @Test
void getAllEvents_restituisceListaEventi() throws Exception {
    Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

    Evento evento = new Evento();
    evento.setTitolo("Workshop React");
    evento.setDataOra(java.time.LocalDateTime.parse("2026-04-15T09:00:00"));
    evento.setDurata(1.5);
    evento.setLuogo("Online");
    evento.setTag(java.util.List.of("Frontend"));
    evento.setDescrizione("Creazione componenti modulari");
    evento.setStato("aperto");
    evento.setCreatore(creatore);

    eventoRepository.save(evento);

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].titolo").value("Workshop React"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].luogo").value("Online"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].dataOra").exists());
}
@Test
void getAllEvents_senzaEventi_restituisceListaVuota() throws Exception {
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
}
@Test
void getAllEvents_utenteNonAutenticato_accessoNegato() throws Exception {
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi"))
            .andExpect(status().isUnauthorized());
}
}
