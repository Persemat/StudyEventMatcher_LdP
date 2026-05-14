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

public class ViewEvents {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private Long userId;

    @Given("l'utente si trova nella pagina degli eventi")
    public void utenteSiTrovaNellaPaginaDegliEventi() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Mario");
        user.setLastname("Rossi");
        user.setEmail("mario@example.com");
        user.setPassword("Password123!");
        userRepository.save(user);

        this.userId = user.getId();
    }

    @And("sono presenti eventi nel sistema")
    public void sonoPresentiEventiNelSistema() {
        User creator = userRepository.findById(userId).orElseThrow();

        Event event1 = new Event();
        event1.setTitle("Ripasso Analisi 1");
        event1.setDescription("Prepariamoci per lo scritto di lunedì");
        event1.setDateTime(LocalDateTime.of(2026, 4, 10, 10, 30));
        event1.setDuration(2.0);
        event1.setLocation("Milano (MI)-Aula 4");
        event1.setTags(List.of(new com.example.backend.model.EventTag("Scienze Matematiche Fisiche e Naturali", "Matematica"), new com.example.backend.model.EventTag("Scienze Matematiche Fisiche e Naturali", "Analisi matematica")));
        event1.setMaxParticipants(10);
        event1.setCreatorParticipationRequired(true);
        event1.setStatus("aperto");
        event1.setCreator(creator);
        event1.addParticipant(creator);

        Event event2 = new Event();
        event2.setTitle("Workshop React");
        event2.setDescription("Creazione componenti modulari");
        event2.setDateTime(LocalDateTime.of(2026, 4, 15, 9, 0));
        event2.setDuration(1.5);
        event2.setLocation("Milano (MI)-Online");
        event2.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Sviluppo frontend")));
        event2.setMaxParticipants(20);
        event2.setCreatorParticipationRequired(false);
        event2.setStatus("aperto");
        event2.setCreator(creator);
        event2.addParticipant(creator);

        eventRepository.save(event1);
        eventRepository.save(event2);
    }

    @And("non sono presenti eventi nel sistema")
    public void nonSonoPresentiEventiNelSistema() {
        eventRepository.deleteAll();
    }

    @When("accede alla pagina degli eventi")
    public void accedeAllaPaginaDegliEventi() throws Exception {
        result = mockMvc.perform(get("/events")
                .sessionAttr("userId", userId));
    }

    @Then("il sistema mostra la lista degli eventi disponibili")
    public void ilSistemaMostraLaListaDegliEventiDisponibili() throws Exception {
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString("Ripasso Analisi 1")))
              .andExpect(content().string(containsString("Workshop React")));
    }

    @And("gli eventi sono visualizzati senza filtri applicati")
    public void gliEventiSonoVisualizzatiSenzaFiltriApplicati() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("per ogni evento vengono mostrati titolo, data e luogo")
    public void perOgniEventoVengonoMostratiTitoloDataELuogo() throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("\"title\""));
        Assertions.assertTrue(response.contains("\"dateTime\""));
        Assertions.assertTrue(response.contains("\"location\""));
    }

    @Then("il sistema non mostra eventi")
    public void ilSistemaNonMostraEventi() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema restituisce una lista vuota")
    public void ilSistemaRestituisceUnaListaVuota() throws Exception {
        result.andExpect(status().isOk())
              .andExpect(content().json("[]"));
    }
}
