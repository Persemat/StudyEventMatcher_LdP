package com.example.backend.cucumber;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventDateFilter {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private User user;

    private void createEventsDataHelper(String title, LocalDateTime dateTime) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Descrizione test data");
        event.setDateTime(dateTime);
        event.setDuration(1.5);
        event.setLocation("Milano (MI)-Aula Test");
        event.setTags(List.of(new com.example.backend.model.EventTag("Informatica e Tecnologie", "Testing software")));
        event.setMaxParticipants(5);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(user);
        eventRepository.save(event);
    }

    // ==========================================
    // BACKGROUND (Comune a tutti gli scenari)
    // ==========================================

    @Given("L'utente è autenticato nel sistema")
    public void l_utente_e_autenticato_nel_sistema() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        user = new User(
                "Paolo",
                "Verdi",
                "paolo.date@example.com",
                "Password123!",
                "Milano",
                List.of("Tech"),
                new ArrayList<>());
        userRepository.save(user);
    }

    @Given("L'utente ha aperto il calendario dei filtri")
    public void l_utente_ha_aperto_il_calendario_dei_filtri() {
        // Step vuoto per il backend: la UI è pronta, il DB è già pulito dal Background
    }

    @Given("L'utente sta scegliendo una data dal calendario dei filtri")
    public void l_utente_sta_scegliendo_una_data_dal_calendario_dei_filtri() {

        createEventsDataHelper("Evento di Ieri", LocalDateTime.now().minusDays(1).withHour(15));
    }

    // ==========================================
    // SCENARIO 1: Filtro data con successo
    // ==========================================

    @And("sono presenti eventi in una data futura")
    public void sono_presenti_eventi_in_una_data_futura() {
        createEventsDataHelper("Evento Domani", LocalDateTime.now().plusDays(1).withHour(10));
        createEventsDataHelper("Evento Settimana Prossima", LocalDateTime.now().plusDays(7).withHour(10));

    }

    @When("seleziona una data futura in cui sono presenti eventi")
    public void seleziona_una_data_futura_in_cui_sono_presenti_eventi() throws Exception {
        String tomorrow = LocalDate.now().plusDays(1).toString();

        result = mockMvc.perform(get("/events/search")
                .param("date", tomorrow)
                .sessionAttr("userId", user.getId()));
    }

    @Then("il sistema aggiorna la dashboard mostrando solo gli eventi programmati per quella data")
    public void il_sistema_aggiorna_la_dashboard_mostrando_solo() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(jsonPath("$[0].title", Matchers.is("Evento Domani")));
    }

    // ==========================================
    // SCENARIO 2: Data senza eventi
    // ==========================================

    @And("non ci sono eventi registrati per una data specifica vuota")
    public void non_ci_sono_eventi_registrati_per_una_data_specifica_vuota() {
        createEventsDataHelper("Evento Altra Data", LocalDateTime.now().plusDays(2));
    }

    @When("seleziona la data specifica vuota")
    public void seleziona_la_data_specifica_vuota() throws Exception {
        String farDate = LocalDate.now().plusDays(100).toString();

        result = mockMvc.perform(get("/events/search")
                .param("date", farDate)
                .sessionAttr("userId", user.getId()));
    }

    @Then("la lista degli eventi si svuota")
    public void la_lista_degli_eventi_si_svuota() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }

    // ==========================================
    // SCENARIO 3: Selezione di una data passata
    // ==========================================

    @When("prova a selezionare una data passata")
    public void prova_a_selezionare_una_data_passata() throws Exception {
        String yesterday = LocalDate.now().minusDays(1).toString();

        result = mockMvc.perform(get("/events/search")
                .param("date", yesterday)
                .sessionAttr("userId", user.getId()));
    }

    @Then("il sistema non restituisce eventi")
    public void il_sistema_non_restituisce_eventi() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }
}