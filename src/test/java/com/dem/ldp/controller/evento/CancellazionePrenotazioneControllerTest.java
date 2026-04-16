package com.dem.ldp.controller.evento;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CancellazionePrenotazioneControllerTest extends BaseEventoControllerTest {

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
        utente.setEmail("mario.cancella@example.com");
        utente.setPassword("Password123!");
        utente = utenteRepository.save(utente);

        creatore = new Utente();
        creatore.setNome("Luca");
        creatore.setCognome("Bianchi");
        creatore.setEmail("luca.creatore.cancella@example.com");
        creatore.setPassword("Password123!");
        creatore = utenteRepository.save(creatore);

        evento = new Evento();
        evento.setTitolo("Studio Basi di Dati");
        evento.setDescrizione("Ripasso SQL e NoSQL");
        evento.setLuogo("Aula 2");
        evento.setDataOra(LocalDateTime.now().plusDays(2));
        evento.setDurata(2.0);
        evento.setMaxPartecipanti(4);
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
    void cancellazionePrenotazioneConSuccesso() throws Exception {
        // Setup: Iscriviamo Mario *prima* di eseguire la cancellazione
        evento.aggiungiPartecipante(utente);
        eventoRepository.save(evento);

        session.setAttribute("userId", utente.getId());

        // Azione: Cancellazione della prenotazione
        mockMvc.perform(
                        delete("/Eventi/{id}/prenota", evento.getId())
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(content().string("Prenotazione cancellata con successo"));

        // Verifica: Simuliamo il React che ricarica la pagina e controlliamo i dati
        mockMvc.perform(
                        get("/Eventi")
                                .session(session)
                )
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].titolo").value(evento.getTitolo()))
                .andExpect(jsonPath("$[0].isBooked").value(false)) // Mario non deve più essere prenotato
                .andExpect(jsonPath("$[0].currentParticipants").value(1)); // Deve esserci solo il creatore
    }

    @Test
    void cancellazionePrenotazioneUtenteNonAutenticato() throws Exception {
        // Non settiamo userId nella sessione!
        
        mockMvc.perform(
                        delete("/Eventi/{id}/prenota", evento.getId())
                                .session(session)
                )
                .andExpect(status().isUnauthorized()) // Verifica lo status 401
                .andExpect(content().string("Sessione non valida o scaduta"));
    }

    @Test
    void cancellazionePrenotazioneEventoNonTrovato() throws Exception {
        session.setAttribute("userId", utente.getId());

        mockMvc.perform(
                        delete("/Eventi/{id}/prenota", 9999L) // ID inesistente
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Evento non trovato"));
    }

    @Test
    void cancellazionePrenotazioneUtenteNonPrenotato() throws Exception {
        session.setAttribute("userId", utente.getId());
        
        // Nel @BeforeEach Mario non è stato aggiunto all'evento, quindi non è prenotato!

        mockMvc.perform(
                        delete("/Eventi/{id}/prenota", evento.getId())
                                .session(session)
                )
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Errore: Non sei prenotato a questo evento")); 
    }
}
