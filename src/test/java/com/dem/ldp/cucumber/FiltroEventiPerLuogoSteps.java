package com.dem.ldp.cucumber;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.datatable.DataTable;
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
import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class FiltroEventiPerLuogoSteps {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private Utente utenteTest;

    // ==========================================
    // BACKGROUND
    // ==========================================

    @Given("l'utente è autenticato per la ricerca per luogo")
    public void l_utente_e_autenticato_per_la_ricerca_per_luogo() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        utenteTest = new Utente();
        utenteTest.setNome("Luigi");
        utenteTest.setCognome("Verdi");
        utenteTest.setEmail("luigi.luogo@example.com");
        utenteTest.setPassword("Pass123!");
        utenteRepository.save(utenteTest);
    }

    @And("esistono i seguenti eventi registrati nel sistema:")
    public void esistono_i_seguenti_eventi_registrati_nel_sistema(DataTable dataTable) {
        List<Map<String, String>> righe = dataTable.asMaps(String.class, String.class);

        for (Map<String, String> riga : righe) {
            Evento evento = new Evento();
            evento.setTitolo(riga.get("Titolo"));
            evento.setLuogo(riga.get("Luogo"));

            // Dati obbligatori fittizi per far felice il database
            evento.setDescrizione("Descrizione automatica");
            evento.setDataOra(LocalDateTime.now().plusDays(10)); // Sempre nel futuro
            evento.setDurata(2.0);
            evento.setStato("aperto");
            evento.setMaxPartecipanti(10);
            evento.setPartecipazioneCreatoreNecessaria(false);
            evento.setCreatore(utenteTest);

            eventoRepository.save(evento);
        }
    }

    // ==========================================
    // WHEN
    // ==========================================

    @When("cerco gli eventi specificando il luogo {string}")
    public void cerco_gli_eventi_specificando_il_luogo(String luogo) throws Exception {
        result = mockMvc.perform(
                get("/Eventi/cerca?luogo=" + luogo)
                        .sessionAttr("userId", utenteTest.getId()));
    }

    @When("cerco gli eventi lasciando il filtro luogo vuoto")
    public void cerco_gli_eventi_lasciando_il_filtro_luogo_vuoto() throws Exception {
        // Parametro vuoto come farebbe il frontend
        result = mockMvc.perform(
                get("/Eventi/cerca?luogo=")
                        .sessionAttr("userId", utenteTest.getId()));
    }

    // ==========================================
    // THEN
    // ==========================================

    @Then("il sistema restituisce {int} eventi")
    public void il_sistema_restituisce_eventi(int numeroAtteso) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(numeroAtteso)));
    }

    @And("tutti gli eventi restituiti si svolgono a {string}")
    public void tutti_gli_eventi_restituiti_si_svolgono_a(String luogoAtteso) throws Exception {
        // Verifica che nel JSON testuale ritorni la stringa del luogo
        result.andExpect(content().string(Matchers.containsString(luogoAtteso)));
    }

    @Then("il sistema restituisce una lista vuota indicando che non ci sono eventi")
    public void il_sistema_restituisce_una_lista_vuota() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(0))); // Array vuoto []
    }

    @Then("il sistema ignora il filtro e restituisce tutti i {int} eventi disponibili")
    public void il_sistema_ignora_il_filtro_e_restituisce_tutti_gli_eventi(int numeroAtteso) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$", Matchers.hasSize(numeroAtteso)));
    }
}