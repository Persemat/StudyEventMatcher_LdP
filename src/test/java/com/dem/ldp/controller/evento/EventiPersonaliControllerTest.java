package com.dem.ldp.controller.evento;


import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

import org.junit.jupiter.api.Test;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventiPersonaliControllerTest extends BaseEventoControllerTest {
    @Test
void getAllEvents_eventoCreatoDaMe_haIsCreatedByMeTrue() throws Exception {
    Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

    Evento evento = new Evento();
    evento.setTitolo("Studio Basi di Dati");
    evento.setDataOra(java.time.LocalDateTime.parse("2026-04-10T15:00:00"));
    evento.setDurata(2.0);
    evento.setLuogo("Aula 2");
    evento.setTag(java.util.List.of("Informatica"));
    evento.setDescrizione("Ripasso SQL");
    evento.setStato("aperto");
    evento.setCreatore(creatore);

    eventoRepository.save(evento);

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].titolo").value("Studio Basi di Dati"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].dataOra").exists())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].luogo").value("Aula 2"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].tag[0]").value("Informatica"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].isCreatedByMe").value(true));
}

@Test
void getAllEvents_eventoCreatoDaAltroUtente_haIsCreatedByMeFalse() throws Exception {
    Utente altroUtente = new Utente();
    altroUtente.setNome("Marco");
    altroUtente.setCognome("Verdi");
    altroUtente.setEmail("marco@example.com");
    altroUtente.setPassword("Pass123");
    utenteRepository.save(altroUtente);

    Evento evento = new Evento();
    evento.setTitolo("Evento di un altro utente");
    evento.setDataOra(java.time.LocalDateTime.parse("2026-04-12T10:00:00"));
    evento.setDurata(3.0);
    evento.setLuogo("Biblioteca");
    evento.setTag(java.util.List.of("Matematica"));
    evento.setDescrizione("Esercizi");
    evento.setStato("aperto");
    evento.setCreatore(altroUtente);

    eventoRepository.save(evento);

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].titolo").value("Evento di un altro utente"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].isCreatedByMe").value(false));
}

@Test
void getAllEvents_utenteAutenticato_senzaEventiCreatiDaMe_nonHaEventiPersonali() throws Exception {
    Utente altroUtente = new Utente();
    altroUtente.setNome("Marco");
    altroUtente.setCognome("Verdi");
    altroUtente.setEmail("marco2@example.com");
    altroUtente.setPassword("Pass123");
    utenteRepository.save(altroUtente);

    Evento evento = new Evento();
    evento.setTitolo("Evento altrui");
    evento.setDataOra(java.time.LocalDateTime.parse("2026-04-12T10:00:00"));
    evento.setDurata(3.0);
    evento.setLuogo("Biblioteca");
    evento.setTag(java.util.List.of("Matematica"));
    evento.setDescrizione("Descrizione");
    evento.setStato("aperto");
    evento.setCreatore(altroUtente);

    eventoRepository.save(evento);

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].isCreatedByMe").value(false));
}
}
