package com.example.backend.cucumber;

import com.example.backend.model.User;
import com.example.backend.model.UserSettings;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserSettingsRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class editNotificationSettings {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserSettingsRepository settingsRepository;

    private User loggedUser;
    private MockHttpSession session;

    // Variabili di stato che rappresentano il payload della richiesta (di default a
    // true)
    private boolean editActivated;
    private boolean deleteActivated;
    private boolean recommendActivated;

    @Given("l'utente è autenticato e nella sezione impostazioni notifiche")
    public void utenteEAutenticato() {
        settingsRepository.deleteAll();
        settingsRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();

        loggedUser = new User("Mario", "Rossi", "mario.settings@example.com", "Password123!",
                "Milano", List.of("Social"), new ArrayList<>());
        loggedUser = userRepository.saveAndFlush(loggedUser);

        UserSettings settings = new UserSettings(loggedUser.getEmail(), true, true, true);
        settingsRepository.saveAndFlush(settings);

        editActivated = true;
        deleteActivated = true;
        recommendActivated = true;

        session = new MockHttpSession();
        session.setAttribute("userId", loggedUser.getId());
    }

    @Given("le notifiche email per nuove pubblicazioni relative agli interessi\\/tag sono disattivate")
    public void raccomandazioniDisattivateNelDB() {
        UserSettings settings = settingsRepository.findById(loggedUser.getEmail()).orElseThrow();
        settings.setRecomendedEventEmailActivated(false);
        settingsRepository.saveAndFlush(settings);

        recommendActivated = false;
    }

    @When("l'utente attiva l'opzione notifiche email per nuove pubblicazioni")
    public void utenteAttivaRaccomandazioni() {
        recommendActivated = true;
    }

    // ==========================================
    // SCENARIO 2: Modifica Evento
    // ==========================================

    @Given("le notifiche email per modifica evento sono attivate")
    public void modificaAttivataNelDB() {
        UserSettings settings = settingsRepository.findById(loggedUser.getEmail()).orElseThrow();
        settings.setEditEventEmailActivated(true);
        settingsRepository.saveAndFlush(settings);

        editActivated = true;
    }

    @When("l'utente disattiva l'opzione notifiche email per modifica evento")
    public void utenteDisattivaModifica() {
        editActivated = false;
    }

    // ==========================================
    // SCENARIO 3: Cancellazione Evento
    // ==========================================

    @Given("le notifiche email per cancellazione evento sono attivate")
    public void cancellazioneAttivataNelDB() {
        UserSettings settings = settingsRepository.findById(loggedUser.getEmail()).orElseThrow();
        settings.setDeleteEventEmailActivated(true);
        settingsRepository.saveAndFlush(settings);

        deleteActivated = true;
    }

    @When("l'utente disattiva l'opzione notifiche email per cancellazione evento")
    public void utenteDisattivaCancellazione() {
        deleteActivated = false;
    }

    // ==========================================
    // AZIONI CONDIVISE (Salvataggio e Verifica)
    // ==========================================

    @And("salva le modifiche alle impostazioni notifiche")
    public void salvaLeModifiche() throws Exception {
        // Costruiamo il JSON in base allo stato attuale delle nostre 3 variabili
        String payload = String.format("""
                {
                  "editEventEmailActivated": %b,
                  "deleteEventEmailActivated": %b,
                  "recomendedEventEmailActivated": %b
                }
                """, editActivated, deleteActivated, recommendActivated);

        // Chiamiamo l'API esattamente come fa il Frontend
        mockMvc.perform(put("/user/me/settings")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(payload))
                .andExpect(status().isOk());
    }

    @Then("il sistema aggiorna correttamente la preferenza nel database")
    public void verificaAggiornamentoDatabase() {
        // Recuperiamo l'entità fresca dal database
        UserSettings savedSettings = settingsRepository.findById(loggedUser.getEmail()).orElseThrow();

        // Verifichiamo che i valori salvati a DB corrispondano alle modifiche che
        // abbiamo inviato
        assertEquals(editActivated, savedSettings.isEditEventEmailActivated(), "Mismatch su modifica evento");
        assertEquals(deleteActivated, savedSettings.isDeleteEventEmailActivated(), "Mismatch su cancellazione evento");
        assertEquals(recommendActivated, savedSettings.isRecomendedEventEmailActivated(),
                "Mismatch su nuove raccomandazioni");
    }
}