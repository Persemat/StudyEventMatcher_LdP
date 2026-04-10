package com.dem.ldp.cucumber;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class VisualizzazioneEventi {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private Long utenteId;

    @Given("l'utente si trova nella pagina degli eventi")
    public void utenteSiTrovaNellaPaginaDegliEventi() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password123!");
        utenteRepository.save(utente);

        this.utenteId = utente.getId();
    }

    @And("sono presenti eventi nel sistema")
    public void sonoPresentiEventiNelSistema() {
        Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

        Evento evento1 = new Evento();
        evento1.setTitolo("Ripasso Analisi 1");
        evento1.setDescrizione("Prepariamoci per lo scritto di lunedì");
        evento1.setDataOra(LocalDateTime.of(2026, 4, 10, 10, 30));
        evento1.setDurata(2.0);
        evento1.setLuogo("Aula 4");
        evento1.setTag(List.of("Matematica", "Analisi"));
        evento1.setMaxPartecipanti(10);
        evento1.setPartecipazioneCreatoreNecessaria(true);
        evento1.setStato("aperto");
        evento1.setCreatore(creatore);
        evento1.aggiungiPartecipante(creatore);

        Evento evento2 = new Evento();
        evento2.setTitolo("Workshop React");
        evento2.setDescrizione("Creazione componenti modulari");
        evento2.setDataOra(LocalDateTime.of(2026, 4, 15, 9, 0));
        evento2.setDurata(1.5);
        evento2.setLuogo("Online");
        evento2.setTag(List.of("Frontend"));
        evento2.setMaxPartecipanti(20);
        evento2.setPartecipazioneCreatoreNecessaria(false);
        evento2.setStato("aperto");
        evento2.setCreatore(creatore);
        evento2.aggiungiPartecipante(creatore);

        eventoRepository.save(evento1);
        eventoRepository.save(evento2);
    }

    @And("non sono presenti eventi nel sistema")
    public void nonSonoPresentiEventiNelSistema() {
        eventoRepository.deleteAll();
    }

    @When("accede alla pagina degli eventi")
    public void accedeAllaPaginaDegliEventi() throws Exception {
        result = mockMvc.perform(get("/Eventi")
                .sessionAttr("userId", utenteId));
    }

    @Then("il sistema mostra la lista degli eventi disponibili")
    public void ilSistemaMostraLaListaDegliEventiDisponibili() throws Exception {
        result.andExpect(status().isOk())
              .andExpect(content().string(containsString("Ripasso Analisi 1")))
              .andExpect(content().string(containsString("Workshop React")));
    }

    @And("gli eventi sono visualizzati senza filtri applicati")
    public void gliEventiSonoVisualizzatiSenzaFiltriApplicati() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("per ogni evento vengono mostrati titolo, data e luogo")
    public void perOgniEventoVengonoMostratiTitoloDataELuogo() throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();

        Assertions.assertTrue(response.contains("\"titolo\""));
        Assertions.assertTrue(response.contains("\"dataOra\""));
        Assertions.assertTrue(response.contains("\"luogo\""));
    }

    @Then("il sistema non mostra eventi")
    public void ilSistemaNonMostraEventi() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema restituisce una lista vuota")
    public void ilSistemaRestituisceUnaListaVuota() throws Exception {
        result.andExpect(status().isOk())
              .andExpect(content().json("[]"));
    }
}