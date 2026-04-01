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

import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class Registrazione {

    @Autowired
    private MockMvc mockMvc;

    private String email;
    private String password;
    private ResultActions result;

    @Given("l'utente non è registrato")
    public void utenteNonRegistrato() {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        password = "Password123!";
    }

    @And("l'utente non è autenticato")
    public void utenteNonAutenticato() {
    }

    @And("inserisce un'email con formato valido")
    public void emailFormatoValido() {
        email = "utente_" + UUID.randomUUID() + "@example.com";
    }

    @And("l'email non è associata ad altri account")
    public void emailNonAssociata() {
    }

    @And("inserisce una password con formato corretto")
    public void passwordFormatoCorretto() {
        password = "Password123!";
    }

    @And("l'email è già associata a un account esistente")
    public void emailGiaPresente() throws Exception {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        password = "Password123!";

        String body = String.format("""
            {
              "nome": "Mario",
              "cognome": "Rossi",
              "email": "%s",
              "password": "%s"
            }
            """, email, password);

        mockMvc.perform(post("/Utenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());
    }

    @And("inserisce un'email con formato non valido")
    public void emailNonValida() {
        email = "email_non_valida";
    }

    @And("inserisce una password con formato non valido")
    public void passwordNonValida() {
        password = "123";
    }

    @When("l'utente prova a registrarsi")
    public void provaARegistrarsi() throws Exception {
        String body = String.format("""
            {
              "nome": "Mario",
              "cognome": "Rossi",
              "email": "%s",
              "password": "%s"
            }
            """, email, password);

        result = mockMvc.perform(post("/Utenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema registra l’utente")
    public void sistemaRegistraUtente() throws Exception {
        result.andExpect(status().isOk());
    }

    @Then("il sistema impedisce la registrazione")
    public void sistemaImpedisceRegistrazione() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("il sistema mostra l'email dell'utente registrato")
    public void ilSistemaMostraLEmailDellUtenteRegistrato() throws Exception {
        result.andExpect(content().string(Matchers.containsString(email)));
    }

    @Then("il sistema mostra {string}")
    public void ilSistemaMostra(String messaggio) throws Exception {
        result.andExpect(content().string(Matchers.containsString(messaggio)));
    }
}