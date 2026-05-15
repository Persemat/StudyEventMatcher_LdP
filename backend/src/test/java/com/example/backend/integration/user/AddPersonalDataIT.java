package com.example.backend.integration.user;

import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.transaction.annotation.Transactional;

public class AddPersonalDataIT extends BaseUserIT {

    private static final String REGISTER_URL = "/user";

    @Test
    @Transactional
    void registration_ShouldSucceed_WithValidData() throws Exception {
        String json = """
                {
                    "name": "Mario",
                    "lastname": "Rossi",
                    "email": "mario.rossi@example.com",
                    "password": "Password123!",
                    "favouriteCity": "Milano",
                    "macroTags": ["Informatica e Tecnologie"],
                    "microTags": ["Java", "Spring"]
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("mario.rossi@example.com"))
                .andExpect(jsonPath("$.favouriteCity").value("Milano"));

        User savedUser = userRepository.findByEmail("mario.rossi@example.com").orElseThrow();
        assertEquals("Mario", savedUser.getName());
        assertEquals(1, savedUser.getMacroTags().size());
    }

    @Test
    void registration_ShouldFail_WhenNameOrLastnameMissing() throws Exception {
        String noNameJson = """
                {
                    "name": "",
                    "lastname": "Rossi",
                    "email": "error@example.com",
                    "password": "Password123!",
                    "favouriteCity": "Roma",
                    "macroTags": ["Psicologia"]
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noNameJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Nome obbligatorio"));

        String noLastnameJson = """
                {
                    "name": "Mario",
                    "lastname": null,
                    "email": "error2@example.com",
                    "password": "Password123!",
                    "favouriteCity": "Roma",
                    "macroTags": ["Psicologia"]
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noLastnameJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Cognome obbligatorio"));
    }

    @Test
    void registration_ShouldFail_WhenCityMissing() throws Exception {
        String noCityJson = """
                {
                    "name": "Mario",
                    "lastname": "Rossi",
                    "email": "nocity@example.com",
                    "password": "Password123!",
                    "favouriteCity": " ",
                    "macroTags": ["Psicologia"]
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noCityJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Bisogna indicare una città preferita"));
    }

    @Test
    void registration_ShouldFail_WhenMacroTagsEmptyOrTooMany() throws Exception {
        String noTagsJson = """
                {
                    "name": "Mario",
                    "lastname": "Rossi",
                    "email": "notags@example.com",
                    "password": "Password123!",
                    "favouriteCity": "Milano",
                    "macroTags": []
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(noTagsJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Almeno una macro area di interesse deve essere indicata"));

        String tooManyMacrosJson = """
                {
                    "name": "Mario",
                    "lastname": "Rossi",
                    "email": "toomany@example.com",
                    "password": "Password123!",
                    "favouriteCity": "Milano",
                    "macroTags": ["T1", "T2", "T3", "T4"]
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tooManyMacrosJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Si possono indicare al massimo 3 aree di interesse generale "));
    }

    @Test
    void registration_ShouldFail_WhenTooManyMicroTags() throws Exception {
        String tooManyMicrosJson = """
                {
                    "name": "Mario",
                    "lastname": "Rossi",
                    "email": "toomanymicro@example.com",
                    "password": "Password123!",
                    "favouriteCity": "Milano",
                    "macroTags": ["Informatica"],
                    "microTags": ["m1", "m2", "m3", "m4", "m5", "m6"]
                }
                """;

        mockMvc.perform(post(REGISTER_URL)
                .contentType(MediaType.APPLICATION_JSON)
                .content(tooManyMicrosJson))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("Si possono indicare al massimo 5 argomenti di interesse specifici "));
    }
}