package com.example.backend.cucumber;

import io.cucumber.java.en.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;

import java.util.List;
import java.util.ArrayList;

import org.springframework.mock.web.MockHttpSession; // Per il check validità
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*; // Tutto JUnit 5
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class Logout {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long loggedUserId;
    private MvcResult result;
    private MockHttpSession activeSession;
    private ResultActions response;

    @Given("l'utente è autenticato e pronto al logout")
    public void autenticazioneUtentePerLogout() throws Exception {
        // Pulizia preventiva per evitare conflitti di email duplicata
        userRepository.deleteAll();

        User user = new User(
                "Marco",
                "Rossi",
                "marco@example.com",
                passwordEncoder.encode("Password123!"),
                "Milano",
                List.of("Tech"),
                new ArrayList<>());
        userRepository.save(user);

        this.loggedUserId = user.getId();

        String loginJson = """
                {
                  "email": "marco@example.com",
                  "password": "Password123!"
                }
                """;

        result = mockMvc.perform(post("/user/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
    }

    @And("l'utente ha una sessione attiva")
    public void controllaSeSessioneAttiva() throws Exception {

        assertEquals(200, result.getResponse().getStatus(), "Il login è fallito, non posso proseguire!");
        // 1. Otteniamo la sessione (senza crearne una nuova)
        activeSession = (MockHttpSession) result.getRequest().getSession(false);

        // 2. Controllo: La sessione deve esistere
        assertNotNull(activeSession, "Errore: La sessione non è stata creata dopo il login!");

        // 3. Controllo: La sessione deve essere valida (non scaduta/invalidata)
        assertFalse((activeSession).isInvalid(), "Errore: La sessione risulta già invalida!");

        // 4. Controllo: Deve contenere lo userId corretto (l'ID di Marco Rossi)
        assertEquals(loggedUserId, activeSession.getAttribute("userId"),
                "Errore: Lo userId in sessione non corrisponde all'utente loggato!");

    }

    @When("l'utente richiede il logout")
    public void utenteRichiedeLogout() throws Exception {
        result = mockMvc.perform(post("/user/logout")
                .session(activeSession)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("il sistema termina la sessione")
    public void sistemaTerminaSessione() {
        // In MockMvc, se la sessione viene invalidata, l'oggetto session esiste ancora
        // nel request ma risulta non valido
        assertTrue(activeSession == null || ((MockHttpSession) activeSession).isInvalid(),
                "La sessione dovrebbe essere nulla o invalida");
    }

    @And("il sistema invalida i token di autenticazione")
    public void sistemaInvalidaToken() throws Exception {

        mockMvc.perform(get("/user/me")
                .session(activeSession))
                .andExpect(status().is4xxClientError());
    }

    @And("il sistema revoca l’accesso alle aree riservate")
    public void sistemaRevocaAccessoAreeRiservate() {
        // Questo step è documentale, la verifica è già stata fatta sopra.

        System.out.println("Accesso revocato verificato con successo.");
    }

    @Given("l'utente non è autenticato per il logout")
    public void utenteNonAutenticatoPerIlLogout() {
        // per simulare l'utente non autenticato è sufficiente non iniettare
        // alcuna sessione in MockMVS
    }

    @When("l'utente prova a effettuare il logout")
    public void utenteNonAutenticatoTentaLogout() throws Exception {
        response = mockMvc.perform(post("/user/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""));

    }

    @Then("il sistema non esegue alcuna operazione")
    public void sistemaRispondeCon200() throws Exception {
        response.andExpect(status().isOk());
        response.andExpect(
                content().string(
                        org.hamcrest.Matchers.containsString("Nessuna sessione attiva, Utente già disconnesso")));
    }

    @And("il sistema mantiene lo stato di utente anonimo")
    public void checkIfSessionIsUserIsAnonimised() throws Exception {
        MvcResult responseReturned = response.andReturn();
        MockHttpSession session = (MockHttpSession) responseReturned.getRequest().getSession(false);
        if (session != null) {
            assertNull(session.getAttribute("userId"), "L'ID utente dovrebbe essere nullo per un utente anonimo");
        }
    }
}