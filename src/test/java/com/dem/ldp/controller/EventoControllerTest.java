/*package com.dem.ldp.controller;

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

    private String sessionCookie; // Qui salveremo il cookie per "fingere" il login

    @BeforeEach
    void preparaDatabase() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        // 1. Creiamo l'utente
        Utente utenteTest = new Utente();
        utenteTest.setNome("Luca");
        utenteTest.setCognome("Bianchi");
        utenteTest.setEmail("luca@example.com");
        utenteTest.setPassword("Pass123"); // In produzione sarebbe criptata
        utenteRepository.save(utenteTest);

        // 2. SIMULIAMO IL LOGIN per ottenere la sessione
        // Assumendo che tu abbia un endpoint /Utenti/login che setta l'attributo "userId"
        // Se non hai ancora l'endpoint di login pronto, questo test fallirà.
        // In alternativa, dobbiamo "mockare" la sessione nel controller (più complesso con RestTemplate)
        
        // Esempio di chiamata login per catturare il cookie:
        
        ResponseEntity<String> loginRes = restTemplate.postForEntity("/Utenti/login", 
            new LoginRequest("luca@example.com", "Pass123"), String.class);
        this.sessionCookie = loginRes.getHeaders().getFirst(HttpHeaders.SET_COOKIE);
        
    }

    @Test
    void creazioneEventoValido_restituisce200ESalvaEvento() {
        // Il JSON ora NON HA PIÙ il creatoreId!
        String json = """
                {
                  "titolo": "Gruppo di studio Java",
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java", "Programmazione"],
                  "descrizione": "Ripasso per l'esame",
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        
        // IMPORTANTE: Aggiungiamo il cookie di sessione se lo abbiamo
        if (sessionCookie != null) {
            headers.add(HttpHeaders.COOKIE, sessionCookie);
        }

        // Lanciamo la chiamata POST
        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Eventi", 
                new HttpEntity<>(json, headers),
                String.class
        );

        // Se non sei loggato, Spring Security o il tuo Service risponderanno 401
        // Se il test deve passare, l'utente deve essere autenticato!
        assertEquals(200, response.getStatusCode().value());
        assertTrue(response.getBody().contains("Gruppo di studio Java"));
        assertEquals(1, eventoRepository.count());
    }

    @Test
    void creazioneEventoSenzaTitolo_restituisce400() {
        String json = """
                {
                  "titolo": "", 
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java"],
                  "descrizione": "Ripasso",
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true
                }
                """;

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        if (sessionCookie != null) headers.add(HttpHeaders.COOKIE, sessionCookie);

        ResponseEntity<String> response = restTemplate.postForEntity(
                "/Eventi",
                new HttpEntity<>(json, headers),
                String.class
        );

        assertEquals(400, response.getStatusCode().value());
        assertEquals(0, eventoRepository.count());
    }
}*/

package com.dem.ldp.controller;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;

@SpringBootTest
@AutoConfigureMockMvc // <--- Abilita MockMvc
@ActiveProfiles("test")
class EventoControllerTest {

    @Autowired
    private MockMvc mockMvc; // <--- Usiamo questo al posto di RestTemplate

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    private Long utenteId;

    @BeforeEach
    void preparaDatabase() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        Utente utenteTest = new Utente();
        utenteTest.setNome("Luca");
        utenteTest.setCognome("Rossi");
        utenteTest.setEmail("luca@example.com");
        utenteTest.setPassword("Pass123");
        utenteRepository.save(utenteTest);

        this.utenteId = utenteTest.getId();
    }

    @Test
    void creazioneEventoValido_restituisce200ESalvaEvento() throws Exception {
        String json = """
                {
                  "titolo": "Gruppo di studio Java",
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java"],
                  "descrizione": "Ripasso",
                  "maxPartecipanti": 10,
                  "partecipazioneCreatoreNecessaria": true
                }
                """;

        mockMvc.perform(post("/Eventi")
                .sessionAttr("userId", utenteId) // <--- ECCO IL TRUCCO: iniettiamo l'ID in sessione
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk()) // Verifica che torni 200
                .andExpect(content().string(org.hamcrest.Matchers.containsString("creato con successo")));

        assertEquals(1, eventoRepository.count());
    }

    @Test
    void creazioneEventoSenzaTitolo_restituisce400() throws Exception {
        String json = """
                {
                  "titolo": "",
                  "dataOra": "2026-10-15T15:00:00",
                  "durata": 2.5,
                  "luogo": "Aula Magna",
                  "tag": ["Java"],
                  "maxPartecipanti": 10
                }
                """;

        mockMvc.perform(post("/Eventi")
                .sessionAttr("userId", utenteId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isBadRequest()); // Verifica che torni 400

        assertEquals(0, eventoRepository.count());
    }
    @Test
void getAllEvents_restituisceListaEventi() throws Exception {
    Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

    Evento evento = new Evento();
    evento.setTitolo("Workshop React");
    evento.setDataOra(java.time.LocalDateTime.parse("2026-04-15T09:00:00"));
    evento.setDurata(1.5);
    evento.setLuogo("Online");
    evento.setTag(java.util.List.of("Frontend"));
    evento.setDescrizione("Creazione componenti modulari");
    evento.setStato("aperto");
    evento.setCreatore(creatore);

    eventoRepository.save(evento);

    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].titolo").value("Workshop React"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].luogo").value("Online"))
            .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath("$[0].dataOra").exists());
}
@Test
void getAllEvents_senzaEventi_restituisceListaVuota() throws Exception {
    mockMvc.perform(org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get("/Eventi")
            .sessionAttr("userId", utenteId))
            .andExpect(status().isOk())
            .andExpect(content().json("[]"));
}
}