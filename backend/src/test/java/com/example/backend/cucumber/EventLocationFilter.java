package com.example.backend.cucumber;

import io.cucumber.datatable.DataTable;
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
import java.util.ArrayList;
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventLocationFilter {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private User user;

    // ==========================================
    // BACKGROUND
    // ==========================================

    @Given("l'utente è autenticato per la ricerca per luogo")
    public void l_utente_e_autenticato_per_la_ricerca_per_luogo() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        user = new User(
                "Luigi",
                "Verdi",
                "luigi.luogo@example.com",
                "Pass123!",
                "Milano",
                List.of("Studio"),
                new ArrayList<>());
        userRepository.save(user);
    }

    @And("esistono i seguenti eventi registrati nel sistema:")
    public void esistono_i_seguenti_eventi_registrati_nel_sistema(DataTable dataTable) {
        List<Map<String, String>> row = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> riga : row) {
            Event event = new Event();
            event.setTitle(riga.get("Titolo"));
            event.setLocation(riga.get("Luogo"));

            // Dati obbligatori fittizi per far felice il database
            event.setDescription("Descrizione automatica");
            event.setDateTime(LocalDateTime.now().plusDays(10)); // Sempre nel futuro
            event.setDuration(2.0);
            event.setStatus("open");
            event.setMaxParticipants(10);
            event.setCreatorParticipationRequired(false);
            event.setCreator(user);

            eventRepository.save(event);
        }
    }

    // ==========================================
    // WHEN
    // ==========================================

    @When("cerco gli eventi specificando il luogo {string}")
    public void cerco_gli_eventi_specificando_il_luogo(String location) throws Exception {
        result = mockMvc.perform(
                get("/events/search?location=" + location)
                        .sessionAttr("userId", user.getId()));
    }

    @When("cerco gli eventi lasciando il filtro luogo vuoto")
    public void cerco_gli_eventi_lasciando_il_filtro_luogo_vuoto() throws Exception {
        // Parametro vuoto come farebbe il frontend
        result = mockMvc.perform(
                get("/events/search?location=")
                        .sessionAttr("userId", user.getId()));
    }

    // ==========================================
    // THEN
    // ==========================================

    @Then("il sistema restituisce {int} eventi")
    public void il_sistema_restituisce_eventi(int expectedNumber) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(expectedNumber)));
    }

    @And("tutti gli eventi restituiti si svolgono a {string}")
    public void tutti_gli_eventi_restituiti_si_svolgono_a(String expectedLocation) throws Exception {
        // Verifica che nel JSON testuale ritorni la stringa del luogo
        result.andExpect(content().string(Matchers.containsString(expectedLocation)));
    }

    @Then("il sistema restituisce una lista vuota indicando che non ci sono eventi")
    public void il_sistema_restituisce_una_lista_vuota() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0))); // Array vuoto []
    }

    @Then("il sistema ignora il filtro e restituisce tutti i {int} eventi disponibili")
    public void il_sistema_ignora_il_filtro_e_restituisce_tutti_gli_eventi(int expectedNumber) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(expectedNumber)));
    }
}