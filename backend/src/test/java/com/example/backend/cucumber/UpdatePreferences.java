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
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
public class UpdatePreferences {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    private User loggedUser;
    private ResultActions result;

    private String currentCity = "Milano";
    private List<String> currentMacroTags = new ArrayList<>(List.of("Generico"));
    private List<String> currentMicroTags = new ArrayList<>();

    @Given("l'utente è autenticato per la modifica preferenze")
    public void utenteAutenticato() {
        userRepository.deleteAll();
        loggedUser = new User();
        loggedUser.setName("Mario");
        loggedUser.setLastname("Rossi");
        loggedUser.setEmail("mario.test@example.com");
        loggedUser.setPassword("Password123!");
        loggedUser.setFavouriteCity(currentCity);
        loggedUser.setMacroTags(new ArrayList<>(List.of("Generico")));
        loggedUser.setMicroTags(new ArrayList<>());
        loggedUser = userRepository.saveAndFlush(loggedUser);
    }

    @And("l'utente si trova nella pagina Profilo")
    public void paginaProfilo() {
        // Step puramente descrittivo (BDD), non esegue operazioni di backend
    }

    @And("l'utente ha inizialmente il macro tag {string} e il micro tag {string}")
    public void initTags(String macro, String micro) {
        currentMacroTags = new ArrayList<>(List.of(macro));
        currentMicroTags = new ArrayList<>(List.of(micro));
        updateDbUser();
    }

    @And("l'utente ha inizialmente i macro tag {string} e {string}")
    public void initTwoMacros(String m1, String m2) {
        currentMacroTags = new ArrayList<>(List.of(m1, m2));
        currentMicroTags = new ArrayList<>();
        updateDbUser();
    }

    @And("l'utente ha già impostato come città preferita {string}")
    public void initCity(String city) {
        currentCity = city;
        updateDbUser();
    }

    @And("l'utente ha inizialmente il macro tag {string} e nessun micro tag")
    public void initMacroNoMicro(String macro) {
        currentMacroTags = new ArrayList<>(List.of(macro));
        currentMicroTags = new ArrayList<>();
        updateDbUser();
    }

    @When("l'utente sostituisce i macro tag con {string} e i micro tag con {string}")
    public void changeTags(String macro, String micro) {
        currentMacroTags = new ArrayList<>(List.of(macro));
        currentMicroTags = new ArrayList<>(List.of(micro));
    }

    @When("l'utente rimuove il tag {string} lasciando solo {string}")
    public void removeTag(String removed, String kept) {
        currentMacroTags.remove(removed);
        if (!currentMacroTags.contains(kept)) {
            currentMacroTags.add(kept);
        }
    }

    @When("l'utente modifica la città preferita in {string}")
    public void changeCity(String newCity) {
        currentCity = newCity;
    }

    @When("l'utente svuota la lista dei macro tag")
    public void emptyMacro() {
        currentMacroTags.clear();
    }

    @When("l'utente rimuove la città preferita senza sostituirla")
    public void emptyCity() {
        currentCity = "";
    }

    @And("salva le modifiche alle preferenze")
    public void salvaModifiche() throws Exception {
        String json = String.format("""
                {
                    "favouriteCity": "%s",
                    "macroTags": %s,
                    "microTags": %s
                }
                """, currentCity, toJsonArray(currentMacroTags), toJsonArray(currentMicroTags));

        result = mockMvc.perform(put("/user/me/preferences")
                .sessionAttr("userId", loggedUser.getId())
                .contentType(MediaType.APPLICATION_JSON)
                .content(json));
    }

    @Then("il sistema associa correttamente i tag {string} e {string}")
    public void verifyTags(String macro, String micro) {
        User updated = userRepository.findById(loggedUser.getId()).orElseThrow();
        assertTrue(updated.getMacroTags().contains(macro));
        assertTrue(updated.getMicroTags().contains(micro));
    }

    @Then("il sistema rimuove correttamente il tag {string}")
    public void verifyRemovedTag(String removedMacro) {
        User updated = userRepository.findById(loggedUser.getId()).orElseThrow();
        assertFalse(updated.getMacroTags().contains(removedMacro));
    }

    @Then("il sistema aggiorna correttamente la città in {string}")
    public void verifyCity(String expectedCity) {
        userRepository.flush();
        User updated = userRepository.findById(loggedUser.getId()).orElseThrow();
        assertEquals(expectedCity, updated.getFavouriteCity());
    }

    @And("l'operazione di aggiornamento ha successo")
    public void operazioneSuccesso() throws Exception {
        result.andExpect(status().isOk());
    }

    @Then("il sistema mostra l'errore {string} nella pagina")
    public void mostraErroreLocale(String messaggioAtteso) throws Exception {
        assertNotNull(result,
                "Il risultato della chiamata HTTP (result) è null! Controlla che lo step precedente abbia eseguito la richiesta.");

        result.andExpect(status().isBadRequest())
                .andExpect(org.springframework.test.web.servlet.result.MockMvcResultMatchers.content()
                        .string(org.hamcrest.Matchers.containsString(messaggioAtteso)));
    }

    private void updateDbUser() {
        loggedUser.setFavouriteCity(currentCity);
        loggedUser.setMacroTags(new ArrayList<>(currentMacroTags));
        loggedUser.setMicroTags(new ArrayList<>(currentMicroTags));
        userRepository.saveAndFlush(loggedUser);
    }

    private String toJsonArray(List<String> list) {
        if (list == null || list.isEmpty())
            return "[]";
        return "[\"" + String.join("\",\"", list) + "\"]";
    }

}