package com.example.backend.integration.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class LoginIT extends BaseUserIT {

        @Test
        void loginValid_shouldReturn200() throws Exception {
                String registration = """
                                {
                                  "name": "Mario",
                                  "lastname": "Rossi",
                                  "email": "mario@example.com",
                                  "password": "Password123",
                                  "favouriteCity": "Milano",
                                  "macroTags": ["Informatica"],
                                  "microTags": []
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

                mockMvc.perform(post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(login))
                                .andExpect(status().isOk())
                                .andExpect(content().string(containsString("mario@example.com")));
        }

        @Test
        void loginWrongPassword_shouldReturn400() throws Exception {
                String registration = """
                                {
                                  "name": "Mario",
                                  "lastname": "Rossi",
                                  "email": "mario@example.com",
                                  "password": "Password123",
                                  "favouriteCity": "Milano",
                                  "macroTags": ["Informatica"],
                                  "microTags": []
                                }
                                """;

                mockMvc.perform(post("/user")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(registration))
                                .andExpect(status().isOk());

                String login = """
                                {
                                  "email": "mario@example.com",
                                  "password": "Password999"
                                }
                                """;

                mockMvc.perform(post("/user/login")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(login))
                                .andExpect(status().isBadRequest())
                                .andExpect(content().string(containsString("Credenziali invalide")));
        }
}