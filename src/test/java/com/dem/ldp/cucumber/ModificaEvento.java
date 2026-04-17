package com.dem.ldp.cucumber;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ModificaEvento {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private MockHttpSession session;
    private Utente utente;
    private Utente altroUtente;
    private Evento evento;
    private ResultActions result;
    private String requestBody;

    @Given("l'utente è autenticato per la modifica")
    public void utenteAutenticatoPerModifica() {
        utenteRepository.deleteAll();
        eventoRepository.deleteAll();

        utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password123");
        utente = utenteRepository.save(utente);

        session = new MockHttpSession();
        session.setAttribute("userId", utente.getId());
    }

    @And("esiste un evento {string} creato dall'utente")
    public void esisteEventoCreatoDallUtente(String titolo) {
        evento = new Evento();
        evento.setTitolo(titolo);
        evento.setDescrizione("Descrizione iniziale");
        evento.setLuogo("Aula 1");
        evento.setDataOra(LocalDateTime.now().plusDays(2));
        evento.setDurata(2);
        evento.setMaxPartecipanti(10);
        evento.setTag(List.of("Java"));
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(utente);
        evento.aggiungiPartecipante(utente);

        evento = eventoRepository.save(evento);
    }

    @And("l'evento inizia tra più di un giorno")
    public void eventoIniziaTraPiuDiUnGiorno() {
        evento.setDataOra(LocalDateTime.now().plusDays(2));
        eventoRepository.save(evento);
    }

    @When("l'utente modifica l'orario di inizio dell'evento")
    public void modificaOrarioEvento() {
        requestBody = """
                {
                  "titolo": "Progetto Java",
                  "descrizione": "Descrizione iniziale",
                  "dataOra": "%s",
                  "durata": 2.0,
                  "luogo": "Aula 1",
                  "tag": ["Java"],
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true
                }
                """.formatted(LocalDateTime.now().plusDays(3).withNano(0).toString());
    }

    @And("salva le modifiche")
    public void salvaLeModifiche() throws Exception {
        result = mockMvc.perform(
                put("/Eventi/" + evento.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody));
    }

    @Then("il sistema aggiorna l'evento correttamente")
    public void sistemaAggiornaEvento() throws Exception {
        result.andExpect(status().isOk());

        Evento aggiornato = eventoRepository.findById(evento.getId()).orElseThrow();
        assertNotEquals(evento.getDataOra(), aggiornato.getDataOra());
    }

    @And("il sistema mostra il messaggio di conferma {string}")
    public void sistemaMostraMessaggio(String messaggio) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        assertTrue(body.contains(messaggio));
    }

    @When("l'utente lascia il campo titolo vuoto")
    public void lasciaTitoloVuoto() {
        requestBody = """
                {
                  "titolo": "",
                  "descrizione": "Descrizione iniziale",
                  "dataOra": "%s",
                  "durata": 2.0,
                  "luogo": "Aula 1",
                  "tag": ["Java"],
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true
                }
                """.formatted(LocalDateTime.now().plusDays(3).withNano(0).toString());
    }

    @Then("il sistema impedisce il salvataggio della modifica")
    public void impedisceSalvataggioModifica() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @And("il sistema mostra il messaggio di errore {string}")
    public void sistemaMostraMessaggioErrore(String messaggio) throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        assertTrue(body.contains(messaggio));
    }

    @And("esiste un evento {string} creato da un altro utente")
    public void esisteEventoCreatoDaAltroUtente(String titolo) {
        altroUtente = new Utente();
        altroUtente.setNome("Luca");
        altroUtente.setCognome("Verdi");
        altroUtente.setEmail("luca@example.com");
        altroUtente.setPassword("Password123");
        altroUtente = utenteRepository.save(altroUtente);

        evento = new Evento();
        evento.setTitolo(titolo);
        evento.setDescrizione("Descrizione");
        evento.setLuogo("Aula 2");
        evento.setDataOra(LocalDateTime.now().plusDays(2));
        evento.setDurata(2.0);
        evento.setMaxPartecipanti(8);
        evento.setTag(List.of("Java"));
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(altroUtente);
        evento.aggiungiPartecipante(altroUtente);

        evento = eventoRepository.save(evento);
    }

    @When("l'utente visualizza gli eventi disponibili")
    public void visualizzaEventiDisponibili() throws Exception {
        result = mockMvc.perform(
                get("/Eventi").session(session));
    }

    @Then("il pulsante modifica evento non è visibile")
    public void pulsanteModificaNonVisibile() throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        assertTrue(body.contains("\"isCreatedByMe\":false"));
    }
}