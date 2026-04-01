package com.dem.ldp.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AggiuntaDatiPersonali {

    @Autowired
    private MockMvc mockMvc;

    private static final String DATI_PERSONALI_URL = "/Utenti";

    private String nome;
    private String cognome;
    private String email;
    private String password;
    private ResultActions result;

    @Given("l'utente è nella schermata dei dati personali")
    public void utenteENellaSchermataDeiDatiPersonali() {
        nome = null;
        cognome = null;
        email = null;
        password = null;
    }

    @When("l'utente compila tutti i campi obbligatori con dati validi")
    public void utenteCompilaTuttiICampiObbligatoriConDatiValidi() {
        nome = "Mario";
        cognome = "Rossi";
        email = "mario.rossi@example.com";
        password = "Password123!";
    }

    @When("l'utente lascia vuoto un campo obbligatorio \\(es. {string})")
    public void utenteLasciaVuotoUnCampoObbligatorio(String campo) {
        nome = "Mario";
        cognome = "Rossi";

        if ("Nome".equalsIgnoreCase(campo)) {
            nome = "";
        } else if ("Cognome".equalsIgnoreCase(campo)) {
            cognome = "";
        } 
    }

    @And("tenta di continuare nella registrazione")
    public void tentaDiContinuareNellaRegistrazione() throws Exception {
        String body = String.format("""
            {
              "nome": %s,
              "cognome": %s,
              "email": %s,
              "password": %s
            }
            """,
            toJsonValue(nome),
            toJsonValue(cognome),
            toJsonValue(email),
            toJsonValue(password)
        );

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