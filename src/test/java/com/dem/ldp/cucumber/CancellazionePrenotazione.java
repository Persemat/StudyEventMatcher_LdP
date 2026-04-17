package com.dem.ldp.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CancellazionePrenotazione {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private MockHttpSession session = new MockHttpSession();
    private Long eventoIdCorrente;
    private int partecipantiIniziali;

    @Given("l'utente è autenticato per la cancellazione")
    public void utenteAutenticatoPerCancellazione() {
        utenteRepository.deleteAll();
        Utente u = new Utente();
        u.setNome("Mario");
        u.setCognome("Rossi");
        u.setEmail("mario@test.com");
        u.setPassword("Pass123!");
        u = utenteRepository.save(u);

        session.setAttribute("userId", u.getId());
    }

    @Given("l'utente non è autenticato per la cancellazione")
    public void utenteNonAutenticato() {
        session.clearAttributes();
    }

    @And("l'utente ha una prenotazione per un evento")
    public void utenteHaUnaPrenotazione() {
        eventoRepository.deleteAll();
        Utente u = utenteRepository.findById((Long) session.getAttribute("userId")).get();

        Evento e = creaEventoBase();
        e.aggiungiPartecipante(u); // Prenotazione!
        e = eventoRepository.save(e);

        this.eventoIdCorrente = e.getId();
    }

    @And("l'utente non è prenotato a quell'evento")
    public void utenteNonPrenotato() {
        eventoRepository.deleteAll();
        Evento e = creaEventoBase();
        e = eventoRepository.save(e); // Viene salvato senza aggiungere l'utente

        this.eventoIdCorrente = e.getId();
    }

    @Transactional
    @And("l'utente ha una prenotazione per un evento con posti limitati")
    public void utenteHaPrenotazionePostiLimitati() {
        utenteHaUnaPrenotazione(); // Sfruttiamo il metodo sopra per creare l'iscrizione

        // Segnamoci quanti partecipanti ci sono all'inizio (dovrebbe essere 1)
        Evento e = eventoRepository.findById(eventoIdCorrente).get();
        this.partecipantiIniziali = e.getPartecipanti().size();
    }

    @And("il sistema chiede l'autenticazione")
    public void ilSistemaRichiedeLAutenticazione() throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    @Then("il sistema mostra il messaggio di cancellazione {string}")
    public void ilSistemaMostra(String messaggio) throws Exception {
        result.andExpect(content().string(Matchers.containsString(messaggio)));
    }

    @When("l'utente cancella la prenotazione")
    public void provaACancellarePrenotazione() throws Exception {
        result = mockMvc.perform(delete("/Eventi/" + eventoIdCorrente + "/prenota").session(session));
    }

    @When("l'utente prova a cancellare la prenotazione")
    public void provaACancellareUnaPrenotazione() throws Exception {
        // Se l'evento non è stato creato prima (come nello scenario 3), usiamo un ID
        // fittizio "1" per far arrivare la richiesta al controller
        Long idDaCancellare = (eventoIdCorrente != null) ? eventoIdCorrente : 1L;

        result = mockMvc.perform(delete("/Eventi/" + idDaCancellare + "/prenota").session(session));
    }

    @Then("il sistema rimuove la prenotazione")
    public void ilSistemaRimuoveLaPrenotazione() throws Exception {
        result.andExpect(status().isOk());
    }

    @Then("il sistema impedisce l'operazione")
    public void ilSistemaImpedisceOperazione() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @Transactional
    @Then("il sistema aumenta il numero di posti disponibili")
    public void ilSistemaAumentaPostiDisponibili() throws Exception {
        // Grazie al @Transactional qui, possiamo rileggere la lista aggiornata
        Evento eAggiornato = eventoRepository.findById(eventoIdCorrente).get();
        int partecipantiAttuali = eAggiornato.getPartecipanti().size();

        org.junit.jupiter.api.Assertions.assertTrue(partecipantiAttuali < partecipantiIniziali);
    }

    // Metodo Helper (per non ripetere codice)
    private Evento creaEventoBase() {
        Utente creatore = new Utente();
        creatore.setNome("Mario");
        creatore.setCognome("Rossi");
        creatore.setEmail("creatore" + Math.random() + "@test.com"); // Email univoca
        creatore.setPassword("Pass123!");
        creatore = utenteRepository.save(creatore);

        Evento e = new Evento();
        e.setTitolo("Evento di Test");
        e.setDescrizione("Test Cucumber");
        e.setLuogo("Aula Virtuale");
        e.setDataOra(LocalDateTime.now().plusDays(5));
        e.setDurata(2.0);
        e.setMaxPartecipanti(10);
        e.setStato("aperto");
        e.setPartecipazioneCreatoreNecessaria(false);
        e.setCreatore(creatore);

        if (e.getPartecipanti() == null) {
            e.setPartecipanti(new HashSet<>());
        }
        e.aggiungiPartecipante(creatore);

        return e;
    }
}