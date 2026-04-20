package com.example.backend.cucumber;

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

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

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

public class EventCancellationNotification {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JavaMailSender mailSender;

    private User creator;
    private User enrolledUser;
    private User notEnrolledUser;
    private Event event;
    private ResultActions result;

    @Given("esiste un evento con creatore obbligatorio")
    public void esisteUnEventoConCreatoreObbligatorio() {
        eventRepository.deleteAll();
        eventRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();

        creator = new User();
        creator.setName("Mario");
        creator.setLastname("Rossi");
        creator.setEmail("mario@example.com");
        creator.setPassword("Password123");
        creator = userRepository.saveAndFlush(creator);

        event = new Event();
        event.setTitle("Ripasso Analisi 1");
        event.setDateTime(LocalDateTime.of(2026, 3, 5, 14, 0));
        event.setDuration(2.0);
        event.setLocation("Aula 2");
        event.setTags(List.of("Analisi"));
        event.setDescription("Ripasso in vista dell'esame");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setStatus("aperto");
        event.setCreator(creator);

        event.addParticipant(creator);

        event = eventRepository.saveAndFlush(event);
    }

    @Given("esiste un evento con partecipanti iscritti")
    public void esisteUnEventoConPartecipantiIscritti() {
        esisteUnEventoConCreatoreObbligatorio();

        enrolledUser = new User();
        enrolledUser.setName("Paolo");
        enrolledUser.setLastname("Neri");
        enrolledUser.setEmail("paolo@example.com");
        enrolledUser.setPassword("Password123");
        enrolledUser = userRepository.saveAndFlush(enrolledUser);

        event.addParticipant(enrolledUser);
        event = eventRepository.saveAndFlush(event);
    }

    @And("l'utente è iscritto all'evento")
    public void lUtenteEIscrittoAllEvento() {
        enrolledUser = new User();
        enrolledUser.setName("Anna");
        enrolledUser.setLastname("Bianchi");
        enrolledUser.setEmail("anna@example.com");
        enrolledUser.setPassword("Password123");
        enrolledUser = userRepository.saveAndFlush(enrolledUser);

        event.addParticipant(enrolledUser);
        event = eventRepository.saveAndFlush(event);
    }

    @And("un altro utente non è iscritto all'evento")
    public void unAltroUtenteNonEIscrittoAllEvento() {
        notEnrolledUser = new User();
        notEnrolledUser.setName("Luca");
        notEnrolledUser.setLastname("Verdi");
        notEnrolledUser.setEmail("luca@example.com");
        notEnrolledUser.setPassword("Password123");
        notEnrolledUser = userRepository.saveAndFlush(notEnrolledUser);
    }

    @When("il creatore cancella l'evento")
    public void ilCreatoreCancellaLEvento() throws Exception {
        result = mockMvc.perform(delete("/events/{id}", event.getId())
                .sessionAttr("userId", creator.getId())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @And("l'utente richiede la lista delle prenotazioni")
    public void lUtenteRichiedeLaListaDellePrenotazioni() throws Exception {
        result = mockMvc.perform(get("/events")
                .sessionAttr("userId", enrolledUser.getId())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Then("il sistema invia una notifica di cancellazione all'utente")
    public void ilSistemaInviaUnaNotificaDiCancellazioneAllUtente() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);

        verify(mailSender, atLeastOnce()).send(captor.capture());

        boolean sentToEnrolledUser = captor.getAllValues().stream()
                .map(SimpleMailMessage::getTo)
                .filter(to -> to != null && to.length > 0)
                .anyMatch(to -> enrolledUser.getEmail().equals(to[0]));

        assertTrue(sentToEnrolledUser);
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

        boolean sentToNotEnrolledUser = captor.getAllValues().stream()
                .map(SimpleMailMessage::getTo)
                .filter(to -> to != null && to.length > 0)
                .anyMatch(to -> notEnrolledUser.getEmail().equals(to[0]));

        assertFalse(sentToNotEnrolledUser);
    }
}