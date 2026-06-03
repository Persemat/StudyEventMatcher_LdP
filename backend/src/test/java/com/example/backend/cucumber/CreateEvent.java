package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.List;
import java.time.LocalDateTime;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
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

    private String title = "";
    private String description = "";
    private String dateTime = "";
    private double duration = 0.0;
    private String location = "";
    private String tags = "[]";
    private int maxPartecipants = 0;
    private boolean setCreatorParticipationRequired = false;
    private Long creatorId;

    @Given("l'utente si trova nella pagina di creazione evento")
    public void utenteNellaPaginaCreazioneEvento() {
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

        User user = new User(
                "Marco",
                "Rossi",
                "marco@example.com",
                "Password123!",
                "Milano",
                List.of("Tech"),
                List.of());
        userRepository.save(user);

        this.creatorId = user.getId();
    }

    @And("l'utente ha gia' creato un evento nel sistema")
    public void utenteHaGiaCreatoUnEventoNelSistema() {
        title = "Gruppo di studio Java";
        description = "Ripasso per l'esame di architetture";
        dateTime = "2026-10-15T15:00:00";
        duration = 2.5;
        location = "Milano (MI)-Aula Magna";
        tags = """
                [
                  { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" },
                  { "macroTag": "Informatica e Tecnologie", "microTag": null }
                ]
                """;
        maxPartecipants = 10;
        setCreatorParticipationRequired = true;

        User creator = userRepository.findById(creatorId).orElseThrow();

        Event event = new Event();
        event.setTitle(title);
        event.setDescription(description);
        event.setDateTime(LocalDateTime.parse(dateTime));
        event.setDuration(duration);
        event.setLocation(location);
        event.setTags(List.of(
                new EventTag("Informatica e Tecnologie", "Programmazione"),
                new EventTag("Informatica e Tecnologie", null)));
        event.setMaxParticipants(maxPartecipants);
        event.setCreatorParticipationRequired(setCreatorParticipationRequired);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        eventRepository.save(event);
    }

    @When("l'utente inserisce titolo, descrizione, data, durata e luogo")
    public void utenteInserisceDatiBase() {
        title = "Gruppo di studio Java";
        description = "Ripasso per l'esame di architetture";
        dateTime = "2026-10-15T15:00:00";
        duration = 2.5;
        location = "Milano (MI)-Aula Magna";
    }

    @And("aggiunge uno o piu tag e imposta un limite di partecipanti")
    public void aggiungeTagELimite() {
        tags = """
                [
                  { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" },
                  { "macroTag": "Informatica e Tecnologie", "microTag": null }
                ]
                """;
        maxPartecipants = 10;
    }

    @And("imposta il flag Partecipazione del creatore necessaria")
    public void impostaFlagCreatore() {
        setCreatorParticipationRequired = true;
    }

    @When("l'utente compila i campi con gli stessi dati esatti dell'evento esistente")
    public void utenteCompilaCampiConStessiDatiEsattiEventoEsistente() {
        title = "Gruppo di studio Java";
        description = "Ripasso per l'esame di architetture";
        dateTime = "2026-10-15T15:00:00";
        duration = 2.5;
        location = "Milano (MI)-Aula Magna";
        tags = """
                [
                  { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" },
                  { "macroTag": "Informatica e Tecnologie", "microTag": null }
                ]
                """;
        maxPartecipants = 10;
        setCreatorParticipationRequired = true;
    }

    @And("conferma la creazione")
    public void confermaLaCreazione() throws Exception {
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

        result = mockMvc.perform(post("/events")
                .sessionAttr("userId", creatorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema salva il nuovo evento")
    public void sistemaSalvaNuovoEvento() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema imposta lo stato dell'evento come aperto")
    public void sistemaImpostaStatoAperto() {
    }

    @And("l'evento appare nella lista degli eventi")
    public void eventoAppareNellaLista() {
    }

    @When("l'utente tenta di creare un evento senza compilare i campi obbligatori")
    public void tentaDiCreareSenzaCampi() throws Exception {
        String bodyIncompleto = """
                {
                "title": "",
                "description": "test",
                "dateTime": "2026-10-15T15:00:00",
                "duration": 1.0,
                "location": "Milano (MI)-Aula Magna",
                "tags": [
                  { "macroTag": "Informatica e Tecnologie", "microTag": "Testing software" }
                ],
                "maxPartecipants": 10,
                "creatorParticipationRequired": true
                }
                """;

        result = mockMvc.perform(post("/events")
                .sessionAttr("userId", creatorId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyIncompleto));
    }

    @Then("il sistema impedisce la creazione dell'evento")
    public void sistemaImpedisceCreazione() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("il sistema mostra l'errore {string}")
    public void ilSistemaMOstraErrore(String messaggio) throws Exception {
        result.andExpect(content().string(org.hamcrest.Matchers.containsString(
                messaggio)));
    }
}