package com.example.backend.cucumber;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateEvent {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private Long userId;
    private ResultActions result;
    
    // Usiamo una semplice Stringa per il JSON!
    private String jsonPayload;

    // Fissiamo una data per usarla nel test dei duplicati
    private final String fixedDateString = LocalDateTime.now().plusDays(10).withNano(0).toString();

    // ==========================================
    // GIVEN - SETUP INIZIALE
    // ==========================================

    @Given("l'utente si trova nella pagina di creazione evento")
    public void lUtenteSiTrovaNellaPaginaDiCreazioneEvento() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User user = new User();
        user.setName("Giulia");
        user.setLastname("Bianchi");
        user.setEmail("giulia.creazione@example.com");
        user.setPassword("Pass123!");
        userRepository.save(user);

        this.userId = user.getId();
        this.jsonPayload = "{}"; // Reset a JSON vuoto
    }

    @And("l'utente ha gia' creato un evento nel sistema")
    public void lUtenteHaGiaCreatoUnEventoNelSistema() {
        User creator = userRepository.findById(userId).orElseThrow();

        Event originalEvent = new Event();
        originalEvent.setTitle("Evento Antiduplicato");
        originalEvent.setDescription("Descrizione originale");
        originalEvent.setDateTime(LocalDateTime.parse(fixedDateString));
        originalEvent.setDuration(2.0);
        originalEvent.setLocation("Aule Studio");
        originalEvent.setMaxParticipants(10);
        originalEvent.setStatus("open");
        originalEvent.setCreator(creator);
        
        eventRepository.save(originalEvent);
    }

    // ==========================================
    // WHEN & AND - AZIONI
    // ==========================================

    @When("l'utente inserisce titolo, descrizione, data, durata e luogo")
    public void lUtenteInserisceTitoloDescrizioneDataDurataELuogo() {
        // Questa è la potenza di Java 21: scriviamo il JSON nudo e crudo!
        jsonPayload = """
            {
                "title": "Nuovo Gruppo di Studio",
                "description": "Prepariamo l'esame insieme",
                "dateTime": "%s",
                "duration": 2.0,
                "location": "Biblioteca"
            }
        """.formatted(LocalDateTime.now().plusDays(5).toString());
    }

    @And("aggiunge uno o piu tag e imposta un limite di partecipanti")
    public void aggiungeUnoOPiuTagEImpostaUnLimiteDiPartecipanti() {
        // Sovrascriviamo il payload per includere tutto
        jsonPayload = """
            {
                "title": "Nuovo Gruppo di Studio",
                "description": "Prepariamo l'esame insieme",
                "dateTime": "%s",
                "duration": 2.0,
                "location": "Biblioteca",
                "tags": ["STUDIO", "ESAME"],
                "maxParticipants": 10
            }
        """.formatted(LocalDateTime.now().plusDays(5).toString());
    }

    @And("imposta il flag Partecipazione del creatore necessaria")
    public void impostaIlFlagPartecipazioneDelCreatoreNecessaria() {
        // Payload completo finale
        jsonPayload = """
            {
                "title": "Nuovo Gruppo di Studio",
                "description": "Prepariamo l'esame insieme",
                "dateTime": "%s",
                "duration": 2.0,
                "location": "Biblioteca",
                "tags": ["STUDIO", "ESAME"],
                "maxParticipants": 10,
                "creatorParticipationRequired": true
            }
        """.formatted(LocalDateTime.now().plusDays(5).toString());
    }

    @When("l'utente tenta di creare un evento senza compilare i campi obbligatori")
    public void lUtenteTentaDiCreareUnEventoSenzaCompilareICampiObbligatori() throws Exception {
        // Inseriamo i campi vuoti e il boolean, così Jackson non va in crash 
        // e permette al Controller di fare la sua validazione.
        jsonPayload = """
            {
                "title": "",
                "description": "",
                "location": "",
                "creatorParticipationRequired": false
            }
        """; 
        
        result = mockMvc.perform(post("/events")
                .sessionAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload));
    }

    @When("l'utente compila i campi con gli stessi dati esatti dell'evento esistente")
    public void lUtenteCompilaICampiConGliStessiDati() {
        // JSON identico all'evento creato nel Setup
        jsonPayload = """
            {
                "title": "Evento Antiduplicato",
                "description": "Nuova descrizione che non importa",
                "dateTime": "%s",
                "duration": 2.0,
                "location": "Aule Studio",
                "tags": ["STUDIO"],
                "maxParticipants": 10,
                "creatorParticipationRequired": true
            }
        """.formatted(fixedDateString);
    }

    @And("conferma la creazione")
    public void confermaLaCreazione() throws Exception {
        result = mockMvc.perform(post("/events")
                .sessionAttr("userId", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(jsonPayload)); // Invia la stringa JSON diretta
    }

    // ==========================================
    // THEN - VERIFICHE
    // ==========================================

    @Then("il sistema salva il nuovo evento")
    public void ilSistemaSalvaIlNuovoEvento() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema imposta lo stato dell'evento come aperto")
    public void ilSistemaImpostaLoStatoDellEventoComeAperto() {
        List<Event> events = eventRepository.findAll();
        Event savedEvent = events.get(events.size() - 1);
        Assertions.assertEquals("open", savedEvent.getStatus());
    }

    @And("l'evento appare nella lista degli eventi")
    public void lEventoAppareNellaListaDegliEventi() throws Exception {
        mockMvc.perform(get("/events").sessionAttr("userId", userId))
               .andExpect(status().isOk())
               .andExpect(content().string(containsString("Nuovo Gruppo di Studio")));
    }

    @Then("il sistema impedisce la creazione dell'evento")
    public void ilSistemaImpedisceLaCreazioneDellEvento() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @And("il sistema mostra l'errore {string}")
    public void ilSistemaMostraLErrore(String expectedErrorMessage) throws Exception {
        result.andExpect(content().string(containsString(expectedErrorMessage)));
    }
}