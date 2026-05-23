package com.example.backend.cucumber;

import com.example.backend.model.Event;
import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.ArrayList;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class EventUpdateNotification {

    @Autowired
    private org.springframework.test.web.servlet.MockMvc mockMvc;

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

    @Given("esiste l'evento {string} con data {string} e luogo {string}")
    public void esisteEventoConDataELuogo(String title, String dateTime, String location) {
        eventRepository.deleteAll();
        eventRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        clearInvocations(mailSender);

        creator = new User("Mario", "Rossi", "mario.update@example.com", "Password123",
                "Milano", List.of("Informatica e Tecnologie"), new ArrayList<>());
        creator = userRepository.saveAndFlush(creator);

        // CORREZIONE DINAMICA DELLA DATA:
        // Se arriva la data fissa del feature che è passata, la spostiamo a 3 mesi nel
        // futuro
        LocalDateTime eventDateTime = LocalDateTime.parse(dateTime);
        if (eventDateTime.isBefore(LocalDateTime.now().plusHours(24))) {
            eventDateTime = LocalDateTime.now().plusMonths(3);
        }

        event = new Event();
        event.setTitle(title);
        event.setDateTime(eventDateTime); // <--- Ora è sempre garantita nel futuro
        event.setDuration(2.0);
        event.setLocation(location);
        event.setTags(List.of(new EventTag("Informatica e Tecnologie", "Programmazione")));
        event.setDescription("Studio progetto");
        event.setMaxParticipants(10);
        event.setCreatorParticipationRequired(true);
        event.setStatus("open");
        event.setCreator(creator);
        event.addParticipant(creator);
        event = eventRepository.saveAndFlush(event);
    }

    @And("l'utente è iscritto all'evento {string}")
    public void utenteIscrittoEvento(String title) {
        enrolledUser = new User("Anna", "Bianchi", "anna.update@example.com", "Password123",
                "Torino", List.of("Informatica e Tecnologie"), new ArrayList<>());
        enrolledUser = userRepository.saveAndFlush(enrolledUser);

        event.addParticipant(enrolledUser);
        event = eventRepository.saveAndFlush(event);
    }

    @And("un utente non è iscritto all'evento {string}")
    public void utenteNonIscrittoEvento(String title) {
        notEnrolledUser = new User("Luca", "Verdi", "luca.update@example.com", "Password123",
                "Napoli", new ArrayList<>(), new ArrayList<>());
        notEnrolledUser = userRepository.saveAndFlush(notEnrolledUser);
    }

    @When("il creatore modifica il luogo dell'evento {string} in {string}")
    public void creatoreModificaLuogoEvento(String title, String newLocation) throws Exception {
        String json = String.format(Locale.ROOT, """
                {
                  "title": "%s",
                  "dateTime": "%s",
                  "duration": %.1f,
                  "location": "%s",
                  "tags": [
                    { "macroTag": "Informatica e Tecnologie", "microTag": "Programmazione" }
                  ],
                  "description": "%s",
                  "maxParticipants": %d,
                  "creatorParticipationRequired": %s
                }
                """,
                event.getTitle(),
                event.getDateTime().toString(), // <--- Usa la data corretta memorizzata nell'oggetto
                event.getDuration(),
                newLocation,
                event.getDescription(),
                event.getMaxParticipants(),
                String.valueOf(event.isCreatorParticipationRequired()));

        result = mockMvc.perform(put("/events/" + event.getId())
                .sessionAttr("userId", creator.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk());
    }

    @Then("il sistema invia un'email all'utente iscritto")
    public void sistemaInviaEmailUtenteIscritto() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(captor.capture());

        boolean sentToEnrolledUser = captor.getAllValues().stream()
                .map(SimpleMailMessage::getTo)
                .filter(to -> to != null && to.length > 0)
                .anyMatch(to -> enrolledUser.getEmail().equals(to[0]));

        assertTrue(sentToEnrolledUser);
    }

    @And("l'email contiene il messaggio {string}")
    public void emailContieneMessaggio(String expectedMessage) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(captor.capture());

        boolean messageFound = captor.getAllValues().stream()
                .map(SimpleMailMessage::getText)
                .anyMatch(text -> text != null && text.contains(expectedMessage));

        assertTrue(messageFound);
    }

    @Then("il sistema non invia alcuna email a quell'utente")
    public void sistemaNonInviaEmailAQuellUtente() {
        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @And("l'utente visualizza i dettagli dell'evento {string}")
    public void utenteVisualizzaDettagliEvento(String title) throws Exception {
        result = mockMvc.perform(get("/events")
                .sessionAttr("userId", enrolledUser.getId())
                .contentType(MediaType.APPLICATION_JSON));
    }

    @Then("il sistema mostra il luogo aggiornato {string}")
    public void sistemaMostraLuogoAggiornato(String updatedLocation) throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(containsString(updatedLocation)));
    }
}