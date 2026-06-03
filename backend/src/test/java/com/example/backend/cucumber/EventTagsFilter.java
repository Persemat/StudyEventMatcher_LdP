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
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class EventTagsFilter {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private User user;
    private List<String> tags;

    private void creaEventoHelper(String title, List<EventTag> tags) {
        Event event = new Event();
        event.setTitle(title);
        event.setDescription("Descrizione test");
        event.setDateTime(LocalDateTime.now().plusDays(5));
        event.setDuration(2.0);
        event.setLocation("Milano (MI)-Aula Test");
        event.setTags(tags);
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(false);
        event.setStatus("open");
        event.setCreator(user);
        eventRepository.save(event);
    }

    @Given("sono L'utente")
    public void sono_l_utente() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        user = new User(
                "Mario",
                "Rossi",
                "mario.test@example.com",
                "Password123!",
                "Milano",
                List.of("Tech"),
                new ArrayList<>());
        userRepository.save(user);
    }

    @And("ci sono 3 eventi attivi associati a un tag specifico nel database")
    public void ci_sono_3_eventi_attivi_associati_a_un_tag_specifico_nel_database() {
        creaEventoHelper("Evento Info 1", List.of(new EventTag("Informatica e Tecnologie", "Informatica")));
        creaEventoHelper("Evento Info 2", List.of(new EventTag("Informatica e Tecnologie", "Informatica")));
        creaEventoHelper("Evento Info 3", List.of(
                new EventTag("Informatica e Tecnologie", "Informatica"),
                new EventTag("Scienze Matematiche Fisiche e Naturali", "Matematica")));
        creaEventoHelper("Evento Trappola",
                List.of(new EventTag("Scienze della Formazione ed Educazione", "Storia della pedagogia")));
    }

    @When("seleziono quel tag dalla lista predefinita per filtrare gli eventi")
    public void seleziono_quel_tag_dalla_lista_predefinita_per_filtrare_gli_eventi() throws Exception {
        result = mockMvc.perform(
                get("/events/search").queryParam("tags", "Informatica e Tecnologie > Informatica")
                        .sessionAttr("userId", user.getId()));
    }

    @Then("il sistema deve restituire un elenco contenente esattamente quei 3 eventi")
    public void il_sistema_deve_restituire_un_elenco_contenente_esattamente_quei_3_eventi() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(3)))
                .andExpect(jsonPath("$[*].tags[0].macroTag",
                        Matchers.everyItem(Matchers.equalTo("Informatica e Tecnologie"))))
                .andExpect(jsonPath("$[*].tags[0].microTag", Matchers.everyItem(Matchers.equalTo("Informatica"))));
    }

    @And("nessun evento privo di quel tag deve apparire nei risultati")
    public void nessun_evento_privo_di_quel_tag_deve_apparire_nei_risultati() throws Exception {
        result.andExpect(content().string(Matchers.not(Matchers.containsString("Evento Trappola"))));
    }

    @And("esiste 1 evento associato sia al “Tag A” che al “Tag B”")
    public void esiste_1_evento_associato_sia_al_tag_a_che_al_tag_b() {
        creaEventoHelper("Evento Doppio Tag", List.of(
                new EventTag("Informatica e Tecnologie", "SQL"),
                new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")));
    }

    @And("esistono 2 eventi associati solo al “Tag A”")
    public void esistono_2_eventi_associati_solo_al_tag_a() {
        creaEventoHelper("Evento Solo A 1", List.of(new EventTag("Informatica e Tecnologie", "SQL")));
        creaEventoHelper("Evento Solo A 2", List.of(
                new EventTag("Informatica e Tecnologie", "SQL"),
                new EventTag("Scienze Matematiche Fisiche e Naturali", "Chimica")));
    }

    @When("seleziono sia il “Tag A” che il “Tag B” dalla lista predefinita")
    public void seleziono_sia_il_tag_a_che_il_tag_b_dalla_lista_predefinita() throws Exception {
        result = mockMvc.perform(
                get("/events/search")
                        .queryParam("tags", "Informatica e Tecnologie > SQL")
                        .queryParam("tags", "Scienze Matematiche Fisiche e Naturali > Fisica")
                        .sessionAttr("userId", user.getId()));
    }

    @Then("il sistema deve restituire gli eventi che possiedono almeno uno dei tag")
    public void il_sistema_deve_restituire_gli_eventi_che_possiedono_almeno_uno_dei_tag() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(3)))
                .andExpect(content().string(Matchers.containsString("Evento Doppio Tag")))
                .andExpect(content().string(Matchers.containsString("Evento Solo A 1")))
                .andExpect(content().string(Matchers.containsString("Evento Solo A 2")));
    }

    @And("gli eventi che possiedono uno solo dei due tag vengono inclusi nei risultati")
    public void gli_eventi_che_possiedono_uno_solo_dei_due_tag_vengono_inclusi_nei_risultati() throws Exception {
        result.andExpect(content().string(Matchers.containsString("Evento Solo A 1")))
                .andExpect(content().string(Matchers.containsString("Evento Solo A 2")));
    }

    @And("esistono eventi associati a microtag dello stesso macrotag")
    public void esistono_eventi_associati_a_microtag_dello_stesso_macrotag() {
        creaEventoHelper("Evento SQL", List.of(new EventTag("Informatica e Tecnologie", "SQL")));
        creaEventoHelper("Evento Frontend", List.of(new EventTag("Informatica e Tecnologie", "Sviluppo frontend")));
        creaEventoHelper("Evento Fisica", List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")));
    }

    @When("seleziono solo il macrotag dalla lista predefinita")
    public void seleziono_solo_il_macrotag_dalla_lista_predefinita() throws Exception {
        result = mockMvc.perform(
                get("/events/search")
                        .queryParam("tags", "Informatica e Tecnologie")
                        .sessionAttr("userId", user.getId()));
    }

    @Then("il sistema deve restituire tutti gli eventi di quel macrotag")
    public void il_sistema_deve_restituire_tutti_gli_eventi_di_quel_macrotag() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(2)))
                .andExpect(content().string(Matchers.containsString("Evento SQL")))
                .andExpect(content().string(Matchers.containsString("Evento Frontend")))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Evento Fisica"))))
                .andExpect(jsonPath("$[*].tags[0].macroTag",
                        Matchers.everyItem(Matchers.equalTo("Informatica e Tecnologie"))));
    }

    @And("seleziono una combinazione di tag dalla lista predefinita")
    public void seleziono_una_combinazione_di_tag_dalla_lista_predefinita() {
        this.tags = List.of("Informatica e Tecnologie > SQL", "Scienze Matematiche Fisiche e Naturali > Biologia");
    }

    @And("non esistono eventi attivi nel database che possiedano almeno uno dei tag scelti")
    public void non_esistono_eventi_attivi_nel_database_che_possiedano_almeno_uno_dei_tag_scelti() {
        creaEventoHelper("Evento Solo Fisica",
                List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Fisica")));
        creaEventoHelper("Evento Solo Chimica",
                List.of(new EventTag("Scienze Matematiche Fisiche e Naturali", "Chimica")));
    }

    @When("il sistema elabora la richiesta di filtraggio")
    public void il_sistema_elabora_la_richiesta_di_filtraggio() throws Exception {
        result = mockMvc.perform(
                get("/events/search")
                        .queryParam("tags", tags.toArray(String[]::new))
                        .sessionAttr("userId", user.getId()));
    }

    @Then("il sistema restituisce un elenco vuoto di risultati")
    public void il_sistema_restituisce_un_elenco_vuoto_di_risultati() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }
}
