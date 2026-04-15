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
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EmailCancellazioneEvento {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private JavaMailSender mailSender;

    private Utente creatore;
    private Utente utenteIscritto;
    private Utente utenteNonIscritto;
    private Evento evento;
    private ResultActions result;

    @Given("esiste un evento con creatore obbligatorio")
    public void esisteUnEventoConCreatoreObbligatorio() {
        eventoRepository.deleteAll();
        eventoRepository.flush();
        utenteRepository.deleteAll();
        utenteRepository.flush();

        creatore = new Utente();
        creatore.setNome("Mario");
        creatore.setCognome("Rossi");
        creatore.setEmail("mario@example.com");
        creatore.setPassword("Password123");
        creatore = utenteRepository.saveAndFlush(creatore);

        evento = new Evento();
        evento.setTitolo("Ripasso Analisi 1");
        evento.setDataOra(LocalDateTime.of(2026, 3, 5, 14, 0));
        evento.setDurata(2.0);
        evento.setLuogo("Aula 2");
        evento.setTag(List.of("Analisi"));
        evento.setDescrizione("Ripasso in vista dell'esame");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(creatore);

        evento.aggiungiPartecipante(creatore);

        evento = eventoRepository.saveAndFlush(evento);
    }

    @Given("esiste un evento con partecipanti iscritti")
    public void esisteUnEventoConPartecipantiIscritti() {
        esisteUnEventoConCreatoreObbligatorio();

        utenteIscritto = new Utente();
        utenteIscritto.setNome("Paolo");
        utenteIscritto.setCognome("Neri");
        utenteIscritto.setEmail("paolo@example.com");
        utenteIscritto.setPassword("Password123");
        utenteIscritto = utenteRepository.saveAndFlush(utenteIscritto);

        evento.aggiungiPartecipante(utenteIscritto);
        evento = eventoRepository.saveAndFlush(evento);
    }

    @And("l'utente è iscritto all'evento")
    public void lUtenteEIscrittoAllEvento() {
        utenteIscritto = new Utente();
        utenteIscritto.setNome("Anna");
        utenteIscritto.setCognome("Bianchi");
        utenteIscritto.setEmail("anna@example.com");
        utenteIscritto.setPassword("Password123");
        utenteIscritto = utenteRepository.saveAndFlush(utenteIscritto);

        evento.aggiungiPartecipante(utenteIscritto);
        evento = eventoRepository.saveAndFlush(evento);
    }

    @And("un altro utente non è iscritto all'evento")
    public void unAltroUtenteNonEIscrittoAllEvento() {
        utenteNonIscritto = new Utente();
        utenteNonIscritto.setNome("Luca");
        utenteNonIscritto.setCognome("Verdi");
        utenteNonIscritto.setEmail("luca@example.com");
        utenteNonIscritto.setPassword("Password123");
        utenteNonIscritto = utenteRepository.saveAndFlush(utenteNonIscritto);
    }

    @When("il creatore cancella l'evento")
    public void ilCreatoreCancellaLEvento() throws Exception {
        result = mockMvc.perform(delete("/Eventi/{id}", evento.getId())
                .sessionAttr("userId", creatore.getId())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @And("l'utente richiede la lista delle prenotazioni")
    public void lUtenteRichiedeLaListaDellePrenotazioni() throws Exception {
        result = mockMvc.perform(get("/Eventi")
                .sessionAttr("userId", utenteIscritto.getId())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Then("il sistema invia una notifica di cancellazione all'utente")
    public void ilSistemaInviaUnaNotificaDiCancellazioneAllUtente() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, atLeastOnce()).send(captor.capture());

        boolean inviataAllUtenteIscritto = captor.getAllValues().stream()
                .map(SimpleMailMessage::getTo)
                .filter(to -> to != null && to.length > 0)
                .anyMatch(to -> utenteIscritto.getEmail().equals(to[0]));

        assertTrue(inviataAllUtenteIscritto);
    }

    @Then("l'evento non è più presente nella lista")
    public void lEventoNonEPiuPresenteNellaLista() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.not(Matchers.containsString("Ripasso Analisi 1"))));
    }

    @Then("il sistema non invia notifiche a quell'utente")
    public void ilSistemaNonInviaNotificheAQuellUtente() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, atLeastOnce()).send(captor.capture());

        boolean inviataAllUtenteNonIscritto = captor.getAllValues().stream()
                .map(SimpleMailMessage::getTo)
                .filter(to -> to != null && to.length > 0)
                .anyMatch(to -> utenteNonIscritto.getEmail().equals(to[0]));

        assertFalse(inviataAllUtenteNonIscritto);
    }
}