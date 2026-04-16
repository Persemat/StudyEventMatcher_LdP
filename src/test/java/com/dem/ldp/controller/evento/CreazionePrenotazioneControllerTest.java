package com.dem.ldp.controller.evento;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import java.time.LocalDateTime;
import java.util.HashSet;


public class CreazionePrenotazioneControllerTest extends BaseEventoControllerTest {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private MockHttpSession session;
    private Utente utente;
    private Utente creatore;
    private Evento evento;

    @BeforeEach
    void preparaDatabase() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        session = new MockHttpSession();

        utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario.prenotazione@example.com");
        utente.setPassword("Password123!");
        utente = utenteRepository.save(utente);

        creatore = new Utente();
        creatore.setNome("Luca");
        creatore.setCognome("Bianchi");
        creatore.setEmail("luca.creatore@example.com");
        creatore.setPassword("Password123!");
        creatore = utenteRepository.save(creatore);

        evento = new Evento();
        evento.setTitolo("Studio Ingegneria del Software");
        evento.setDescrizione("Sessione di studio");
        evento.setLuogo("Aula 1");
        evento.setDataOra(LocalDateTime.now().plusDays(1));
        evento.setDurata(2.0);
        evento.setMaxPartecipanti(3);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setStato("aperto");
        evento.setCreatore(creatore);

        if (evento.getPartecipanti() == null) {
            evento.setPartecipanti(new HashSet<>());
        }
        evento.aggiungiPartecipante(creatore);

        evento = eventoRepository.save(evento);
    }

    @Test
void prenotazioneConSuccesso() throws Exception {
    session.setAttribute("userId", utente.getId());

    mockMvc.perform(
                    post("/Eventi/{id}/prenota", evento.getId())
                            .session(session)
            )
            .andExpect(status().isOk())
            .andExpect(content().string("Prenotazione completata con successo"));

    mockMvc.perform(
                    get("/Eventi")
                            .session(session)
            )
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].titolo").value(evento.getTitolo()))
            .andExpect(jsonPath("$[0].isBooked").value(true))
            .andExpect(jsonPath("$[0].currentParticipants").value(2));
}
    @Test
    void prenotazioneUtenteNonAutenticato() throws Exception {
        mockMvc.perform(
                        post("/Eventi/{id}/prenota", evento.getId())
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Utente non autenticato"));
    }

    @Test
    void prenotazioneEventoNonTrovato_restituisce400() throws Exception {
        session.setAttribute("userId", utente.getId());

        mockMvc.perform(
                        post("/Eventi/{id}/prenota", 9999L)
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Evento non trovato"));
    }

    @Test
    void prenotazioneGiaEsistente_restituisce400() throws Exception {
        session.setAttribute("userId", utente.getId());

        evento.aggiungiPartecipante(utente);
        eventoRepository.save(evento);

        mockMvc.perform(
                        post("/Eventi/{id}/prenota", evento.getId())
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Sei già prenotato a questo evento"));
    }

    @Test
    void prenotazioneEventoAlCompleto_restituisce400() throws Exception {
        session.setAttribute("userId", utente.getId());

        evento.setMaxPartecipanti(1);
        eventoRepository.save(evento);

        mockMvc.perform(
                        post("/Eventi/{id}/prenota", evento.getId())
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Evento al completo"));
    }
}