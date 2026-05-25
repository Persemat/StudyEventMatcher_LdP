package com.example.backend.cucumber;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import io.cucumber.java.Before;
import io.cucumber.java.en.*;

import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.HashSet;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FavoriteEvent {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    private MockHttpSession session;
    private User user;
    private User creator;
    private Event event;

    private ResultActions result;
    private ResultActions resultGet;

    private static final String EVENTI_URL = "/events";

    /* ---------------- SETUP ---------------- */

    @Before
    public void setUp() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        session = new MockHttpSession();
        user = null;
        creator = null;
        event = null;
        result = null;
        resultGet = null;
    }

    /* ---------------- GIVEN ---------------- */

    @Given("l'utente sta visualizzando i dettagli di un evento")
    public void utenteVisualizzaDettagliEvento() {

        user = new User();
        user.setName("Mario");
        user.setLastname("Rossi");
        user.setEmail("fav@example.com");
        user.setPassword("Password123!");
        user.setFavouriteCity("Milano");
        user = userRepository.save(user);

        session.setAttribute("userId", user.getId());

        creator = new User();
        creator.setName("Luca");
        creator.setLastname("Bianchi");
        creator.setEmail("creator@example.com");
        creator.setPassword("Password123!");
        creator.setFavouriteCity("Milano");
        creator = userRepository.save(creator);

        event = new Event();
        event.setTitle("Progetto Java");
        event.setDescription("Evento di test");
        event.setLocation("Milano");
        event.setDateTime(LocalDateTime.now().plusDays(1));
        event.setDuration(2.0);
        event.setMaxParticipants(10);
        event.setCreator(creator);
        event.setStatus("open");

        event.setParticipants(new HashSet<>());
        event.addParticipant(creator);

        event = eventRepository.save(event);
    }

    @Given("l'utente si trova nella sezione {string}")
    public void utenteSiTrovaNellaSezione(String section) {
        if(user == null){
            utenteVisualizzaDettagliEvento();
        }
        session.setAttribute("userId", user.getId());
    }

    @Given("l'evento {string} è presente nell'elenco dei preferiti")
    public void eventoPresenteNeiPreferiti(String title) {
        if(user == null){
            utenteVisualizzaDettagliEvento();
        }
        event.setTitle(title);
        event = eventRepository.save(event);

        try {
            mockMvc.perform(
                    post("/events/{id}/favorite", event.getId())
                            .session(session)
            );
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Given("l'utente ha salvato nell'elenco degli eventi preferiti l'evento {string}")
    public void utenteHaGiaSalvatoPreferito(String title) throws Exception {
        if(user == null || event == null){
            utenteVisualizzaDettagliEvento();
        }
        event.setTitle(title);
        event = eventRepository.save(event);

        session.setAttribute("userId", user.getId());

        // simula già preferito
        result = mockMvc.perform(
                post("/events/{id}/favorite", event.getId())
                        .session(session)
        );
    }

    /* ---------------- WHEN ---------------- */

    @When("l'utente clicca il pulsante {string}")
    public void utenteCliccaPulsante(String button) throws Exception {

        if (button.equals("Aggiungi ai preferiti") ||
            button.equals("Rimuovi dai preferiti")) {

            result = mockMvc.perform(
                    post("/events/{id}/favorite", event.getId())
                            .session(session)
            );
        }
    }

    @When("il creatore dell'evento {string} cancella l'evento dalla piattaforma")
    public void creatoreCancellaEvento(String title) throws Exception {
        MockHttpSession creatorSession = new MockHttpSession();
        creatorSession.setAttribute("userId", creator.getId());

        result = mockMvc.perform(
                delete("/events/{id}", event.getId())
                        .session(creatorSession)
        );
    }

    /* ---------------- THEN ---------------- */

    @Then("il sistema inserisce l'evento nella sezione personale {string}")
    public void sistemaInseriscePreferito(String section) throws Exception {

        result.andExpect(status().isOk())
              .andExpect(content().string("Operazione sui preferiti completata"));

        resultGet = mockMvc.perform(get(EVENTI_URL).session(session));

        resultGet.andExpect(status().isOk())
                 .andExpect(jsonPath("$[0].isFavorite").value(true));
    }

    @Then("l'evento scompare immediatamente dall'elenco")
    public void eventoScompareDallElenco() throws Exception {

        result.andExpect(status().isOk());

        resultGet = mockMvc.perform(get(EVENTI_URL).session(session));

        resultGet.andExpect(status().isOk())
                 .andExpect(jsonPath("$[0].isFavorite").value(false));
    }

    @Then("il sistema rimuove automaticamente l'evento anche dalla lista dei preferiti dell'utente")
    public void sistemaRimuoveAutomaticamentePreferito() throws Exception {

        resultGet = mockMvc.perform(get(EVENTI_URL).session(session));

        resultGet.andExpect(status().isOk())
                 .andExpect(jsonPath("$").isEmpty());
    }
}