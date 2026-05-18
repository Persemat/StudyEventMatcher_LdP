package com.example.backend.cucumber;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.time.LocalDateTime;
import java.util.*;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.not;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class SearchByDistance {

    @Autowired
    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    /*
     * Cambia questi endpoint se nel tuo backend hanno nomi diversi.
     */
    private static final String CREATE_EVENT_ENDPOINT = "/event";
    private static final String SEARCH_EVENTS_ENDPOINT = "/event/search/distance";

    private String email;
    private String password;
    private String token;

    private String cittaRicerca;
    private int raggioKm;

    private ResultActions result;

    private final List<String> eventiCreati = new ArrayList<>();

    private final Map<String, double[]> coordinateCitta = Map.of(
            "Milano", new double[] { 45.4642, 9.1900 },
            "Monza", new double[] { 45.5845, 9.2744 },
            "Bergamo", new double[] { 45.6983, 9.6773 },
            "Torino", new double[] { 45.0703, 7.6869 });

    @Given("l'utente è autenticato")
    public void utenteAutenticato() throws Exception {
        email = "user_" + UUID.randomUUID() + "@example.com";
        password = "Password123!";

        String bodyRegistration = String.format("""
                {
                  "name": "Mario",
                  "lastname": "Rossi",
                  "email": "%s",
                  "password": "%s",
                  "favouriteCity": "Milano",
                  "macroTags": ["Informatica"],
                  "microTags": []
                }
                """, email, password);

        mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyRegistration))
                .andExpect(status().isOk());

        String loginBody = String.format("""
                {
                  "email": "%s",
                  "password": "%s"
                }
                """, email, password);

        String loginResponse = mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        token = estraiToken(loginResponse);
    }

    @And("l'utente ha impostato {string} come città nella barra di ricerca")
    public void utenteHaImpostatoCittaNellaBarraDiRicerca(String citta) {
        this.cittaRicerca = citta;
    }

    @And("esiste un evento {string} a {word}, distante {int} km da Milano")
    public void esisteUnEventoADistanzaDaMilano(String titoloEvento, String cittaEvento, int distanzaKm)
            throws Exception {
        double[] coordinate = coordinateCitta.get(cittaEvento);

        if (coordinate == null) {
            throw new IllegalArgumentException("Coordinate non definite per la città: " + cittaEvento);
        }

        String bodyEvento = String.format(Locale.US, """
                {
                  "title": "%s",
                  "description": "Evento di test creato da Cucumber",
                  "city": "%s",
                  "latitude": %.6f,
                  "longitude": %.6f,
                  "date": "%s",
                  "macroTags": ["Informatica"],
                  "microTags": []
                }
                """,
                titoloEvento,
                cittaEvento,
                coordinate[0],
                coordinate[1],
                LocalDateTime.now().plusDays(10));

        MockHttpServletRequestBuilder request = post(CREATE_EVENT_ENDPOINT)
                .contentType(MediaType.APPLICATION_JSON)
                .content(bodyEvento);

        aggiungiAutorizzazione(request);

        mockMvc.perform(request)
                .andExpect(status().is2xxSuccessful());

        eventiCreati.add(titoloEvento);
    }

    @When("l'utente seleziona un raggio di distanza di {int} km")
    public void utenteSelezionaRaggioDiDistanza(int raggioKm) throws Exception {
        this.raggioKm = raggioKm;
        eseguiRicerca();
    }

    @And("l'utente ha già effettuato una ricerca con raggio di {int} km")
    public void utenteHaGiaEffettuatoUnaRicercaConRaggio(int raggioKm) throws Exception {
        this.raggioKm = raggioKm;
        eseguiRicerca();
    }

    @And("il sistema mostra {string} e {string}")
    public void ilSistemaMostraDueEventi(String primoEvento, String secondoEvento) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString(primoEvento)))
                .andExpect(content().string(containsString(secondoEvento)));
    }

    @When("l'utente modifica il raggio di distanza a {int} km")
    public void utenteModificaIlRaggioDiDistanza(int nuovoRaggioKm) throws Exception {
        this.raggioKm = nuovoRaggioKm;
        eseguiRicerca();
    }

    @Then("il sistema mostra l'evento {string}")
    public void ilSistemaMostraEvento(String titoloEvento) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString(titoloEvento)));
    }

    @And("il sistema mostra anche l'evento {string}")
    public void ilSistemaMostraAncheEvento(String titoloEvento) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString(titoloEvento)));
    }

    @And("il sistema non mostra l'evento {string}")
    public void ilSistemaNonMostraEvento(String titoloEvento) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(not(containsString(titoloEvento))));
    }

    @Then("il sistema ricalcola gli eventi in base al nuovo raggio")
    public void ilSistemaRicalcolaGliEventiInBaseAlNuovoRaggio() throws Exception {
        result.andExpect(status().isOk());
    }

    @Then("il sistema non mostra nessun evento")
    public void ilSistemaNonMostraNessunEvento() throws Exception {
        result.andExpect(status().isOk());

        for (String evento : eventiCreati) {
            result.andExpect(content().string(not(containsString(evento))));
        }
    }

    @And("il sistema mostra il messaggio {string} nella pagina")
    public void ilSistemaMostraIlMessaggio(String messaggio) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString(messaggio)));
    }

    private void eseguiRicerca() throws Exception {
        MockHttpServletRequestBuilder request = get(SEARCH_EVENTS_ENDPOINT)
                .param("city", cittaRicerca)
                .param("radiusKm", String.valueOf(raggioKm))
                .contentType(MediaType.APPLICATION_JSON);

        aggiungiAutorizzazione(request);

        result = mockMvc.perform(request);
    }

    private void aggiungiAutorizzazione(MockHttpServletRequestBuilder request) {
        if (token != null && !token.isBlank()) {
            request.header("Authorization", "Bearer " + token);
        }
    }

    private String estraiToken(String loginResponse) {
        try {
            JsonNode root = objectMapper.readTree(loginResponse);

            if (root.has("token")) {
                return root.get("token").asText();
            }

            if (root.has("accessToken")) {
                return root.get("accessToken").asText();
            }

            if (root.has("jwt")) {
                return root.get("jwt").asText();
            }

            return loginResponse;

        } catch (Exception e) {
            return loginResponse;
        }
    }
}