package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.repository.UserRepository;
import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ViewEventDetails {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private Long userId;
    private Long eventId;

    @Given("l'utente e' autenticato per i dettagli")
    public void lUtenteEAutenticatoPerI_dettagli() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Mario");
        user.setLastname("Rossi");
        user.setEmail("mario.dettagli@example.com");
        user.setPassword("Password123!");
        // Required profile fields must be populated to satisfy the User entity constraints.
        user.setFavouriteCity("Milano");
        user.setMacroTags(List.of("Tech"));
        user.setMicroTags(List.of());
        userRepository.save(user);

        this.userId = user.getId();
    }

    @Given("l'utente non e' autenticato per i dettagli")
    public void lUtenteNonEAutenticatoPerI_dettagli() {
        this.userId = null;
    }

    @And("esiste un evento disponibile nel sistema")
    public void esisteUnEventoDisponibileNelSistema() {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Evento Dettagli Base");
        event.setDescription("Descrizione per test dettagli");
        event.setDateTime(LocalDateTime.of(2026, 6, 10, 15, 0));
        event.setDuration(2.0);
        event.setLocation("Aula Magna");
        event.setTags(List.of("Generale"));
        event.setMaxParticipants(50);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        event = eventRepository.save(event);
        this.eventId = event.getId();
    }

    @And("l'utente e' prenotato all'evento")
    public void lUtenteEPrenotatoAllEvento() {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Evento Prenotato");
        event.setDescription("Descrizione prenotazione");
        event.setDateTime(LocalDateTime.of(2026, 6, 15, 10, 0));
        event.setDuration(1.5);
        event.setLocation("Laboratorio");
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator); 

        event = eventRepository.save(event);
        this.eventId = event.getId();
    }

    @And("esiste un evento con un limite massimo di partecipanti")
    public void esisteUnEventoConUnLimiteMassimoDiPartecipanti() {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Evento a numero chiuso");
        event.setDescription("Descrizione limite");
        event.setDateTime(LocalDateTime.of(2026, 7, 20, 9, 0));
        event.setDuration(3.0);
        event.setLocation("Online");
        event.setMaxParticipants(15);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);

        event = eventRepository.save(event);
        this.eventId = event.getId();
    }


    @When("l'utente seleziona un evento dalla lista")
    public void lUtenteSelezionaUnEventoDallaLista() throws Exception {
        result = mockMvc.perform(get("/events/" + eventId)
                .sessionAttr("userId", userId));
    }

    @When("l'utente prova a visualizzare i dettagli di un evento")
    public void lUtenteProvaAVisualizzareIDettagliDiUnEvento() throws Exception {
        result = mockMvc.perform(get("/events/1")); // Chiamata senza caricare la sessione
    }

    @Then("il sistema mostra i dettagli dell'evento")
    public void ilSistemaMostraIDettagliDellEvento() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("vengono mostrati titolo, descrizione, data, ora, luogo, tag, limite persone e stato evento")
    public void vengonoMostratiTitoloDescrizioneDataOraLuogoTagLimitePersoneEStatoEvento() throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(response.contains("\"title\""));
        Assertions.assertTrue(response.contains("\"description\""));
        Assertions.assertTrue(response.contains("\"dateTime\""));
        Assertions.assertTrue(response.contains("\"location\""));
        Assertions.assertTrue(response.contains("\"tags\""));
        Assertions.assertTrue(response.contains("\"maxParticipants\""));
        Assertions.assertTrue(response.contains("\"status\""));
    }

    @Then("il sistema mostra che l'utente e' gia' prenotato")
    public void ilSistemaMostraCheLUtenteEGiaPrenotato() throws Exception {
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(response.contains("\"isBooked\":true"));
    }

    @Then("il sistema non permette l'accesso")
    public void ilSisstemaNonPermetteLAccesso() throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    @And("il sistema richiede l'autenticazione per i dettagli")
    public void ilSistemaRichiedeLAutenticazione() throws Exception {
        result.andExpect(content().string(containsString("Sessione invalida o scaduta")));
    }

    @Then("il sistema mostra il numero di posti disponibili")
    public void ilSistemaMostraIlNumeroDiPostiDisponibili() throws Exception {
        result.andExpect(status().isOk());
        String response = result.andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(response.contains("\"maxParticipants\""));
        Assertions.assertTrue(response.contains("\"currentParticipants\""));
    }

    @And("il numero di posti riflette correttamente le prenotazioni effettuate")
    public void ilNumeroDiPostiRifletteCorrettamenteLePrenotazioniEffettuate() throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();
        Assertions.assertTrue(response.contains("\"currentParticipants\":1"));
    }
}
