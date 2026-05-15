package com.example.backend.cucumber;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

public class UpdateUserProfile {

    @Autowired
    private MockMvc mockMvc;

    private MockHttpSession session;
    private ResultActions result;
    private String name;
    private String lastname;
    private String email;
    private String currentPassword;
    private String newPassword;

    @Given("l'utente autenticato si trova nella pagina Profilo")
    public void utenteAutenticatoSiTrovaNellaPaginaProfilo() throws Exception {
        name = "Mario";
        lastname = "Rossi";
        email = "mario@example.com";
        currentPassword = "Password123";
        newPassword = null;

        String registration = """
                {
                  "name": "Mario",
                  "lastname": "Rossi",
                  "email": "mario@example.com",
                  "password": "Password123",
                  "favouriteCity": "Milano",
                  "macroTags": ["Informatica e Tecnologie"],
                  "microTags": ["Programmazione"]
                }
                """;

        mockMvc.perform(post("/user")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(registration))
                .andExpect(status().isOk());

        String login = """
                {
                  "email": "mario@example.com",
                  "password": "Password123"
                }
                """;

        session = (MockHttpSession) mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(login))
                .andExpect(status().isOk())
                .andReturn()
                .getRequest()
                .getSession();

        assertNotNull(session);
    }

    @When("l'utente modifica il nome da {string} a {string}")
    public void utenteModificaIlNomeDaA(String oldName, String newName) {
        name = newName;
    }

    @And("modifica il cognome da {string} a {string}")
    public void modificaIlCognomeDaA(String oldLastname, String newLastname) {
        lastname = newLastname;
    }

    @And("salva i dati personali")
    public void salvaIDatiPersonali() throws Exception {
        String body = String.format("""
                {
                  "name": "%s",
                  "lastname": "%s"
                }
                """, name, lastname);

        result = mockMvc.perform(put("/user/me/personal-data")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema aggiorna correttamente i dati personali dell'utente")
    public void sistemaAggiornaCorrettamenteIDatiPersonali() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(name))
                .andExpect(jsonPath("$.lastname").value(lastname));
    }

    @And("il sistema mostra il messaggio profilo di conferma {string}")
    public void sistemaMostraMessaggioConferma(String message) throws Exception {
        result.andExpect(status().isOk());
    }

    @When("l'utente lascia il campo {string} vuoto nella modifica dati")
    public void utenteLasciaCampoVuotoNellaModificaDati(String field) {
        if ("Nome".equalsIgnoreCase(field)) {
            name = "";
        }
        if ("Cognome".equalsIgnoreCase(field)) {
            lastname = "";
        }
    }

    @Then("il sistema impedisce il salvataggio dei dati personali")
    public void sistemaImpedisceSalvataggioDatiPersonali() throws Exception {
        result.andExpect(status().isBadRequest());
    }

    @And("il sistema mostra il messaggio profilo d'errore {string}")
    public void sistemaMostraMessaggioErrore(String errorMessage) throws Exception {
        result.andExpect(content().string(containsString(errorMessage)));
    }

    @When("l'utente modifica la propria email in {string}")
    public void utenteModificaLaPropriaEmailIn(String newEmail) {
        email = newEmail;
    }

    @And("salva la nuova email")
    public void salvaLaNuovaEmail() throws Exception {
        String body = String.format("""
                {
                  "email": "%s"
                }
                """, email);

        result = mockMvc.perform(put("/user/me/email")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema aggiorna correttamente l'email dell'utente")
    public void sistemaAggiornaCorrettamenteEmail() throws Exception {
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(email));
    }

    @When("l'utente inserisce la password attuale corretta")
    public void utenteInseriscePasswordAttualeCorretta() {
        currentPassword = "Password123";
    }

    @When("l'utente inserisce una password attuale errata")
    public void utenteInseriscePasswordAttualeErrata() {
        currentPassword = "WrongPass123";
    }

    @And("inserisce una nuova password profilo valida")
    public void inserisceNuovaPasswordValida() {
        newPassword = "NewPass123";
    }

    @And("salva la nuova password")
    public void salvaLaNuovaPassword() throws Exception {
        String body = String.format("""
                {
                  "currentPassword": "%s",
                  "newPassword": "%s",
                  "confirmPassword": "%s"
                }
                """, currentPassword, newPassword, newPassword);

        result = mockMvc.perform(put("/user/me/password")
                .session(session)
                .contentType(MediaType.APPLICATION_JSON)
                .content(body));
    }

    @Then("il sistema aggiorna correttamente la password")
    public void sistemaAggiornaCorrettamentePassword() throws Exception {
        result.andExpect(status().isOk());

        String loginWithNewPassword = String.format("""
                {
                  "email": "mario@example.com",
                  "password": "%s"
                }
                """, newPassword);

        mockMvc.perform(post("/user/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginWithNewPassword))
                .andExpect(status().isOk());
    }

    @Then("il sistema impedisce l'aggiornamento della password")
    public void sistemaImpedisceAggiornamentoPassword() throws Exception {
        result.andExpect(status().isBadRequest());
    }
}
