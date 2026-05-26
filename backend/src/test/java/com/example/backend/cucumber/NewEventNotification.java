package com.example.backend.cucumber;

import com.example.backend.model.EventTag;
import com.example.backend.model.User;
import com.example.backend.model.UserSettings;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserSettingsRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class NewEventNotification {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EventRepository eventRepository;

    // AGGIUNTO: Repository per gestire le impostazioni di notifica
    @Autowired
    private UserSettingsRepository settingsRepository;

    @Autowired
    private JavaMailSender mailSender;

    private User interestedUser;
    private User creator;
    private String eventTitle;
    private String eventDateTime;
    private boolean published;
    private final List<EventTag> eventTags = new ArrayList<>();

    @Given("l'utente ha un indirizzo email valido")
    public void utenteHaIndirizzoEmailValido() {
        // Pulizia completa del database prima di iniziare
        eventRepository.deleteAll();
        eventRepository.flush();
        settingsRepository.deleteAll(); // Pulizia delle impostazioni vecchie
        settingsRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        clearInvocations(mailSender);
        eventTags.clear();
        published = false;

        interestedUser = new User("Anna", "Bianchi", "anna.notification@example.com", "Password123!",
                "Milano", new ArrayList<>(), new ArrayList<>());
        interestedUser = userRepository.saveAndFlush(interestedUser);

        // AGGIUNTO: Attiviamo le notifiche per l'utente interessato nel DB
        UserSettings settings = new UserSettings(interestedUser.getEmail(), true, true, true);
        settingsRepository.saveAndFlush(settings);

        creator = new User("Marco", "Rossi", "marco.notification@example.com", "Password123!",
                "Roma", new ArrayList<>(), new ArrayList<>());
        creator = userRepository.saveAndFlush(creator);
    }

    @And("l'utente ha tra i propri interessi il tag {string}")
    public void utenteHaTraInteressiTag(String tagLabel) {
        EventTag tag = parseTag(tagLabel);
        interestedUser.getMacroTags().add(tag.getMacroTag());
        if (tag.getMicroTag() != null && !tag.getMicroTag().isBlank()) {
            interestedUser.getMicroTags().add(tag.getMicroTag());
        }
        interestedUser = userRepository.saveAndFlush(interestedUser);
    }

    @When("viene pubblicato l'evento {string} con data {string}")
    public void vienePubblicatoEventoConData(String title, String dateTime) {
        eventTitle = title;
        eventDateTime = dateTime;
    }

    @And("l'evento {string} ha il tag {string}")
    public void eventoHaIlTag(String title, String tagLabel) {
        eventTags.add(parseTag(tagLabel));
    }

    @Then("il sistema invia un'email all'utente")
    public void sistemaInviaEmailUtente() throws Exception {
        publishEventIfNeeded();

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        SimpleMailMessage message = captor.getValue();
        assertTrue(message.getTo() != null && message.getTo().length > 0);
        assertEquals(interestedUser.getEmail(), message.getTo()[0]);
    }

    @And("l'email contiene il titolo {string}")
    public void emailContieneTitolo(String expectedTitle) {
        assertSentEmailContains(expectedTitle);
    }

    @And("l'email contiene la data e ora {string}")
    public void emailContieneData(String expectedDate) {
        assertSentEmailContains(expectedDate);
    }

    @Then("il sistema non invia alcuna email all'utente")
    public void sistemaNonInviaEmailUtente() throws Exception {
        publishEventIfNeeded();

        verify(mailSender, never()).send(any(SimpleMailMessage.class));
    }

    @Then("il sistema invia una sola email all'utente")
    public void sistemaInviaUnaSolaEmailUtente() throws Exception {
        sistemaInviaEmailUtente();
    }

    private void publishEventIfNeeded() throws Exception {
        if (published) {
            return;
        }

        String tagsJson = eventTags.stream()
                .map(tag -> String.format("""
                        { "macroTag": "%s", "microTag": %s }
                        """, tag.getMacroTag(), tag.getMicroTag() == null ? "null" : "\"" + tag.getMicroTag() + "\""))
                .toList()
                .toString();

        String body = String.format("""
                {
                  "title": "%s",
                  "dateTime": "%s",
                  "duration": 2.0,
                  "location": "Milano (MI)-Aula 1",
                  "latitude": 45.4642,
                  "longitude": 9.1900,
                  "tags": %s,
                  "description": "Evento di test",
                  "maxParticipants": 10,
                  "creatorParticipationRequired": true
                }
                """, eventTitle, eventDateTime, tagsJson);

        mockMvc.perform(post("/events")
                .sessionAttr("userId", creator.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(body))
                .andExpect(status().isOk());

        published = true;
    }

    private EventTag parseTag(String tagLabel) {
        String[] parts = tagLabel.split("\\s*>\\s*", 2);
        if (parts.length == 2) {
            return new EventTag(parts[0], parts[1]);
        }
        return new EventTag(tagLabel, null);
    }

    private void assertSentEmailContains(String expectedText) {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, times(1)).send(captor.capture());

        assertTrue(captor.getValue().getText() != null && captor.getValue().getText().contains(expectedText));
    }
}