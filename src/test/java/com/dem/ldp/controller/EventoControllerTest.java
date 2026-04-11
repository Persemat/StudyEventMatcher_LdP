
package com.dem.ldp.controller;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc // <--- Abilita MockMvc
@ActiveProfiles("test")
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc; // <--- Usiamo questo al posto di RestTemplate

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
@Test
void getAllEvents_utenteNonAutenticato_accessoNegato() throws Exception {
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi"))
            .andExpect(status().isUnauthorized());
}
}