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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class CancellazioneEvento {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private EventoRepository eventoRepository;

    private ResultActions result;
    private Long utenteId;
    private Long eventoId;
    private Long primoAltroUtenteId;
    private int partecipantiPrimaDellaDelete;

    @Given("l'utente è il creatore di un evento")
    public void lUtenteECreatoreDiUnEvento() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        Utente creatore = new Utente();
        creatore.setNome("Luca");
        creatore.setCognome("Rossi");
        creatore.setEmail("luca@example.com");
        creatore.setPassword("Pass123");
        utenteRepository.save(creatore);

        this.utenteId = creatore.getId();

        Evento evento = new Evento();
        evento.setTitolo("Studio basi di dati");
        evento.setDescrizione("Ripasso SQL");
        evento.setDataOra(LocalDateTime.of(2026, 4, 20, 15, 0));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 2");
        evento.setTag(List.of("Informatica"));
        evento.setMaxPartecipanti(10);
        evento.setStato("aperto");
        evento.setCreatore(creatore);
        evento.aggiungiPartecipante(creatore);

        eventoRepository.save(evento);
        this.eventoId = evento.getId();
    }

    @And("l’evento ha il flag di presenza obbligatoria del creatore attivo")
    public void eventoHaIlFlagAttivo() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        evento.setPartecipazioneCreatoreNecessaria(true);
        eventoRepository.save(evento);
    }

    @And("l’evento ha il flag di presenza obbligatoria del creatore disattivo")
    public void eventoHaIlFlagDisattivo() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        evento.setPartecipazioneCreatoreNecessaria(false);
        eventoRepository.save(evento);
    }

    @And("l'evento ha 5 prenotazioni associate nel database")
    public void eventoHaCinquePrenotazioniAssociateNelDatabase() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();

        for (int i = 1; i <= 5; i++) {
            Utente utente = new Utente();
            utente.setNome("Utente" + i);
            utente.setCognome("Test");
            utente.setEmail("utente" + i + "@example.com");
            utente.setPassword("Pass123");
            utenteRepository.save(utente);
            evento.aggiungiPartecipante(utente);
        }

        eventoRepository.save(evento);
        partecipantiPrimaDellaDelete = evento.getPartecipanti().size();
    }

    @And("l’evento ha altri utenti iscritti")
    public void eventoHaAltriUtentiIscritti() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();

        Utente utente2 = new Utente();
        utente2.setNome("Marco");
        utente2.setCognome("Verdi");
        utente2.setEmail("marco@example.com");
        utente2.setPassword("Pass123");
        utenteRepository.save(utente2);

        Utente utente3 = new Utente();
        utente3.setNome("Anna");
        utente3.setCognome("Bianchi");
        utente3.setEmail("anna@example.com");
        utente3.setPassword("Pass123");
        utenteRepository.save(utente3);

        evento.aggiungiPartecipante(utente2);
        evento.aggiungiPartecipante(utente3);
        eventoRepository.save(evento);

        this.primoAltroUtenteId = List.of(utente2, utente3).stream()
                .map(Utente::getId)
                .min(Long::compareTo)
                .orElseThrow();
    }

    @And("l’evento non ha altri utenti iscritti")
    public void eventoNonHaAltriUtentiIscritti() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        Set<Utente> soloCreatore = new HashSet<>();
        soloCreatore.add(evento.getCreatore());
        evento.setPartecipanti(soloCreatore);
        eventoRepository.save(evento);
    }

    @And("esistono altri utenti iscritti all’evento")
    public void esistonoAltriUtentiIscrittiAllevento() {
        eventoHaAltriUtentiIscritti();
    }

    @When("prova a cancellare l'evento")
    public void provaACancellareLevento() throws Exception {
        result = mockMvc.perform(delete("/Eventi/" + eventoId)
                .sessionAttr("userId", utenteId));
    }

    @Then("dovrebbe ricevere un messaggio di conferma di avvenuta cancellazione")
    public void dovrebbeRicevereMessaggioConferma() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("successo")));
    }

    @And("il record dell'evento viene rimosso dal database")
    public void eventoVieneRimossoDalDatabase() {
        assertFalse(eventoRepository.findById(eventoId).isPresent());
    }

    @And("le 5 prenotazioni associate vengono eliminate a cascata")
    public void prenotazioniAssociateEliminateACascata() {
        assertFalse(eventoRepository.findById(eventoId).isPresent());
        assertTrue(partecipantiPrimaDellaDelete >= 6); // creatore + 5 utenti
    }

    @And("il creatore viene rimosso dall’evento")
    public void creatoreVieneRimossoDallevento() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        boolean creatorePresente = evento.getPartecipanti().stream()
                .anyMatch(u -> u.getId().equals(utenteId));
        assertFalse(creatorePresente);
    }

    @And("l’evento rimane presente nel database")
    public void eventoRimanePresenteNelDatabase() {
        assertTrue(eventoRepository.findById(eventoId).isPresent());
    }

    @And("le prenotazioni degli altri utenti rimangono attive")
    public void prenotazioniDegliAltriUtentiRimangonoAttive() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        assertTrue(evento.getPartecipanti().size() >= 1);
        assertTrue(evento.getPartecipanti().stream().noneMatch(u -> u.getId().equals(utenteId)));
    }

    @And("il sistema assegna il ruolo di creatore al primo utente iscritto")
    public void sistemaAssegnaRuoloCreatoreAlPrimoUtenteIscritto() {
        Evento evento = eventoRepository.findById(eventoId).orElseThrow();
        assertNotNull(evento.getCreatore());
        assertEquals(primoAltroUtenteId, evento.getCreatore().getId());
    }
}