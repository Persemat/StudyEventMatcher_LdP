package com.dem.ldp.controller;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class CancellazioneEventoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private Long utenteId;

    @BeforeEach
    void preparaDatabase() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        Utente utenteTest = new Utente();
        utenteTest.setNome("Luca");
        utenteTest.setCognome("Rossi");
        utenteTest.setEmail("luca@example.com");
        utenteTest.setPassword("Pass123");
        utenteRepository.save(utenteTest);

        this.utenteId = utenteTest.getId();
    }

    @Test
    void cancellaEvento_presenzaCreatoreObbligatoria_eliminaEvento() throws Exception {
        Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

        Evento evento = new Evento();
        evento.setTitolo("Evento con creatore obbligatorio");
        evento.setDataOra(LocalDateTime.parse("2026-04-20T15:00:00"));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 1");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Descrizione");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(creatore);
        evento.aggiungiPartecipante(creatore);

        for (int i = 1; i <= 5; i++) {
            Utente u = new Utente();
            u.setNome("Utente" + i);
            u.setCognome("Test");
            u.setEmail("utente" + i + "@example.com");
            u.setPassword("Pass123");
            utenteRepository.save(u);
            evento.aggiungiPartecipante(u);
        }

        eventoRepository.save(evento);
        long eventoId = evento.getId();

        mockMvc.perform(delete("/Eventi/" + eventoId)
                        .sessionAttr("userId", utenteId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Evento cancellato con successo")));

        assertFalse(eventoRepository.findById(eventoId).isPresent());
    }

    @Test
    void cancellaEvento_senzaPresenzaObbligatoria_conAltriIscritti_rimuoveCreatoreERiassegna() throws Exception {
        Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

        Utente altro1 = new Utente();
        altro1.setNome("Marco");
        altro1.setCognome("Verdi");
        altro1.setEmail("marco@example.com");
        altro1.setPassword("Pass123");
        utenteRepository.save(altro1);

        Utente altro2 = new Utente();
        altro2.setNome("Anna");
        altro2.setCognome("Bianchi");
        altro2.setEmail("anna@example.com");
        altro2.setPassword("Pass123");
        utenteRepository.save(altro2);

        Evento evento = new Evento();
        evento.setTitolo("Evento senza creatore obbligatorio");
        evento.setDataOra(LocalDateTime.parse("2026-04-20T15:00:00"));
        evento.setDurata(2.0);
        evento.setLuogo("Biblioteca");
        evento.setTag(List.of("Database"));
        evento.setDescrizione("Descrizione");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setStato("aperto");
        evento.setCreatore(creatore);
        evento.aggiungiPartecipante(creatore);
        evento.aggiungiPartecipante(altro1);
        evento.aggiungiPartecipante(altro2);

        eventoRepository.save(evento);
        long eventoId = evento.getId();

        mockMvc.perform(delete("/Eventi/" + eventoId)
                        .sessionAttr("userId", utenteId))
                .andExpect(status().isOk());

        Evento aggiornato = eventoRepository.findById(eventoId).orElseThrow();

        assertTrue(eventoRepository.findById(eventoId).isPresent());
        assertEquals(altro1.getId(), aggiornato.getCreatore().getId());
        assertFalse(aggiornato.getPartecipanti().stream().anyMatch(u -> u.getId().equals(utenteId)));
        assertTrue(aggiornato.getPartecipanti().stream().anyMatch(u -> u.getId().equals(altro1.getId())));
        assertTrue(aggiornato.getPartecipanti().stream().anyMatch(u -> u.getId().equals(altro2.getId())));
    }

    @Test
    void cancellaEvento_senzaPresenzaObbligatoria_senzaAltriIscritti_eliminaEvento() throws Exception {
        Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

        Evento evento = new Evento();
        evento.setTitolo("Evento da eliminare");
        evento.setDataOra(LocalDateTime.parse("2026-04-20T15:00:00"));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 3");
        evento.setTag(List.of("React"));
        evento.setDescrizione("Descrizione");
        evento.setMaxPartecipanti(5);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setStato("aperto");
        evento.setCreatore(creatore);
        evento.aggiungiPartecipante(creatore);

        eventoRepository.save(evento);
        long eventoId = evento.getId();

        mockMvc.perform(delete("/Eventi/" + eventoId)
                        .sessionAttr("userId", utenteId))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Evento cancellato con successo")));

        assertFalse(eventoRepository.findById(eventoId).isPresent());
    }

    @Test
    void cancellaEvento_utenteNonCreatore_restituisceErrore() throws Exception {
        Utente creatore = new Utente();
        creatore.setNome("Marco");
        creatore.setCognome("Verdi");
        creatore.setEmail("marco2@example.com");
        creatore.setPassword("Pass123");
        utenteRepository.save(creatore);

        Evento evento = new Evento();
        evento.setTitolo("Evento altrui");
        evento.setDataOra(LocalDateTime.parse("2026-04-20T15:00:00"));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 5");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Descrizione");
        evento.setMaxPartecipanti(8);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(creatore);
        evento.aggiungiPartecipante(creatore);

        eventoRepository.save(evento);
        long eventoId = evento.getId();

        mockMvc.perform(delete("/Eventi/" + eventoId)
                        .sessionAttr("userId", utenteId))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Solo il creatore può cancellare l'evento")));

        assertTrue(eventoRepository.findById(eventoId).isPresent());
    }

    @Test
    void cancellaEvento_utenteNonAutenticato_restituisce401() throws Exception {
        Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

        Evento evento = new Evento();
        evento.setTitolo("Evento protetto");
        evento.setDataOra(LocalDateTime.parse("2026-04-20T15:00:00"));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 6");
        evento.setTag(List.of("Sicurezza"));
        evento.setDescrizione("Descrizione");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(creatore);
        evento.aggiungiPartecipante(creatore);

        eventoRepository.save(evento);
        long eventoId = evento.getId();

        mockMvc.perform(delete("/Eventi/" + eventoId))
                .andExpect(status().isUnauthorized());
    }
}