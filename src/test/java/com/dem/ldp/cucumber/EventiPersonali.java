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

public class EventiPersonali {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private Long utenteId;

    @Given("l'utente è autenticato per eventi personali")
    public void utenteEAutenticatoPerEventiPersonali() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        Utente utente = new Utente();
        utente.setNome("Luca");
        utente.setCognome("Rossi");
        utente.setEmail("luca@example.com");
        utente.setPassword("Pass123");
        utenteRepository.save(utente);

        this.utenteId = utente.getId();
    }

    @And("ha eventi personali")
    public void haEventiPersonali() {
        Utente creatore = utenteRepository.findById(utenteId).orElseThrow();

        Evento evento = new Evento();
        evento.setTitolo("Studio Basi di Dati");
        evento.setDescrizione("Ripasso SQL");
        evento.setDataOra(LocalDateTime.of(2026, 4, 10, 15, 0));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 2");
        evento.setTag(List.of("Informatica"));
        evento.setMaxPartecipanti(5);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setStato("aperto");
        evento.setCreatore(creatore);

        eventoRepository.save(evento);
    }

    @And("non ha eventi personali")
    public void nonHaEventiPersonali() {
        Utente altroUtente = new Utente();
        altroUtente.setNome("Marco");
        altroUtente.setCognome("Verdi");
        altroUtente.setEmail("marco@example.com");
        altroUtente.setPassword("Pass123");
        utenteRepository.save(altroUtente);

        Evento evento = new Evento();
        evento.setTitolo("Evento di un altro utente");
        evento.setDescrizione("Descrizione");
        evento.setDataOra(LocalDateTime.of(2026, 4, 12, 10, 0));
        evento.setDurata(3.0);
        evento.setLuogo("Biblioteca");
        evento.setTag(List.of("Matematica"));
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(altroUtente);

        eventoRepository.save(evento);
    }

    @When("l'utente visualizza la lista degli eventi personali")
    public void utenteVisualizzaLaListaDegliEventiPersonali() throws Exception {
        if (utenteId != null) {
            result = mockMvc.perform(get("/Eventi").sessionAttr("userId", utenteId));
        } else {
            result = mockMvc.perform(get("/Eventi"));
        }
    }

    @Then("per ogni evento personale vengono mostrati titolo, data e luogo")
    public void perOgniEventoPersonaleVengonoMostratiTitoloDataELuogo() throws Exception {
        String response = result.andReturn().getResponse().getContentAsString();

        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"titolo\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"dataOra\""));
        org.junit.jupiter.api.Assertions.assertTrue(response.contains("\"luogo\""));
    }

    @Then("il sistema mostra la lista degli eventi personali")
    public void ilSistemaMostraLaListaDegliEventiPersonali() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Studio Basi di Dati")))
                .andExpect(content().string(Matchers.containsString("\"isCreatedByMe\":true")));
    }

    @Then("il sistema mostra un messaggio che indica che non ci sono eventi personali")
    public void ilSistemaMostraUnMessaggioCheIndicaCheNonCiSonoEventiPersonali() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("\"isCreatedByMe\":true"))));
    }

    @Then("il sistema impedisce l'accesso")
    public void ilSistemaImpedisceLAccesso() throws Exception {
        result.andExpect(status().isUnauthorized());
    }

    @And("il sistema richiede l'autenticazione")
    public void ilSistemaRichiedeLAutenticazione() throws Exception {
        result.andExpect(status().isUnauthorized());
    }
}