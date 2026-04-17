package com.dem.ldp.cucumber;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FiltroEventiPerTagSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private Utente utenteTest;
    private List<String> tagSelezionati;

    private void creaEventoHelper(String titolo, List<String> tags) {
        Evento evento = new Evento();
        evento.setTitolo(titolo);
        evento.setDescrizione("Descrizione test");
        evento.setDataOra(LocalDateTime.now().plusDays(5));
        evento.setDurata(2.0);
        evento.setLuogo("Aula Test");
        evento.setTag(tags);
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setStato("aperto");
        evento.setCreatore(utenteTest);
        eventoRepository.save(evento);
    }

    // ==========================================
    // GIVEN COMUNI
    // ==========================================

    @Given("sono L'utente")
    public void sono_l_utente() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        utenteTest = new Utente();
        utenteTest.setNome("Mario");
        utenteTest.setCognome("Rossi");
        utenteTest.setEmail("mario.test@example.com");
        utenteTest.setPassword("Password123!");
        utenteRepository.save(utenteTest); // Salva e genera l'ID
    }

    // ==========================================
    // SCENARIO 1: Singolo Tag
    // ==========================================

    @And("ci sono 3 eventi attivi associati a un tag specifico nel database")
    public void ci_sono_3_eventi_attivi_associati_a_un_tag_specifico_nel_database() {
        creaEventoHelper("Evento Info 1", List.of("Informatica"));
        creaEventoHelper("Evento Info 2", List.of("Informatica"));
        creaEventoHelper("Evento Info 3", List.of("Informatica", "Matematica"));
        creaEventoHelper("Evento Trappola", List.of("Storia"));
    }

    @When("seleziono quel tag dalla lista predefinita per filtrare gli eventi")
    public void seleziono_quel_tag_dalla_lista_predefinita_per_filtrare_gli_eventi() throws Exception {
        // AGGIUNTO IL SESSION ATTR!
        result = mockMvc.perform(
                get("/Eventi/cerca?tags=Informatica")
                        .sessionAttr("userId", utenteTest.getId()));
    }

    @Then("il sistema deve restituire un elenco contenente esattamente quei 3 eventi")
    public void il_sistema_deve_restituire_un_elenco_contenente_esattamente_quei_3_eventi() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(3)));
    }

    @And("nessun evento privo di quel tag deve apparire nei risultati")
    public void nessun_evento_privo_di_quel_tag_deve_apparire_nei_risultati() throws Exception {
        result.andExpect(content().string(Matchers.not(Matchers.containsString("Evento Trappola"))));
    }

    // ==========================================
    // SCENARIO 2: Logica AND (Più tag)
    // ==========================================

    @And("esiste 1 evento associato sia al “Tag A” che al “Tag B”")
    public void esiste_1_evento_associato_sia_al_tag_a_che_al_tag_b() {
        creaEventoHelper("Evento Doppio Tag", List.of("Tag A", "Tag B"));
    }

    @And("esistono 2 eventi associati solo al “Tag A”")
    public void esistono_2_eventi_associati_solo_al_tag_a() {
        creaEventoHelper("Evento Solo A 1", List.of("Tag A"));
        creaEventoHelper("Evento Solo A 2", List.of("Tag A", "Tag C"));
    }

    @When("seleziono sia il “Tag A” che il “Tag B” dalla lista predefinita")
    public void seleziono_sia_il_tag_a_che_il_tag_b_dalla_lista_predefinita() throws Exception {
        // AGGIUNTO IL SESSION ATTR!
        result = mockMvc.perform(
                get("/Eventi/cerca?tags=Tag A&tags=Tag B")
                        .sessionAttr("userId", utenteTest.getId()));
    }

    @Then("il sistema deve restituire solo l'evento che possiede entrambi i tag")
    public void il_sistema_deve_restituire_solo_l_evento_che_possiede_entrambi_i_tag() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(content().string(Matchers.containsString("Evento Doppio Tag")));
    }

    @And("gli eventi che possiedono solo uno dei due tag vengono esclusi dai risultati")
    public void gli_eventi_che_possiedono_solo_uno_dei_due_tag_vengono_esclusi_dai_risultati() throws Exception {
        result.andExpect(content().string(Matchers.not(Matchers.containsString("Evento Solo A 1"))))
                .andExpect(content().string(Matchers.not(Matchers.containsString("Evento Solo A 2"))));
    }

    // ==========================================
    // SCENARIO 3: Risultato Vuoto
    // ==========================================

    @And("seleziono una combinazione di tag dalla lista predefinita")
    public void seleziono_una_combinazione_di_tag_dalla_lista_predefinita() {
        this.tagSelezionati = List.of("Fisica", "Chimica");
    }

    @And("non esistono eventi attivi nel database che possiedano tutti i tag scelti")
    public void non_esistono_eventi_attivi_nel_database_che_possiedano_tutti_i_tag_scelti() {
        creaEventoHelper("Evento Solo Fisica", List.of("Fisica"));
        creaEventoHelper("Evento Solo Chimica", List.of("Chimica"));
    }

    @When("il sistema elabora la richiesta di filtraggio")
    public void il_sistema_elabora_la_richiesta_di_filtraggio() throws Exception {
        String url = "/Eventi/cerca?tags=" + String.join("&tags=", tagSelezionati);

        // AGGIUNTO IL SESSION ATTR!
        result = mockMvc.perform(
                get(url)
                        .sessionAttr("userId", utenteTest.getId()));
    }

    @Then("il sistema restituisce un elenco vuoto di risultati")
    public void il_sistema_restituisce_un_elenco_vuoto_di_risultati() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }
}
