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

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FiltroEventiPerDataSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private Utente utenteTest;

    private String titoloEventoDomani = "Evento di Domani";
    private String titoloEventoProssimaSettimana = "Evento Prossima Settimana";

    // ==========================================
    // BACKGROUND
    // ==========================================

    @Given("l'utente è autenticato per la ricerca per data")
    public void l_utente_e_autenticato_per_la_ricerca_per_data() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        utenteTest = new Utente();
        utenteTest.setNome("Giulia");
        utenteTest.setCognome("Bianchi");
        utenteTest.setEmail("giulia.data@example.com");
        utenteTest.setPassword("Pass123!");
        utenteRepository.save(utenteTest);
    }

    @And("esistono eventi pianificati per domani e per la prossima settimana")
    public void esistono_eventi_pianificati_per_domani_e_per_la_prossima_settimana() {
        // Evento di DOMANI
        Evento eventoDomani = new Evento();
        eventoDomani.setTitolo(titoloEventoDomani);
        eventoDomani.setDataOra(LocalDateTime.now().plusDays(1).withHour(15)); // Domani alle 15:00
        eventoDomani.setLuogo("Aula 1");
        eventoDomani.setDescrizione("Descrizione domani");
        eventoDomani.setDurata(2.0);
        eventoDomani.setStato("aperto");
        eventoDomani.setMaxPartecipanti(10);
        eventoDomani.setCreatore(utenteTest);
        eventoRepository.save(eventoDomani);

        // Evento della PROSSIMA SETTIMANA
        Evento eventoFuturo = new Evento();
        eventoFuturo.setTitolo(titoloEventoProssimaSettimana);
        eventoFuturo.setDataOra(LocalDateTime.now().plusDays(7).withHour(10)); // Tra 7 giorni
        eventoFuturo.setLuogo("Aula 2");
        eventoFuturo.setDescrizione("Descrizione futuro");
        eventoFuturo.setDurata(2.0);
        eventoFuturo.setStato("aperto");
        eventoFuturo.setMaxPartecipanti(10);
        eventoFuturo.setCreatore(utenteTest);
        eventoRepository.save(eventoFuturo);
    }

    // ==========================================
    // WHEN
    // ==========================================

    @When("cerco gli eventi specificando la data di domani")
    public void cerco_gli_eventi_specificando_la_data_di_domani() throws Exception {
        // Formatta la data in YYYY-MM-DD, esattamente come fa <input type="date"> in
        // React
        String dataDomani = LocalDate.now().plusDays(1).toString();

        result = mockMvc.perform(
                get("/Eventi/cerca?data=" + dataDomani)
                        .sessionAttr("userId", utenteTest.getId()));
    }

    @When("cerco gli eventi in una data remota in cui non c'è nulla di programmato \\(es. tra {int} giorni)")
    public void cerco_gli_eventi_in_una_data_remota_in_cui_non_c_e_nulla_di_programmato(int giorni) throws Exception {
        String dataVuota = LocalDate.now().plusDays(giorni).toString();

        result = mockMvc.perform(
                get("/Eventi/cerca?data=" + dataVuota)
                        .sessionAttr("userId", utenteTest.getId()));
    }

    @When("un client aggira il frontend e cerca eventi per una data di ieri")
    public void un_client_aggira_il_frontend_e_cerca_eventi_per_una_data_di_ieri() throws Exception {
        String dataPassata = LocalDate.now().minusDays(1).toString(); // IERI

        result = mockMvc.perform(
                get("/Eventi/cerca?data=" + dataPassata)
                        .sessionAttr("userId", utenteTest.getId()));
    }

    // ==========================================
    // THEN
    // ==========================================

    @Then("il sistema restituisce solo gli eventi programmati per domani")
    public void il_sistema_restituisce_solo_gli_eventi_programmati_per_domani() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(1)))
                .andExpect(content().string(Matchers.containsString(titoloEventoDomani)));
    }

    @And("non restituisce gli eventi della prossima settimana")
    public void non_restituisce_gli_eventi_della_prossima_settimana() throws Exception {
        result.andExpect(content().string(Matchers.not(Matchers.containsString(titoloEventoProssimaSettimana))));
    }

    @Then("il sistema restituisce una lista vuota indicando che non ci sono eventi in quella data")
    public void il_sistema_restituisce_una_lista_vuota_indicando_che_non_ci_sono_eventi() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }

    @Then("il sistema protegge i dati e restituisce una lista vuota poiché gli eventi passati sono chiusi")
    public void il_sistema_protegge_i_dati_e_restituisce_una_lista_vuota() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0)));
    }
}