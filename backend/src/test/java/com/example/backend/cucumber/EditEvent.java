package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EditEvent {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private MockHttpSession session;
    private User user;
    private User otherUser;
    private Event event;
    private ResultActions result;
    private String requestBody;

    @Given("l'utente è autenticato per la modifica")
    public void utenteAutenticatoPerModifica() {
        userRepository.deleteAll();
        eventRepository.deleteAll();

        user = new User("Mario", "Rossi", "mario@example.com", "Password123",
                "Milano", List.of("Tech"), new ArrayList<>());
        user = userRepository.save(user);

        session = new MockHttpSession();
        session.setAttribute("userId", user.getId());
    }

    @And("esiste un evento {string} creato dall'utente")
    public void esisteEventoCreatoDallUtente(String title) {
        event = new Event();
        event.setTitle(title);
        event.setDescription("Descrizione iniziale");
        event.setLocation("Milano (MI)-Aula 1");
        event.setDateTime(LocalDateTime.now().plusDays(2));
        event.setDuration(2);
        event.setMaxParticipants(10);
        event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
        event.setCreatorParticipationRequired(true);
        event.setStatus("aperto");
        event.setCreator(user);
        event.addParticipant(user);

        event = eventRepository.save(event);
    }

    @And("l'evento inizia tra più di un giorno")
    public void eventoIniziaTraPiuDiUnGiorno() {
        event.setDateTime(LocalDateTime.now().plusDays(2));
        eventRepository.save(event);
    }

    @When("l'utente modifica l'orario di inizio dell'evento")
    public void modificaOrarioEvento() {
        requestBody = """
                {
                  "title": "Progetto Java",
                  "description": "Descrizione iniziale",
                  "dateTime": "%s",
                  "duration": 2.0,
                  "location": "Milano (MI)-Aula 1",
                  "tags": [
                    { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
                  ],
                  "maxPartecipants": 10,
                  "creatorParticipationRequired": true
                }
                """.formatted(LocalDateTime.now().plusDays(3).withNano(0).toString());
    }

    @And("salva le modifiche")
    public void salvaLeModifiche() throws Exception {
        result = mockMvc.perform(
                put("/events/" + event.getId())
                        .session(session)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody));
    }

    @Then("il sistema aggiorna l'evento correttamente")
    public void sistemaAggiornaEvento() throws Exception {
        result.andExpect(status().isOk());

        Event updatedEvent = eventRepository.findById(event.getId()).orElseThrow();
        assertNotEquals(event.getDateTime(), updatedEvent.getDateTime());
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
                  "title": "",
                  "description": "Descrizione iniziale",
                  "dateTime": "%s",
                  "duration": 2.0,
                  "location": "Milano (MI)-Aula 1",
                  "tags": [
                    { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
                  ],
                  "maxPartecipants": 10,
                  "creatorParticipationRequired": true
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
    public void esisteEventoCreatoDaAltroUtente(String title) {
        otherUser = new User("Luca", "Verdi", "luca@example.com", "Password123",
                "Roma", List.of("Social"), new ArrayList<>());
        otherUser = userRepository.save(otherUser);

        event = new Event();
        event.setTitle(title);
        event.setDescription("Descrizione");
        event.setLocation("Milano (MI)-Aula 2");
        event.setDateTime(LocalDateTime.now().plusDays(2));
        event.setDuration(2.0);
        event.setMaxParticipants(8);
        event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Programmazione")));
        event.setCreatorParticipationRequired(true);
        event.setStatus("open");
        event.setCreator(otherUser);
        event.addParticipant(otherUser);

        event = eventRepository.save(event);
    }

    @When("l'utente visualizza gli eventi disponibili")
    public void visualizzaEventiDisponibili() throws Exception {
        result = mockMvc.perform(
                get("/events").session(session));
    }

    @Then("il pulsante modifica evento non è visibile")
    public void pulsanteModificaNonVisibile() throws Exception {
        String body = result.andReturn().getResponse().getContentAsString();
        assertTrue(body.contains("\"isCreatedByMe\":false"));
    }
}
