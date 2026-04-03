package com.dem.ldp.controller;

import com.dem.ldp.model.Utente; // Assicurati che l'import corrisponda al tuo modello
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
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
class EventoControllerTest {

    @Autowired
    private TestRestTemplate restTemplate;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private Long utenteId; // Salveremo qui l'ID dell'utente di prova

    // Questo metodo viene eseguito PRIMA di ogni singolo @Test
    @BeforeEach
    void preparaDatabase() {
        
        //Puliamo tutto per avere un ambiente pulito
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        //Creiamo un utente di base che farà da "creatore" degli eventi
        Utente utenteTest = new Utente();
        utenteTest.setNome("Luca");
        utenteTest.setCognome("Bianchi");
        utenteTest.setEmail("luca@example.com");
        utenteTest.setPassword("Pass123");
        utenteRepository.save(utenteTest);

        //Salviamo l'ID generato dal database per usarlo nei test
        this.utenteId = utenteTest.getId(); 
    }

    @Test
    void creazioneEventoValido_restituisce200ESalvaEvento() {
        // Prepariamo il pacchetto JSON. Usiamo %d per inserire dinamicamente l'ID dell'utente
        String jsonTemplate = """
                {
                  "titolo": "Gruppo di studio Java",
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java", "Programmazione"],
                  "descrizione": "Ripasso per l'esame",
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true,
                  "creatoreId": %d
                }
                """;
        //Inseriamo l'ID vero al posto di %d
        String json = String.format(jsonTemplate, utenteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        //Lanciamo la chiamata POST
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Eventi", 
                new HttpEntity<>(json, headers),
                String.class
        );

        //Verifiche
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Gruppo di studio Java"));
        //Verifichiamo che il database non sia vuoto
        assertEquals(1, eventoRepository.count());
    }

    @Test
    void creazioneEventoSenzaTitolo_restituisce400() {
        String jsonTemplate = """
                {
                  "titolo": "", 
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java"],
                  "descrizione": "Ripasso",
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true,
                  "creatoreId": %d
                }
                """;
        String json = String.format(jsonTemplate, utenteId);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Eventi",
                new HttpEntity<>(json, headers),
                String.class
        );

        // Ci aspettiamo un errore 400 Bad Request
        assertEquals(400, response.getStatusCode().value());
        // L'evento NON deve essere stato salvato
        assertEquals(0, eventoRepository.count());
    }
}