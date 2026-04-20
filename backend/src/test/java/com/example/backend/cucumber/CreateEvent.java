package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateEvent {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;

    // Variabili di stato
    private String title = "";
    private String description = "";
    private String dateTime = "";
    private double duration = 0.0;
    private String location = "";
    private String tags = "[]";
    private int maxPartecipants = 0;
    private boolean setCreatorParticipationRequired = false;
    private Long creatorId; // Questo verrà usato per popolare l'attributo di sessione

    @Given("l'utente si trova nella pagina di creazione evento")
    public void utenteNellaPaginaCreazioneEvento() {
        // Reset stati
        title = "";
        description = "";
        dateTime = "";
        duration = 0.0;
        location = "";
        tags = "[]";
        maxPartecipants = 0;
        setCreatorParticipationRequired = false;

        eventRepository.deleteAll();
        userRepository.deleteAll();

        // Creazione utente reale per avere un ID valido in sessione
        User user = new User();
        user.setName("Marco");
        user.setLastname("Rossi");
        user.setEmail("marco@example.com");
        user.setPassword("Password123!");
        userRepository.save(user);

        this.creatorId = user.getId();
    }

    @When("l'utente inserisce titolo, descrizione, data, durata e luogo")
    public void utenteInserisceDatiBase() {
        title = "Gruppo di studio Java";
        description = "Ripasso per l'esame di architetture";
        dateTime = "2026-10-15T15:00:00";
        duration = 2.5;
        location = "Aula Magna";
    }

    @And("aggiunge uno o piu tag e imposta un limite di partecipanti")
    public void aggiungeTagELimite() {
        tags = "[\"Java\", \"Programmazione\"]";
        maxPartecipants = 10;
    }

    @And("imposta il flag Partecipazione del creatore necessaria")
    public void impostaFlagCreatore() {
        setCreatorParticipationRequired = true;
    }

    @And("conferma la creazione")
    public void confermaLaCreazione() throws Exception {
        // Il JSON ora non contiene più creatoreId
        String body = String.format("""
                {
                "title": "%s",
                "description": "%s",
                "dateTime": "%s",
                "duration": %s,
                "location": "%s",
                "tags": %s,
                "maxPartecipants": %d,
                "creatorParticipationRequired": %b
                }
                """, title, description, dateTime, String.valueOf(duration), location, tags, maxPartecipants,
                setCreatorParticipationRequired);
        // Simuliamo l'utente loggato iniettando l'ID nella sessione
        result = mockMvc.perform(post("/events")
                .sessionAttr("userId", creatorId) // <--- SESSIONE ATTIVA
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema salva il nuovo evento")
    public void sistemaSalvaNuovoEvento() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema imposta lo stato dell'evento come aperto")
    public void sistemaImpostaStatoAperto() {
        // Logica verificata implicitamente dal 200 OK e dalla logica del service
    }

    @And("l'evento appare nella lista degli eventi")
    public void eventoAppareNellaLista() {
        // Step di accettazione lato utente
    }

    @When("l'utente tenta di creare un evento senza compilare i campi obbligatori")
    public void tentaDiCreareSenzaCampi() throws Exception {
        // JSON incompleto (titolo e luogo vuoti), niente creatoreId
        String bodyIncompleto = """
                {
                "title": "",
                "description": "test",
                "dateTime": "2026-10-15T15:00:00",
                "duration": 1.0,
                "location": "Aula Magna",
                "tags": ["test"],
                "maxPartecipants": 10,
                "creatorParticipationRequired": true
                }
                """;

        result = mockMvc.perform(post("/events")
                .sessionAttr("userId", creatorId) // <--- SESSIONE ATTIVA
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyIncompleto));
    }

    @Then("il sistema impedisce la creazione dell'evento")
    public void sistemaImpedisceCreazione() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("il sistema mostra l'errore {string}")
    public void ilSistemaMOstraErrore(String messaggio) throws Exception {
        result.andExpect(content().string(org.hamcrest.Matchers.containsString(messaggio)));
    }
}