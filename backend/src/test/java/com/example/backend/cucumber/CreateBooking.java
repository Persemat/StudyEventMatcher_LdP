package com.example.backend.cucumber;

import io.cucumber.java.Before;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CreateBooking {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession session;
    private User user;
    private Event event;
    private ResultActions result;
    private ResultActions resultGet;

    private static final String EVENTI_URL = "/events";

    @Before
    public void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        session = new MockHttpSession();
        user = null;
        event = null;
        result = null;
    }

    @Given("esiste un utente autenticato per la prenotazione")
    public void esisteUnUtenteAutenticatoPerLaPrenotazione() {
        user = new User("Mario", "Rossi", "mario.prenotazione@example.com", "Password123!",
                "Milano", List.of("Tech"), new ArrayList<>());
        user = userRepository.save(user);

        session.setAttribute("userId", user.getId());
    }

    @And("esiste un evento aperto e non al completo")
    public void esisteUnEventoApertoENonAlCompleto() {
        User creator = new User("Luca", "Bianchi", "luca.creatore@example.com", "Password123!",
                "Roma", List.of("Social"), new ArrayList<>());
        creator = userRepository.save(creator);

        event = new Event();
        event.setTitle("Studio Ingegneria del Software");
        event.setDescription("Sessione di studio");
        event.setLocation("Milano (MI)-Aula 1");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setDuration(2.0);
        event.setMaxParticipants(3);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creator);

        if (event.getParticipants() == null) {
            event.setParticipants(new HashSet<>());
        }
        event.addParticipant(creator);

        event = eventRepository.save(event);
    }

    @Then("il sistema impedisce la prenotazione per evento pieno")
    public void ilSistemaImpedisceLaPrenotazionePerEventoPieno() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @And("il sistema mostra il messaggio di evento al completo")
    public void ilSistemaMostraIlMessaggioDiEventoAlCompleto() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Errore: L'evento è pieno")));
    }

    @And("esiste un evento aperto ma al completo")
    public void esisteUnEventoApertoMaAlCompleto() {
        User creator = new User("Giulia", "Verdi", "giulia.creatore@example.com", "Password123!",
                "Napoli", List.of("Cultura"), new ArrayList<>());
        creator = userRepository.save(creator);

        event = new Event();
        event.setTitle("Progetto Basi di Dati");
        event.setDescription("Lavoro di gruppo");
        event.setLocation("Milano (MI)-Lab 2");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setDuration(2.0);
        event.setMaxParticipants(1);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(creator);

        if (event.getParticipants() == null) {
            event.setParticipants(new HashSet<>());
        }
        event.addParticipant(creator);

        event = eventRepository.save(event);
    }

    @And("l'utente è già prenotato a un evento")
    public void utenteGiaPrenotatoAUnEvento() {
        if (user == null) {
            user = new User("Mario", "Rossi", "mario.giaprenotato@example.com", "Password123!",
                    "Milano", List.of("Tech"), new ArrayList<>());
            user = userRepository.save(user);
            session.setAttribute("userId", user.getId());
        }

        if (event == null) {
            User creator = new User("Anna", "Neri", "anna.creatore@example.com", "Password123!",
                    "Torino", List.of("Education"), new ArrayList<>());
            creator = userRepository.save(creator);

            event = new Event();
            event.setTitle("Progetto Java");
            event.setDescription("Sessione collaborativa");
            event.setLocation("Milano (MI)-Lab 1");
            event.setDateTime(LocalDateTime.now().plusDays(1));
            event.setDuration(2.0);
            event.setMaxParticipants(5);
            event.setCreatorParticipationRequired(false);
            event.setStatus("open");
            event.setCreator(creator);

            if (event.getParticipants() == null) {
                event.setParticipants(new HashSet<>());
            }
            event.addParticipant(creator);
        }

        event.addParticipant(user);
        event = eventRepository.save(event);
    }

    @When("l'utente prenota il posto per quell'evento")
    public void utentePrenotaIlPostoPerQuellEvento() throws Exception {
        result = mockMvc.perform(post(EVENTI_URL + "/" + event.getId() + "/book")
                .session(session));
    }

    @When("l'utente visualizza l'evento")
    public void utenteVisualizzaLEvento() throws Exception {
        result = mockMvc.perform(get(EVENTI_URL)
                .session(session));
    }

    @Then("il sistema registra la prenotazione all'evento")
    public void ilSistemaRegistraLaPrenotazioneAllEvento() throws Exception {
        result.andExpect(status().isOk());

        resultGet = mockMvc.perform(get("/events").session(session));

        resultGet.andExpect(status().isOk());
        resultGet.andExpect(jsonPath("$[0].title").value(event.getTitle()));
        resultGet.andExpect(jsonPath("$[0].isBooked").value(true));
        resultGet.andExpect(jsonPath("$[0].currentParticipants").value(2));
    }

    @And("il sistema mostra il messaggio di successo della prenotazione")
    public void ilSistemaMostraIlMessaggioDiSuccessoDellaPrenotazione() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Prenotazione completata con successo")));
    }

    @Then("il sistema non mostra il pulsante {string}")
    public void ilSistemaNonMostraIlPulsante(String pulsante) throws Exception {
        result.andExpect(status().isOk());

        if ("prenota".equalsIgnoreCase(pulsante)) {
            result.andExpect(content().string(Matchers.containsString(event.getTitle())));
        }
    }

    @And("il sistema segnala che l'evento è al completo")
    public void ilSistemaSegnalaCheLEventoEAlCompleto() throws Exception {
        String eventTitle = event.getTitle();

        result.andExpect(jsonPath("$[?(@.title=='" + eventTitle + "')][0].maxPartecipants")
                .value(event.getMaxParticipants()));

        result.andExpect(jsonPath("$[?(@.title=='" + eventTitle + "')][0].currentParticipants")
                .value(event.getMaxParticipants()));

        result.andExpect(jsonPath("$[?(@.title=='" + eventTitle + "')][0].isBooked")
                .value(false));
    }

    @And("il sistema mostra il pulsante {string}")
    public void ilSistemaMostraIlPulsante(String pulsante) throws Exception {
        result.andExpect(status().isOk());

        if ("annulla prenotazione".equalsIgnoreCase(pulsante)) {
            result.andExpect(jsonPath("$[0].title").value(event.getTitle()));
            result.andExpect(jsonPath("$[0].isBooked").value(true));
        }
    }
}