package com.dem.ldp.cucumber;

import io.cucumber.java.en.*;
import jakarta.servlet.http.HttpSession;
import jakarta.servlet.http.Cookie;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.mock.web.MockHttpSession; // Per il check validità
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*; // Tutto JUnit 5
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class Logout {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private Long loggedUserId;
    private MvcResult result;
    private MockHttpSession sessioneAttiva;
    private ResultActions response;

    @Given("l'utente è autenticato e pronto al logout")
    public void autenticazioneUtentePerLogout() throws Exception {
        // Pulizia preventiva per evitare conflitti di email duplicata
        utenteRepository.deleteAll();

        Utente utenteFinto = new Utente();
        utenteFinto.setNome("Marco");
        utenteFinto.setCognome("Rossi");
        utenteFinto.setEmail("marco@example.com");
        utenteFinto.setPassword(passwordEncoder.encode("Password123!"));
        utenteRepository.save(utenteFinto);

        this.loggedUserId = utenteFinto.getId();

        String loginJson = """
                {
                  "email": "marco@example.com",
                  "password": "Password123!"
                }
                """;

        result = mockMvc.perform(post("/Utenti/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(loginJson))
                .andExpect(status().isOk())
                .andReturn();
    }

    @And("l'utente ha una sessione attiva")
    public void controllaSeSessioneAttiva() throws Exception {

        assertEquals(200, result.getResponse().getStatus(), "Il login è fallito, non posso proseguire!");
        // 1. Otteniamo la sessione (senza crearne una nuova)
        sessioneAttiva = (MockHttpSession) result.getRequest().getSession(false);

        // 2. Controllo: La sessione deve esistere
        assertNotNull(sessioneAttiva, "Errore: La sessione non è stata creata dopo il login!");

        // 3. Controllo: La sessione deve essere valida (non scaduta/invalidata)
        assertFalse((sessioneAttiva).isInvalid(), "Errore: La sessione risulta già invalida!");

        // 4. Controllo: Deve contenere lo userId corretto (l'ID di Marco Rossi)
        assertEquals(loggedUserId, sessioneAttiva.getAttribute("userId"),
                "Errore: Lo userId in sessione non corrisponde all'utente loggato!");

    }

    @When("l'utente richiede il logout")
    public void utenteRichiedeLogout() throws Exception {
        result = mockMvc.perform(post("/Utenti/logout")
                .session(sessioneAttiva)
                .contentType(MediaType.APPLICATION_JSON)
                .content(""))
                .andExpect(status().isOk())
                .andReturn();
    }

    @Then("il sistema termina la sessione")
    public void sistemaTerminaSessione() {
        // In MockMvc, se la sessione viene invalidata, l'oggetto session esiste ancora
        // nel request ma risulta non valido
        assertTrue(sessioneAttiva == null || ((MockHttpSession) sessioneAttiva).isInvalid(),
                "La sessione dovrebbe essere nulla o invalida");
    }

    @And("il sistema invalida i token di autenticazione")
    public void sistemaInvalidaToken() throws Exception {

        mockMvc.perform(get("/Utenti/me")
                .session(sessioneAttiva))
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
        response = mockMvc.perform(post("/Utenti/logout")
                .contentType(MediaType.APPLICATION_JSON)
                .content(""));

    }

    @Then("il sistema non esegue alcuna operazione")
    public void sistemaRispondeCon200() throws Exception {
        response.andExpect(status().isOk());
        response.andExpect(
                content().string(org.hamcrest.Matchers.containsString("Nessuna sessione attiva, utente già sloggato")));
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