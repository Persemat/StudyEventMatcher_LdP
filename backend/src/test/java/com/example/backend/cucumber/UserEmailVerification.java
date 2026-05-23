package com.example.backend.cucumber;

import com.example.backend.model.UserVerification;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserVerificationRepository;
import com.example.backend.service.UserService;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.clearInvocations;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class UserEmailVerification {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserVerificationRepository userVerificationRepository;

    @Autowired
    private JavaMailSender mailSender;

    // Iniettiamo il service per forzarlo a TRUE a runtime
    @Autowired
    private UserService userService;

    private ResultActions result;
    private String registrationJson;
    private final String testEmail = "test.verification@example.com";
    private String extractedCode;

    // =========================================================================
    // SCENARIO 1: Invio codice di conferma dopo registrazione
    // =========================================================================

    @Given("l'utente si trova nella pagina di registrazione")
    public void utenteNellaPaginaDiRegistrazione() {
        // TRUCCO CRUCIALE: Forziamo la variabile booleana del Service a TRUE prima del
        // test
        ReflectionTestUtils.setField(userService, "requireEmailVerification", true);

        userVerificationRepository.deleteAll();
        userVerificationRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();
        clearInvocations(mailSender);
    }

    @And("l'utente inserisce dati validi")
    public void utenteInserisceDatiValidi() {
        registrationJson = """
                {
                  "name": "Valerio",
                  "lastname": "Verdi",
                  "email": "%s",
                  "password": "Password123!",
                  "favouriteCity": "Roma",
                  "macroTags": ["Informatica"],
                  "microTags": []
                }
                """.formatted(testEmail);
    }

    @When("l'utente completa la registrazione")
    public void utenteCompletaLaRegistrazione() throws Exception {
        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registrationJson));
    }

    @Then("il sistema crea l'account in stato “non verificato”")
    public void sistemaCreaAccountNonVerificato() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(resultMatch -> {
                    String jsonResponse = resultMatch.getResponse().getContentAsString();
                    assertTrue(jsonResponse.contains(testEmail),
                            "Il JSON di risposta deve contenere l'email dell'utente");
                });

        // Ora che la proprietà è TRUE, questo record ESISTERÀ al 100%
        UserVerification verification = userVerificationRepository.findById(testEmail)
                .orElseThrow(() -> new AssertionError(
                        "Record di verifica non generato nel database. Verifica l'inizializzazione!"));

        assertFalse(verification.isVerified(), "L'account non dovrebbe essere già verificato!");
    }

    @And("il sistema invia un’email di conferma contenente un codice numerico a 6 cifre")
    public void sistemaInviaEmailConCodice6Cifre() {
        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender, atLeastOnce()).send(captor.capture());

        String emailBody = captor.getValue().getText();
        assertNotNull(emailBody);

        UserVerification verification = userVerificationRepository.findById(testEmail).orElseThrow();
        extractedCode = verification.getToken();

        assertEquals(6, extractedCode.length(), "Il codice deve essere lungo 6 cifre");
        assertTrue(extractedCode.matches("\\d+"), "Il codice deve contenere solo numeri");
        assertTrue(emailBody.contains(extractedCode), "L'email non contiene il codice generato");
    }

    @And("il sistema mostra il messaggio “Registrazione completata. Controlla la tua email e inserisci il codice di conferma”")
    public void sistemaMostraMessaggioRegistrazioneCompletata() throws Exception {
        result.andExpect(status().isOk());
    }

    // =========================================================================
    // SCENARIO 2: Conferma email completata con successo
    // =========================================================================

    @Given("l'utente ha completato la registrazione")
    public void utenteHaCompletatoLaRegistrazione() throws Exception {
        utenteNellaPaginaDiRegistrazione();
        utenteInserisceDatiValidi();
        utenteCompletaLaRegistrazione();
    }

    @And("l'account è in stato “non verificato”")
    public void accountInStatoNonVerificato() {
        UserVerification verification = userVerificationRepository.findById(testEmail).orElseThrow();
        assertFalse(verification.isVerified());
    }

    @And("l'utente ha ricevuto un codice di conferma a 6 cifre via email")
    public void utenteHaRicevutoCodice6Cifre() {
        UserVerification verification = userVerificationRepository.findById(testEmail).orElseThrow();
        extractedCode = verification.getToken();
        assertNotNull(extractedCode);
    }

    @When("l'utente inserisce il codice corretto")
    public void utenteInserisceIlCodiceCorretto() {
        // Codice estratto via DB
    }

    @And("conferma la verifica")
    public void confermaLaVerifica() throws Exception {
        result = mockMvc.perform(get("/user/verify")
                .param("token", extractedCode));
    }

    @Then("il sistema verifica l'indirizzo email dell'utente")
    public void sistemaVerificaIndirizzoEmail() throws Exception {
        result.andExpect(status().isOk());
    }

    @And("il sistema aggiorna lo stato dell'account a “verificato”")
    public void sistemaAggiornaStatoAccountVerificato() {
        UserVerification verification = userVerificationRepository.findById(testEmail).orElseThrow();
        assertTrue(verification.isVerified(), "L'account sul DB deve passare a verified=true");
    }

    @And("il sistema mostra il messaggio “Email confermata con successo”")
    public void sistemaMostraMessaggioEmailConfermata() throws Exception {
        result.andExpect(content().string(containsString("Email confermata con successo!")));
    }

    // =========================================================================
    // SCENARIO 3: Codice di conferma non valido o già utilizzato
    // =========================================================================

    @Given("l'utente ha ricevuto un codice di conferma a 6 cifre")
    public void utenteHaRicevutoCodiceDiConferma() throws Exception {
        utenteHaCompletatoLaRegistrazione();
        utenteHaRicevutoCodice6Cifre();
    }

    @When("l'utente inserisce un codice non valido o già utilizzato")
    public void utenteInserisceCodiceNonValidoOGiaUtilizzato() {
        extractedCode = "999999";
    }

    @Then("il sistema non verifica l'account")
    public void sistemaNonVerificaAccount() throws Exception {
        result.andExpect(status().isBadRequest());

        UserVerification verification = userVerificationRepository.findById(testEmail).orElseThrow();
        assertFalse(verification.isVerified(), "L'account deve rimanere NON verificato");
    }

    @And("il sistema mostra il messaggio d'errore “Codice di conferma non valido o già utilizzato”")
    public void sistemaMostraMessaggioErroreCodiceNonValido() throws Exception {
        result.andExpect(content().string(containsString("Errore di verifica")));
    }
}