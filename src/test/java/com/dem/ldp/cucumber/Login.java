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

public class Login {

    @Autowired
    private MockMvc mockMvc;

    private String email;
    private String password;
    private ResultActions result;

    @Given("le credenziali fornite corrispondono a un utente registrato")
    public void credenzialiValideUtenteRegistrato() throws Exception {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        password = "Password123!";

        String registrazioneBody = String.format("""
            {
              "nome": "Mario",
              "cognome": "Rossi",
              "email": "%s",
              "password": "%s"
            }
            """, email, password);

        mockMvc.perform(post("/Utenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrazioneBody))
                .andExpect(status().isOk());
    }

    @Given("l'identificativo o la password non sono corretti")
    public void credenzialiNonCorrette() throws Exception {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        String passwordCorretta = "Password123!";

        String registrazioneBody = String.format("""
            {
              "nome": "Mario",
              "cognome": "Rossi",
              "email": "%s",
              "password": "%s"
            }
            """, email, passwordCorretta);

        mockMvc.perform(post("/Utenti")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrazioneBody))
                .andExpect(status().isOk());

        password = "PasswordErrata";
    }

    @Given("non vengono fornite credenziali")
    public void credenzialiNonFornite() {
        email = null;
        password = null;
    }

    @When("l'utente prova ad accedere")
    public void utenteProvaAdAccedere() throws Exception {
        String loginBody = String.format("""
            {
              "email": %s,
              "password": %s
            }
            """,
            email == null ? "null" : "\"" + email + "\"",
            password == null ? "null" : "\"" + password + "\""
        );

        result = mockMvc.perform(post("/Utenti/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginBody));
    }

    @Then("il sistema riconosce l'identità dell'utente")
    public void sistemaRiconosceIdentitaUtente() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("permette il passaggio all'area riservata")
    public void permettePassaggioAreaRiservata() throws Exception {
        result.andExpect(content().string(Matchers.not(Matchers.isEmptyOrNullString())));
    }

    @Then("il sistema respinge la richiesta")
    public void sistemaRespingeRichiesta() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("non permette l'accesso all'area riservata")
    public void nonPermetteAccessoAreaRiservata() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Password errata")));
    }

    @Then("il sistema non procede alla verifica")
    public void sistemaNonProcedeAllaVerifica() throws Exception {
        result.andExpect(status().is4xxClientError());
    }

    @And("nega l'autorizzazione all'utente")
    public void negaAutorizzazioneUtente() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Email obbligatoria")));
    }
}