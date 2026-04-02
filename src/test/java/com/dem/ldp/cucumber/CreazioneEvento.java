package com.dem.ldp.cucumber;

import io.cucumber.java.en.Given;
import io.cucumber.java.en.When;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.And;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CreazioneEvento {

    @Autowired
    private TestRestTemplate restTemplate;

    private String jsonRequest;
    private ResponseEntity<String> response;
    
    // VARIABILI CONDIVISE TRA GLI STEP
    private HttpHeaders headers = new HttpHeaders();

    @Given("l'utente si trova nella pagina di creazione evento")
    public void utente_nella_pagina_creazione() {
        // Qui prepariamo gli header, simulando che l'utente sia pronto a inviare dati
        headers.setContentType(MediaType.APPLICATION_JSON);
    }

    @When("l'utente inserisce titolo, descrizione, data e luogo")
    public void utente_inserisce_dati_base() {
        // Iniziamo a costruire il nostro JSON valido
        jsonRequest = """
            {
              "titolo": "Gruppo di Studio",
              "dataOra": "2026-10-15T15:00:00",
              "luogo": "Biblioteca",
              "descrizione": "Studiamo insieme",
              "utenteId": 1,
        """;
    }

    @And("aggiunge uno o piu tag e imposta un limite di partecipanti")
    public void aggiunge_tag_e_limite() {
        jsonRequest += """
              "tag": ["Studio"],
              "limitePartecipanti": 5,
        """;
    }

    @And("imposta il flag Partecipazione del creatore necessaria")
    public void imposta_flag_creatore() {
        jsonRequest += """
              "partecipazioneCreatoreNecessaria": true
            }
        """;
    }

    @And("conferma la creazione")
    public void conferma_la_creazione() {
        // Lanciamo la chiamata al server!
        response = restTemplate.postForEntity(
                "/api/eventi", 
                new HttpEntity<>(jsonRequest, headers), 
                String.class
        );
    }

    @Then("il sistema salva il nuovo evento")
    public void sistema_salva_nuovo_evento() {
        // Verifichiamo che la risposta sia 200 OK
        assertEquals(200, response.getStatusCode().value());
    }

    @And("il sistema imposta lo stato dell'evento come aperto")
    public void sistema_imposta_stato_aperto() {
        // Verifichiamo che il server ci abbia restituito l'evento con lo stato giusto
        assertTrue(response.getBody().contains("\"stato\":\"aperto\""));
    }

    @And("l'evento appare nella lista degli eventi")
    public void evento_appare_nella_lista() {
        // Facciamo una chiamata GET per vedere se l'evento è nella lista pubblica
        ResponseEntity<String> getResponse = restTemplate.getForEntity("/api/eventi", String.class);
        assertTrue(getResponse.getBody().contains("Gruppo di Studio"));
    }

    @When("l'utente tenta di creare un evento senza compilare i campi obbligatori")
    public void crea_evento_senza_campi() {
        // Creiamo un JSON difettoso (senza titolo)
        String jsonDifettoso = """
            {
              "dataOra": "2026-10-15T15:00:00",
              "luogo": "Biblioteca"
            }
        """;
        response = restTemplate.postForEntity(
                "/api/eventi", 
                new HttpEntity<>(jsonDifettoso, headers), 
                String.class
        );
    }

    @Then("il sistema impedisce la creazione dell'evento")
    public void sistema_impedisce_creazione() {
        // Verifichiamo l'errore 400
        assertEquals(400, response.getStatusCode().value());
    }

    @And("il sistema mostra {string}")
    public void sistema_mostra_messaggio(String messaggioAtteso) {
        // Verifichiamo che la risposta contenga esattamente il testo di errore
        assertTrue(response.getBody().contains(messaggioAtteso));
    }
}
