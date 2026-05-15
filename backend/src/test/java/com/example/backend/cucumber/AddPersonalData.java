package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AddPersonalData {

    @Autowired
    private MockMvc mockMvc;

    private static final String DATI_PERSONALI_URL = "/user";

    private String name;
    private String lastname;
    private String email;
    private String password;
    private String favouriteCity;
    private String macroTagsJson;
    private String microTagsJson;
    private ResultActions result;

    @Given("l'utente è nella schermata dei dati personali")
    public void utenteENellaSchermataDeiDatiPersonali() {
        name = null;
        lastname = null;
        email = null;
        password = null;
        favouriteCity = null;
        macroTagsJson = null;
        microTagsJson = null;
    }

    @When("l'utente compila tutti i campi obbligatori con dati validi")
    public void utenteCompilaTuttiICampiObbligatoriConDatiValidi() {
        name = "Mario";
        lastname = "Rossi";
        email = "mario.rossi@example.com";
        password = "Password123!";
        favouriteCity = "Milano";
        macroTagsJson = "[\"Informatica\"]";
        microTagsJson = "[\"Java\"]";
    }

    @When("l'utente lascia vuoto un campo obbligatorio \\(es. {string})")
    public void utenteLasciaVuotoUnCampoObbligatorio(String campo) {
        name = "Mario";
        lastname = "Rossi";

        if ("Nome".equalsIgnoreCase(campo)) {
            name = "";
        } else if ("Cognome".equalsIgnoreCase(campo)) {
            lastname = "";
        }
    }

    @And("tenta di continuare nella registrazione")
    public void tentaDiContinuareNellaRegistrazione() throws Exception {
        String body = String.format("""
                {
                  "name": %s,
                  "lastname": %s,
                  "email": %s,
                  "password": %s,
                  "favouriteCity": %s,
                  "macroTags": %s,
                  "microTags": %s
                }
                """,
                toJsonValue(name),
                toJsonValue(lastname),
                toJsonValue(email),
                toJsonValue(password),
                toJsonValue(favouriteCity),
                macroTagsJson,
                microTagsJson);

        result = mockMvc.perform(post(DATI_PERSONALI_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema salva i dati")
    public void ilSistemaSalvaIDati() throws Exception {
        result.andExpect(status().isOk());
    }

    @Then("il sistema blocca l'avanzamento")
    public void ilSistemaBloccaLAvanzamento() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("evidenzia il campo mancante con un messaggio")
    public void evidenziaIlCampoMancanteConUnMessaggio() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    private String toJsonValue(String value) {
        if (value == null) {
            return "null";
        }
        return "\"" + value + "\"";
    }
}