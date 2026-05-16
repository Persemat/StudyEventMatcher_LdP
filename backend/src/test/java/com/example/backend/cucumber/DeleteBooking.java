package com.example.backend.cucumber;

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

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DeleteBooking {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private MockHttpSession session = new MockHttpSession();
    private Long currentEventId;
    private int initialPartecipants;

    @Given("l'utente è autenticato per la cancellazione")
    public void utenteAutenticatoPerCancellazione() {
        userRepository.deleteAll();
        User user = new User("Mario", "Rossi", "mario@test.com", "Pass123!",
                "Milano", List.of("Tech"), List.of());
        user = userRepository.save(user);

        session.setAttribute("userId", user.getId());
    }

    @Given("l'utente non è autenticato per la cancellazione")
    public void utenteNonAutenticato() {
        session.clearAttributes();
    }

    @And("l'utente ha una prenotazione per un evento")
    public void utenteHaUnaPrenotazione() {
        eventRepository.deleteAll();
        User user = userRepository.findById((Long) session.getAttribute("userId")).get();

        Event event = createBaseEvent();
        event.addParticipant(user); // Prenotazione!
        event = eventRepository.save(event);

        this.currentEventId = event.getId();
    }

    @And("l'utente non è prenotato a quell'evento")
    public void utenteNonPrenotato() {
        eventRepository.deleteAll();
        Event event = createBaseEvent();
        event = eventRepository.save(event); // Viene salvato senza aggiungere l'utente

        this.currentEventId = event.getId();
    }

    @Transactional
    @And("l'utente ha una prenotazione per un evento con posti limitati")
    public void utenteHaPrenotazionePostiLimitati() {
        utenteHaUnaPrenotazione(); // Sfruttiamo il metodo sopra per creare l'iscrizione

        // Segnamoci quanti partecipanti ci sono all'inizio (dovrebbe essere 1)
        Event event = eventRepository.findById(currentEventId).get();
        this.initialPartecipants = event.getParticipants().size();
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
        result = mockMvc.perform(delete("/events/" + currentEventId + "/book").session(session));
    }

    @When("l'utente prova a cancellare la prenotazione")
    public void provaACancellareUnaPrenotazione() throws Exception {
        // Se l'evento non è stato creato prima (come nello scenario 3), usiamo un ID
        // fittizio "1" per far arrivare la richiesta al controller
        Long idToDelete = (currentEventId != null) ? currentEventId : 1L;

        result = mockMvc.perform(delete("/events/" + idToDelete + "/book").session(session));
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
        Event updatedEvent = eventRepository.findById(currentEventId).get();
        int currentPartecipants = updatedEvent.getParticipants().size();

        org.junit.jupiter.api.Assertions.assertTrue(currentPartecipants < initialPartecipants);
    }

    private Event createBaseEvent() {
        User creator = new User("Mario", "Rossi", "creatore" + Math.random() + "@test.com", "Pass123!",
                "Roma", List.of("Social"), List.of());
        creator = userRepository.save(creator);

        Event event = new Event();
        event.setTitle("Evento di Test");
        event.setDescription("Test Cucumber");
        event.setLocation("Milano (MI)-Aula Virtuale");
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setDuration(2.0);
        event.setMaxParticipants(10);
        event.setStatus("aperto");
        event.setCreatorParticipationRequired(false);
        event.setCreator(creator);

        if (event.getParticipants() == null) {
            event.setParticipants(new HashSet<>());
        }
        event.addParticipant(creator);

        return event;
    }
}