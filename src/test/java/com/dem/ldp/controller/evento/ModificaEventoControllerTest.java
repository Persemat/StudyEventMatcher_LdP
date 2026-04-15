package com.dem.ldp.controller.evento;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

import jakarta.transaction.Transactional;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class ModificaEventoControllerTest extends BaseEventoControllerTest {

    @Test
    void modificaEventoValido_restituisce200EAggiornaEvento() throws Exception {
        Evento evento = new Evento();
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.of(2026, 10, 15, 15, 0));
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(utenteRepository.findById(utenteId).orElseThrow());
        evento.aggiungiPartecipante(utenteRepository.findById(utenteId).orElseThrow());
        evento = eventoRepository.save(evento);

        String json = """
                {
                  "titolo": "Progetto Java Aggiornato",
                  "dataOra": "2026-10-16T17:00:00",
                  "durata": 3.0,
                  "luogo": "Biblioteca",
                  "tag": ["Java", "Backend"],
                  "descrizione": "Descrizione aggiornata",
                  "maxPartecipanti": 12,
                  "partecipazioneCreatoreNecessaria": false
                }
                """;

        mockMvc.perform(put("/Eventi/" + evento.getId())
                .sessionAttr("userId", utenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("modificato con successo")));

        Evento eventoAggiornato = eventoRepository.findById(evento.getId()).orElseThrow();

        assertEquals("Progetto Java Aggiornato", eventoAggiornato.getTitolo());
        assertEquals(LocalDateTime.of(2026, 10, 16, 17, 0), eventoAggiornato.getDataOra());
        assertEquals(3.0, eventoAggiornato.getDurata());
        assertEquals("Biblioteca", eventoAggiornato.getLuogo());
        assertTrue(eventoAggiornato.getTag().contains("Backend"));
        assertEquals("Descrizione aggiornata", eventoAggiornato.getDescrizione());
        assertEquals(12, eventoAggiornato.getMaxPartecipanti());
        assertEquals(false, eventoAggiornato.isPartecipazioneCreatoreNecessaria());
    }

    @Test
    void modificaEventoConTitoloVuoto_restituisce400() throws Exception {
        Evento evento = new Evento();
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.of(2026, 10, 15, 15, 0));
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(utenteRepository.findById(utenteId).orElseThrow());
        evento.aggiungiPartecipante(utenteRepository.findById(utenteId).orElseThrow());
        evento = eventoRepository.save(evento);

        String json = """
                {
                  "titolo": "",
                  "dataOra": "2026-10-16T17:00:00",
                  "durata": 3.0,
                  "luogo": "Biblioteca",
                  "tag": ["Java"],
                  "descrizione": "Descrizione aggiornata",
                  "maxPartecipanti": 12,
                  "partecipazioneCreatoreNecessaria": false
                }
                """;

        mockMvc.perform(put("/Eventi/" + evento.getId())
                .sessionAttr("userId", utenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Campi obbligatori mancanti")));
    }

    @Test
    void modificaEventoConSessioneNonValida_restituisce401() throws Exception {
        Evento evento = new Evento();
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.of(2026, 10, 15, 15, 0));
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(utenteRepository.findById(utenteId).orElseThrow());
        evento.aggiungiPartecipante(utenteRepository.findById(utenteId).orElseThrow());
        evento = eventoRepository.save(evento);

        String json = """
                {
                  "titolo": "Progetto Java Aggiornato",
                  "dataOra": "2026-10-16T17:00:00",
                  "durata": 3.0,
                  "luogo": "Biblioteca",
                  "tag": ["Java"],
                  "descrizione": "Descrizione aggiornata",
                  "maxPartecipanti": 12,
                  "partecipazioneCreatoreNecessaria": false
                }
                """;

        mockMvc.perform(put("/Eventi/" + evento.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isUnauthorized())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Sessione non valida o scaduta")));
    }

    @Test
    void modificaEventoDaUtenteNonCreatore_restituisce400() throws Exception {
        Utente altroUtente = new Utente();
        altroUtente.setNome("Luca");
        altroUtente.setCognome("Verdi");
        altroUtente.setEmail("luca_modifica_test@example.com");
        altroUtente.setPassword("Password123");
        altroUtente = utenteRepository.save(altroUtente);

        Evento evento = new Evento();
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.of(2026, 10, 15, 15, 0));
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(altroUtente);
        evento.aggiungiPartecipante(altroUtente);
        evento = eventoRepository.save(evento);

        String json = """
                {
                  "titolo": "Progetto Java Aggiornato",
                  "dataOra": "2026-10-16T17:00:00",
                  "durata": 3.0,
                  "luogo": "Biblioteca",
                  "tag": ["Java"],
                  "descrizione": "Descrizione aggiornata",
                  "maxPartecipanti": 12,
                  "partecipazioneCreatoreNecessaria": false
                }
                """;

        mockMvc.perform(put("/Eventi/" + evento.getId())
                .sessionAttr("userId", utenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Solo il creatore può modificare l'evento")));
    }

    @Test
    void modificaEventoConMaxPartecipantiMinoreDeiPartecipantiAttuali_restituisce400() throws Exception {
        Utente altroUtente = new Utente();
        altroUtente.setNome("Luca");
        altroUtente.setCognome("Verdi");
        altroUtente.setEmail("luca_partecipante_test@example.com");
        altroUtente.setPassword("Password123");
        altroUtente = utenteRepository.save(altroUtente);

        Evento evento = new Evento();
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.of(2026, 10, 15, 15, 0));
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(utenteRepository.findById(utenteId).orElseThrow());
        evento.aggiungiPartecipante(utenteRepository.findById(utenteId).orElseThrow());
        evento.aggiungiPartecipante(altroUtente);
        evento = eventoRepository.save(evento);

        String json = """
                {
                  "titolo": "Progetto Java Aggiornato",
                  "dataOra": "2026-10-16T17:00:00",
                  "durata": 3.0,
                  "luogo": "Biblioteca",
                  "tag": ["Java"],
                  "descrizione": "Descrizione aggiornata",
                  "maxPartecipanti": 1,
                  "partecipazioneCreatoreNecessaria": false
                }
                """;

        mockMvc.perform(put("/Eventi/" + evento.getId())
                .sessionAttr("userId", utenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("numero massimo di partecipanti")));
    }
}