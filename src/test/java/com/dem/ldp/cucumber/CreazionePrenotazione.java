package com.dem.ldp.cucumber;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;


public class CreazionePrenotazione {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private MockHttpSession session;
    private Utente utente;
    private Evento evento;
    private ResultActions result;
    private ResultActions resultGet;

    private static final String EVENTI_URL = "/Eventi";

    @Before
    public void setUp() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        session = new MockHttpSession();
        utente = null;
        evento = null;
        result = null;
    }

    @Given("esiste un utente autenticato per la prenotazione")
    public void esisteUnUtenteAutenticatoPerLaPrenotazione() {
        utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario.prenotazione@example.com");
        utente.setPassword("Password123!");
        utente = utenteRepository.save(utente);

        session.setAttribute("userId", utente.getId());
    }

    @And("esiste un evento aperto e non al completo")
    public void esisteUnEventoApertoENonAlCompleto() {
        Utente creatore = new Utente();
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
    @Then("il sistema impedisce la prenotazione per evento pieno")
    public void ilSistemaImpedisceLaPrenotazionePerEventoPieno() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @And("il sistema mostra il messaggio di evento al completo")
    public void ilSistemaMostraIlMessaggioDiEventoAlCompleto() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Errore: Evento al completo")));
    }


    @And("esiste un evento aperto ma al completo")
    public void esisteUnEventoApertoMaAlCompleto() {
        Utente creatore = new Utente();
        creatore.setNome("Giulia");
        creatore.setCognome("Verdi");
        creatore.setEmail("giulia.creatore@example.com");
        creatore.setPassword("Password123!");
        creatore = utenteRepository.save(creatore);

        evento = new Evento();
        evento.setTitolo("Progetto Basi di Dati");
        evento.setDescrizione("Lavoro di gruppo");
        evento.setLuogo("Lab 2");
        evento.setDataOra(LocalDateTime.now().plusDays(1));
        evento.setDurata(2.0);
        evento.setMaxPartecipanti(1);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setStato("aperto");
        evento.setCreatore(creatore);

        if (evento.getPartecipanti() == null) {
            evento.setPartecipanti(new HashSet<>());
        }
        evento.aggiungiPartecipante(creatore);

        evento = eventoRepository.save(evento);
    }

    @And("l'utente è già prenotato a un evento")
    public void utenteGiaPrenotatoAUnEvento() {
        if (utente == null) {
            utente = new Utente();
            utente.setNome("Mario");
            utente.setCognome("Rossi");
            utente.setEmail("mario.giaprenotato@example.com");
            utente.setPassword("Password123!");
            utente = utenteRepository.save(utente);
            session.setAttribute("userId", utente.getId());
        }

        if (evento == null) {
            Utente creatore = new Utente();
            creatore.setNome("Anna");
            creatore.setCognome("Neri");
            creatore.setEmail("anna.creatore@example.com");
            creatore.setPassword("Password123!");
            creatore = utenteRepository.save(creatore);

            evento = new Evento();
            evento.setTitolo("Progetto Java");
            evento.setDescrizione("Sessione collaborativa");
            evento.setLuogo("Lab 1");
            evento.setDataOra(LocalDateTime.now().plusDays(1));
            evento.setDurata(2.0);
            evento.setMaxPartecipanti(5);
            evento.setPartecipazioneCreatoreNecessaria(false);
            evento.setStato("aperto");
            evento.setCreatore(creatore);

            if (evento.getPartecipanti() == null) {
                evento.setPartecipanti(new HashSet<>());
            }
            evento.aggiungiPartecipante(creatore);
        }

        evento.aggiungiPartecipante(utente);
        evento = eventoRepository.save(evento);
    }

    @When("l'utente prenota il posto per quell'evento")
    public void utentePrenotaIlPostoPerQuellEvento() throws Exception {
        result = mockMvc.perform(post(EVENTI_URL + "/" + evento.getId() + "/prenota")
                .session(session));
    }

    @When("l'utente visualizza l'evento")
    public void utenteVisualizzaLEvento() throws Exception {
        result = mockMvc.perform(get(EVENTI_URL)
                .session(session));
    }

    @Then("il sistema registra la prenotazione all'evento")
    public void ilSistemaRegistraLaPrenotazioneAllEvento() throws Exception {
        result.andExpect(status().isOk());

        resultGet = mockMvc.perform(get("/Eventi").session(session));

        resultGet.andExpect(status().isOk());
        resultGet.andExpect(jsonPath("$[0].titolo").value(evento.getTitolo()));
        resultGet.andExpect(jsonPath("$[0].isBooked").value(true));
        resultGet.andExpect(jsonPath("$[0].currentParticipants").value(2));
    }

    @And("il sistema mostra il messaggio di successo della prenotazione")
    public void ilSistemaMostraIlMessaggioDiSuccessoDellaPrenotazione() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Prenotazione completata con successo")));
    }

    @Then("il sistema non mostra il pulsante {string}")
public void ilSistemaNonMostraIlPulsante(String pulsante) throws Exception {
    result.andExpect(status().isOk());

    if ("prenota".equalsIgnoreCase(pulsante)) {
        result.andExpect(content().string(Matchers.containsString(evento.getTitolo())));
    }
}

    @And("il sistema segnala che l'evento è al completo")
    public void ilSistemaSegnalaCheLEventoEAlCompleto() throws Exception {
        String titoloEvento = evento.getTitolo();

        result.andExpect(jsonPath("$[?(@.titolo=='" + titoloEvento + "')][0].maxPartecipanti")
                .value(evento.getMaxPartecipanti()));

        result.andExpect(jsonPath("$[?(@.titolo=='" + titoloEvento + "')][0].currentParticipants")
                .value(evento.getMaxPartecipanti()));

        result.andExpect(jsonPath("$[?(@.titolo=='" + titoloEvento + "')][0].isBooked")
                .value(false));
    }

    @And("il sistema mostra il pulsante {string}")
public void ilSistemaMostraIlPulsante(String pulsante) throws Exception {
    result.andExpect(status().isOk());

    if ("annulla prenotazione".equalsIgnoreCase(pulsante)) {
        result.andExpect(jsonPath("$[0].titolo").value(evento.getTitolo()));
        result.andExpect(jsonPath("$[0].isBooked").value(true));
    }
}
}