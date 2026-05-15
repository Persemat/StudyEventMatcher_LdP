package com.example.backend.cucumber;

import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Given;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.transaction.annotation.Transactional;

@Transactional
public class AddPreferences {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private String name, lastname, email, password, city;
    private List<String> macroTags = new ArrayList<>();
    private List<String> microTags = new ArrayList<>();
    private ResultActions result;

    @Given("il database utenti è pulito")
    public void cleanDb() {
        userRepository.deleteAll();
    }

    @When("inserisco nome {string}, cognome {string}, email {string} e password {string}")
    public void inputBasicData(String n, String l, String e, String p) {
        this.name = n;
        this.lastname = l;
        this.email = e;
        this.password = p;
    }

    @And("scelgo {string} come città preferita")
    public void chooseCity(String city) {
        this.city = city;
    }

    @And("lascio vuoto il campo città preferita")
    public void emptyCity() {
        this.city = "";
    }

    @And("scelgo {string} come macro argomento di interesse")
    @And("scelgo {string} come interesse")
    public void addMacro(String macro) {
        this.macroTags.add(macro);
    }

    @And("non seleziono nessun macro argomento di interesse")
    public void noMacro() {
        this.macroTags = new ArrayList<>();
    }

    @And("scelgo i micro argomenti {string} e {string}")
    public void addMicros(String m1, String m2) {
        this.microTags.addAll(List.of(m1, m2));
    }

    @And("non scelgo alcun micro argomento di interesse")
    public void noMicro() {
        this.microTags = new ArrayList<>();
    }

    @And("confermo la registrazione")
    @And("provo a confermare la registrazione")
    public void submit() throws Exception {
        String json = String.format("""
                {
                    "name": "%s",
                    "lastname": "%s",
                    "email": "%s",
                    "password": "%s",
                    "favouriteCity": "%s",
                    "macroTags": %s,
                    "microTags": %s
                }
                """, name, lastname, email, password, city,
                toJsonList(macroTags), toJsonList(microTags));

        result = mockMvc.perform(post("/user")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Then("il mio account deve essere creato correttamente")
    public void checkCreated() throws Exception {
        result.andExpect(status().isOk());
        assertTrue(userRepository.findByEmail(email).isPresent());
    }

    @And("nel mio profilo la città deve essere {string}")
    public void checkCity(String expectedCity) {
        User u = userRepository.findByEmail(email).orElseThrow();
        assertEquals(expectedCity, u.getFavouriteCity());
    }

    @And("i macro tag devono contenere {string}")
    public void checkMacros(String expectedMacro) {
        User u = userRepository.findByEmail(email).orElseThrow();
        assertTrue(u.getMacroTags().contains(expectedMacro));
    }

    @And("i micro tag devono contenere {string} e {string}")
    public void checkMicros(String m1, String m2) {
        User u = userRepository.findByEmail(email).orElseThrow();
        assertTrue(u.getMicroTags().containsAll(List.of(m1, m2)));
    }

    @And("i micro tag salvati devono rimanere vuoti")
    public void checkMicrosEmpty() {
        User u = userRepository.findByEmail(email).orElseThrow();
        assertTrue(u.getMicroTags().isEmpty());
    }

    @Then("il sistema deve mostrare l'errore {string}")
    public void checkError(String expectedError) throws Exception {
        result.andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString(expectedError)));
    }

    @And("nessun nuovo account deve essere creato")
    public void noAccount() {
        assertFalse(userRepository.findByEmail(email).isPresent());
    }

    private String toJsonList(List<String> list) {
        if (list == null || list.isEmpty())
            return "[]";
        return "[\"" + String.join("\",\"", list) + "\"]";
    }
}
