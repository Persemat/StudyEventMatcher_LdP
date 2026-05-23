package com.example.backend.integration.user;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.junit.jupiter.api.Assertions.*;

@Tag("integration")
@TestPropertySource(properties = "app.security.require-email-verification=false")
public class LogoutIT extends BaseUserIT {

  @Test
  void fullFlow_loginLogout_accessDenied() throws Exception {

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

    MockHttpSession session = (MockHttpSession) mockMvc.perform(post("/user/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(login))
        .andExpect(status().isOk())
        .andReturn()
        .getRequest()
        .getSession();

    assertNotNull(session);

    mockMvc.perform(get("/user/me")
        .session(session))
        .andExpect(status().isOk());

    mockMvc.perform(post("/user/logout")
        .session(session))
        .andExpect(status().isOk());

    mockMvc.perform(get("/user/me")
        .session(session))
        .andExpect(status().is4xxClientError());

    assertTrue(session.isInvalid(), "La sessione deve essere segnata come invalida dopo il logout.");
  }
}