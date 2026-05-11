package com.example.backend.cucumber;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;

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

public class PasswordRecovery {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private String email;
    private String code;
    private String newPassword;
    private ResultActions result;

    @Given("esiste un utente registrato per il recupero password")
    public void esisteUnUtenteRegistratoPerIlRecuperoPassword() {
        email = "user_" + UUID.randomUUID() + "@example.com";

        User user = new User("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        userRepository.save(user);
    }

    @Given("non esiste alcun utente con quell'email per il recupero password")
    public void nonEsisteAlcunUtenteConQuellEmailPerIlRecuperoPassword() {
        email = "user_" + UUID.randomUUID() + "@example.com";
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
                .andExpect(content().string(Matchers.containsString("Codice di reset della password inviato per email")));
    }

    @Then("il sistema rifiuta la richiesta di recupero password")
    public void ilSistemaRifiutaLaRichiestaDiRecuperoPassword() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @Given("esiste un utente con un codice di recupero valido")
    public void esisteUnUtenteConUnCodiceDiRecuperoValido() {
        email = "user_" + UUID.randomUUID() + "@example.com";
        code = "123456";
        newPassword = "NuovaPass1";

        User utente = new User("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        userRepository.save(utente);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);
    }

    @Given("esiste un utente registrato per il reset password")
    public void esisteUnUtenteRegistratoPerIlResetPassword() {
        email = "user_" + UUID.randomUUID() + "@example.com";
        newPassword = "NewPass1";

        User user = new User("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        userRepository.save(user);
    }

    @Given("esiste un utente con un codice di recupero scaduto")
    public void esisteUnUtenteConUnCodiceDiRecuperoScaduto() {
        email = "user_" + UUID.randomUUID() + "@example.com";
        code = "123456";
        newPassword = "NewPass1";

        User user = new User("Mario", "Rossi", email, passwordEncoder.encode("Password123"));
        userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail(email);
        token.setCode(code);
        token.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);
    }

    @When("l'utente conferma il reset password con un nuovo valore valido")
    public void lUtenteConfermaIlResetPasswordConUnNuovoValoreValido() throws Exception {
        String json = """
                {
                  "email": "%s",
                  "code": "%s",
                  "newPassword": "%s"
                }
                """.formatted(email, code, newPassword);

        result = mockMvc.perform(post("/password-reset/confirm")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @When("l'utente conferma il reset password con un codice non valido")
    public void lUtenteConfermaIlResetPasswordConUnCodiceNonValido() throws Exception {
        String json = """
                {
                  "email": "%s",
                  "code": "999999",
                  "newPassword": "%s"
                }
                """.formatted(email, newPassword);

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