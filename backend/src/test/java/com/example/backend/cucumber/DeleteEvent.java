package com.example.backend.cucumber;

import com.example.backend.model.Event;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;

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
public class DeleteEvent {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    private ResultActions result;
    private Long userId;
    private Long eventId;
    private Long firstOtherUserId;
    private int partecipantsBeforeDelete;

    @Given("l'utente è il creatore di un evento")
    public void lUtenteECreatoreDiUnEvento() {
        eventRepository.deleteAll();
        userRepository.deleteAll();

        User creator = new User();
        creator.setName("Luca");
        creator.setLastname("Rossi");
        creator.setEmail("luca@example.com");
        creator.setPassword("Pass123");
        userRepository.save(creator);

        this.userId = creator.getId();

        Event event = new Event();
        event.setTitle("Studio basi di dati");
        event.setDescription("Ripasso SQL");
        event.setDateTime(LocalDateTime.of(2026, 4, 20, 15, 0));
        event.setDuration(2.0);
        event.setLocation("Aula 2");
        event.setTags(List.of("Informatica"));
        event.setMaxParticipants(10);
        event.setStatus("aperto");
        event.setCreator(creator);
        event.addParticipant(creator);

        eventRepository.save(event);
        this.eventId = event.getId();
    }

    @And("l’evento ha il flag di presenza obbligatoria del creatore attivo")
    public void eventoHaIlFlagAttivo() {
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.setCreatorParticipationRequired(true);
        eventRepository.save(event);
    }

    @And("l’evento ha il flag di presenza obbligatoria del creatore disattivo")
    public void eventoHaIlFlagDisattivo() {
        Event event = eventRepository.findById(eventId).orElseThrow();
        event.setCreatorParticipationRequired(false);
        eventRepository.save(event);
    }

    @And("l'evento ha 5 prenotazioni associate nel database")
    public void eventoHaCinquePrenotazioniAssociateNelDatabase() {
        Event event = eventRepository.findById(eventId).orElseThrow();

        for (int i = 1; i <= 5; i++) {
            User user = new User();
            user.setName("Utente" + i);
            user.setLastname("Test");
            user.setEmail("utente" + i + "@example.com");
            user.setPassword("Pass123");
            userRepository.save(user);
            event.addParticipant(user);
        }

        eventRepository.save(event);
        partecipantsBeforeDelete = event.getParticipants().size();
    }

    @And("l’evento ha altri utenti iscritti")
    public void eventoHaAltriUtentiIscritti() {
        Event event = eventRepository.findById(eventId).orElseThrow();

        User user2 = new User();
        user2.setName("Marco");
        user2.setLastname("Verdi");
        user2.setEmail("marco@example.com");
        user2.setPassword("Pass123");
        userRepository.save(user2);

        User user3 = new User();
        user3.setName("Anna");
        user3.setLastname("Bianchi");
        user3.setEmail("anna@example.com");
        user3.setPassword("Pass123");
        userRepository.save(user3);

        event.addParticipant(user2);
        event.addParticipant(user3);
        eventRepository.save(event);

        this.firstOtherUserId = List.of(user2, user3).stream()
                .map(User::getId)
                .min(Long::compareTo)
                .orElseThrow();
    }

    @And("l’evento non ha altri utenti iscritti")
    public void eventoNonHaAltriUtentiIscritti() {
        Event event = eventRepository.findById(eventId).orElseThrow();
        Set<User> onlyCreator = new HashSet<>();
        onlyCreator.add(event.getCreator());
        event.setParticipants(onlyCreator);
        eventRepository.save(event);
    }

    @And("esistono altri utenti iscritti all’evento")
    public void esistonoAltriUtentiIscrittiAllevento() {
        eventoHaAltriUtentiIscritti();
    }

    @When("prova a cancellare l'evento")
    public void provaACancellareLevento() throws Exception {
        result = mockMvc.perform(delete("/events/" + eventId)
                .sessionAttr("userId", userId));
    }

    @Then("dovrebbe ricevere un messaggio di conferma di avvenuta cancellazione")
    public void dovrebbeRicevereMessaggioConferma() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("successfully")));
    }

    @And("il record dell'evento viene rimosso dal database")
    public void eventoVieneRimossoDalDatabase() {
        assertFalse(eventRepository.findById(eventId).isPresent());
    }

    @And("le 5 prenotazioni associate vengono eliminate a cascata")
    public void prenotazioniAssociateEliminateACascata() {
        assertFalse(eventRepository.findById(eventId).isPresent());
        assertTrue(partecipantsBeforeDelete >= 6); // creatore + 5 utenti
    }

    @And("il creatore viene rimosso dall’evento")
    public void creatoreVieneRimossoDallevento() {
        Event event = eventRepository.findById(eventId).orElseThrow();
        boolean isThereCreatore = event.getParticipants().stream()
                .anyMatch(u -> u.getId().equals(userId));
        assertFalse(isThereCreatore);
    }

    @And("l’evento rimane presente nel database")
    public void eventoRimanePresenteNelDatabase() {
        assertTrue(eventRepository.findById(eventId).isPresent());
    }

    @And("le prenotazioni degli altri utenti rimangono attive")
    public void prenotazioniDegliAltriUtentiRimangonoAttive() {
        Event event = eventRepository.findById(eventId).orElseThrow();
        assertTrue(event.getParticipants().size() >= 1);
        assertTrue(event.getParticipants().stream().noneMatch(u -> u.getId().equals(userId)));
    }

    @And("il sistema assegna il ruolo di creatore al primo utente iscritto")
    public void sistemaAssegnaRuoloCreatoreAlPrimoUtenteIscritto() {
        Event event = eventRepository.findById(eventId).orElseThrow();
        assertNotNull(event.getCreator());
        assertEquals(firstOtherUserId, event.getCreator().getId());
    }
}