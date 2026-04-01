package com.dem.ldp.controller;

import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureTestRestTemplate
@ActiveProfiles("test")
class UtenteControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private UtenteRepository utenteRepository;

    @BeforeEach
    void cleanDatabase() {
        utenteRepository.deleteAll();
    }

    @Test
    void registrazioneValida_restituisce200ESalvaUtente() {
        String json = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Utenti",
                new HttpEntity<>(json, headers),
                String.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("mario@example.com"));
        assertTrue(utenteRepository.findByEmail("mario@example.com").isPresent());
    }

    @Test
    void registrazioneEmailGiaPresente_restituisce400() {
        String json = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/Utenti", new HttpEntity<>(json, headers), String.class);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Utenti",
                new HttpEntity<>(json, headers),
                String.class
        );

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Email già registrata"));
    }

    @Test
    void loginValido_restituisce200() {
        String registrazione = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/Utenti", new HttpEntity<>(registrazione, headers), String.class);

        String login = """
                {
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Utenti/login",
                new HttpEntity<>(login, headers),
                String.class
        );

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("mario@example.com"));
    }

    @Test
    void loginPasswordErrata_restituisce400() {
        String registrazione = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/Utenti", new HttpEntity<>(registrazione, headers), String.class);

        String login = """
                {
                  "email": "mario@example.com",
                  "password": "Password999"
                }
                """;

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Utenti/login",
                new HttpEntity<>(login, headers),
                String.class
        );

        assertEquals(400, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Password errata"));
    }

    @Test
    void getUtenti_restituisceListaUtenti() {
        String json1 = """
                {
                  "nome": "Mario",
                  "cognome": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        String json2 = """
                {
                  "nome": "Luigi",
                  "cognome": "Verdi",
                  "email": "luigi@example.com",
                  "password": "Password123"
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        restTemplate.postForEntity("/Utenti", new HttpEntity<>(json1, headers), String.class);
        restTemplate.postForEntity("/Utenti", new HttpEntity<>(json2, headers), String.class);

        ResponseEntity<String> response = restTemplate.getForEntity("/Utenti", String.class);

        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("mario@example.com"));
        assertTrue(response.getBody().contains("luigi@example.com"));
    }
}