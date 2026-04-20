package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class PersonalEvents {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private Long userId;

    @Given("l'utente è autenticato per eventi personali")
    public void utenteEAutenticatoPerEventiPersonali() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Luca");
        user.setLastname("Rossi");
        user.setEmail("luca@example.com");
        user.setPassword("Pass123");
        userRepository.save(user);

        this.userId = user.getId();
    }

    @And("ha eventi personali")
    public void haEventiPersonali() {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event = new Event();
        event.setTitle("Studio Basi di Dati");
        event.setDescription("Ripasso SQL");
        event.setDateTime(LocalDateTime.of(2026, 4, 10, 15, 0));
        event.setDuration(2.0);
        event.setLocation("Aula 2");
        event.setTags(List.of("Informatica"));
        event.setMaxParticipants(5);
        event.setCreatorParticipationRequired(false);
        event.setStatus("aperto");
        event.setCreator(creator);

        eventRepository.save(event);
    }

    @And("non ha eventi personali")
    public void nonHaEventiPersonali() {
        User otherUser = new User();
        otherUser.setName("Marco");
        otherUser.setLastname("Verdi");
        otherUser.setEmail("marco@example.com");
        otherUser.setPassword("Pass123");
        userRepository.save(otherUser);

        Event event = new Event();
        event.setTitle("Evento di un altro utente");
        event.setDescription("Descrizione");
        event.setDateTime(LocalDateTime.of(2026, 4, 12, 10, 0));
        event.setDuration(3.0);
        event.setLocation("Biblioteca");
        event.setTags(List.of("Matematica"));
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setStatus("aperto");
        event.setCreator(otherUser);

        eventRepository.save(event);
    }

    @When("l'utente visualizza la lista degli eventi personali")
    public void utenteVisualizzaLaListaDegliEventiPersonali() throws Exception {
        if (userId != null) {
            result = mockMvc.perform(get("/events").sessionAttr("userId", userId));
        } else {
            result = mockMvc.perform(get("/events"));
        }
    }

    @Then("per ogni evento personale vengono mostrati titolo, data e luogo")
    public void perOgniEventoPersonaleVengonoMostratiTitoloDataELuogo() throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"title\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"dateTime\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"location\""));
    }

    @Then("il sistema mostra la lista degli eventi personali")
    public void ilSistemaMostraLaListaDegliEventiPersonali() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Studio Basi di Dati")))
                .andExpect(content().string(Matchers.containsString("\"isCreatedByMe\":true")));
    }

    @Then("il sistema mostra un messaggio che indica che non ci sono eventi personali")
    public void ilSistemaMostraUnMessaggioCheIndicaCheNonCiSonoEventiPersonali() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("\"isCreatedByMe\":true"))));
    }

    @Then("il sistema impedisce l'accesso")
    public void ilSistemaImpedisceLAccesso() throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    @And("il sistema richiede l'autenticazione")
    public void ilSistemaRichiedeLAutenticazione() throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}