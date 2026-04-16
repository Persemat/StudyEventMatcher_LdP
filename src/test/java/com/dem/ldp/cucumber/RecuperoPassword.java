package com.dem.ldp.cucumber;

import com.dem.ldp.model.PasswordResetToken;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.PasswordResetTokenRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.hamcrest.Matchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class RecuperoPassword {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UtenteRepository utenteRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String email;
    private String codice;
    private String nuovaPassword;
    private ResultActions result;

    @Given("esiste un utente registrato per il recupero password")
    public void esisteUnUtenteRegistratoPerIlRecuperoPassword() {
        email = "utente_" + UUID.randomUUID() + "@example.com";

        Utente utente = new Utente("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);
    }

    @Given("non esiste alcun utente con quell'email per il recupero password")
    public void nonEsisteAlcunUtenteConQuellEmailPerIlRecuperoPassword() {
        email = "utente_" + UUID.randomUUID() + "@example.com";
    }

    @When("l'utente richiede il codice di recupero password")
    public void lUtenteRichiedeIlCodiceDiRecuperoPassword() throws Exception {
        String json = """
                {
                  "email": "%s"
                }
                """.formatted(email);

        result = mockMvc.perform(post("/password-reset/forgot")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Then("il sistema invia il codice di recupero")
    public void ilSistemaInviaIlCodiceDiRecupero() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Codice di recupero inviato via email")));
    }

    @Then("il sistema rifiuta la richiesta di recupero password")
    public void ilSistemaRifiutaLaRichiestaDiRecuperoPassword() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @Given("esiste un utente con un codice di recupero valido")
    public void esisteUnUtenteConUnCodiceDiRecuperoValido() {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        codice = "123456";
        nuovaPassword = "NuovaPass1";

        Utente utente = new Utente("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCodice(codice);
        token.setCreatoIl(LocalDateTime.now().minusMinutes(1));
        token.setScadenza(LocalDateTime.now().plusMinutes(10));
        token.setUsato(false);
        passwordResetTokenRepository.save(token);
    }

    @Given("esiste un utente registrato per il reset password")
    public void esisteUnUtenteRegistratoPerIlResetPassword() {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        nuovaPassword = "NuovaPass1";

        Utente utente = new Utente("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);
    }

    @Given("esiste un utente con un codice di recupero scaduto")
    public void esisteUnUtenteConUnCodiceDiRecuperoScaduto() {
        email = "utente_" + UUID.randomUUID() + "@example.com";
        codice = "123456";
        nuovaPassword = "NuovaPass1";

        Utente utente = new Utente("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        utenteRepository.save(utente);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCodice(codice);
        token.setCreatoIl(LocalDateTime.now().minusMinutes(20));
        token.setScadenza(LocalDateTime.now().minusMinutes(1));
        token.setUsato(false);
        passwordResetTokenRepository.save(token);
    }

    @When("l'utente conferma il reset password con un nuovo valore valido")
    public void lUtenteConfermaIlResetPasswordConUnNuovoValoreValido() throws Exception {
        String json = """
                {
                  "email": "%s",
                  "codice": "%s",
                  "nuovaPassword": "%s"
                }
                """.formatted(email, codice, nuovaPassword);

        result = mockMvc.perform(post("/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @When("l'utente conferma il reset password con un codice non valido")
    public void lUtenteConfermaIlResetPasswordConUnCodiceNonValido() throws Exception {
        String json = """
                {
                  "email": "%s",
                  "codice": "999999",
                  "nuovaPassword": "%s"
                }
                """.formatted(email, nuovaPassword);

        result = mockMvc.perform(post("/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Then("il sistema aggiorna la password")
    public void ilSistemaAggiornaLaPassword() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(content().string(Matchers.containsString("Password aggiornata con successo")));
    }

    @Then("il sistema rifiuta la richiesta di reset password")
    public void ilSistemaRifiutaLaRichiestaDiResetPassword() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @Then("il sistema mostra il messaggio {string}")
    public void ilSistemaMostraIlMessaggio(String messaggio) throws Exception {
        result.andExpect(content().string(Matchers.containsString(messaggio)));
    }
}