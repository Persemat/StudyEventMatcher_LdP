package com.example.backend.integration.user;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.context.TestPropertySource;

@TestPropertySource(properties = "app.security.require-email-verification=false")
public class UpdateUserProfileIT extends BaseUserIT {

  @Test
  void updatePersonalData_shouldReturnUpdatedUser_whenValid() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/personal-data")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "Marco",
              "lastname": "Bianchi"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.name").value("Marco"))
        .andExpect(jsonPath("$.lastname").value("Bianchi"))
        .andExpect(jsonPath("$.email").value("mario@example.com"));
  }

  @Test
  void updatePersonalData_shouldReturn400_whenRequiredFieldsAreMissing() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/personal-data")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "name": "",
              "lastname": "Rossi"
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Campi obbligatori mancanti")));
  }

  @Test
  void updateEmail_shouldReturnUpdatedUser_whenValid() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/email")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "marco@example.com"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("marco@example.com"));
  }

  @Test
  void updateEmail_shouldReturn400_whenEmailIsInvalid() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/email")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "marcoexample.com"
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Email non valida")));
  }

  @Test
  void updateEmail_shouldReturn400_whenEmailAlreadyExists() throws Exception {
    register("luca@example.com", "Password123");
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/email")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "email": "luca@example.com"
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Email gi")));
  }

  @Test
  void updatePassword_shouldReturn200_whenCurrentPasswordIsCorrect() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/password")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "currentPassword": "Password123",
              "newPassword": "NewPass123",
              "confirmPassword": "NewPass123"
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.email").value("mario@example.com"));

    login("mario@example.com", "NewPass123")
        .andExpect(status().isOk());
  }

  @Test
  void updatePassword_shouldReturn400_whenCurrentPasswordIsWrong() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/password")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "currentPassword": "WrongPass123",
              "newPassword": "NewPass123",
              "confirmPassword": "NewPass123"
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Password attuale non corretta")));
  }

  @Test
  void updatePassword_shouldReturn400_whenNewPasswordIsWeak() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/password")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "currentPassword": "Password123",
              "newPassword": "password",
              "confirmPassword": "password"
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("La password deve contenere almeno una maiuscola")));
  }

  @Test
  void updatePreferences_shouldReturnUpdatedUser_whenValid() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/preferences")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "favouriteCity": "Roma",
              "macroTags": ["Psicologia", "Medicina"],
              "microTags": ["Psicoanalisi"]
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.favouriteCity").value("Roma"))
        .andExpect(jsonPath("$.macroTags.length()").value(2))
        .andExpect(jsonPath("$.macroTags[0]").value("Psicologia"))
        .andExpect(jsonPath("$.microTags.length()").value(1))
        .andExpect(jsonPath("$.microTags[0]").value("Psicoanalisi"));
  }

  @Test
  void updatePreferences_shouldReturn400_whenCityIsMissing() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/preferences")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "favouriteCity": "   ",
              "macroTags": ["Psicologia"],
              "microTags": []
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Bisogna indicare una città preferita")));
  }

  @Test
  void updatePreferences_shouldReturn400_whenMacroTagsAreEmpty() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/preferences")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "favouriteCity": "Milano",
              "macroTags": [],
              "microTags": ["Java"]
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Almeno una macro area di interesse deve essere indicata")));
  }

  @Test
  void updatePreferences_shouldReturn400_whenTooManyMacroTags() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/preferences")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "favouriteCity": "Milano",
              "macroTags": ["T1", "T2", "T3", "T4"],
              "microTags": []
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(content().string(containsString("Si possono indicare al massimo 3 aree di interesse generale")));
  }

  @Test
  void updatePreferences_shouldReturn400_whenTooManyMicroTags() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/preferences")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "favouriteCity": "Milano",
              "macroTags": ["Informatica"],
              "microTags": ["m1", "m2", "m3", "m4", "m5", "m6"]
            }
            """))
        .andExpect(status().isBadRequest())
        .andExpect(
            content().string(containsString("Si possono indicare al massimo 5 argomenti di interesse specifici")));
  }

  @Test
  void updateNotificationSettings_shouldReturnUpdatedSettings_whenValid() throws Exception {
    MockHttpSession session = registerAndLogin("mario@example.com", "Password123");

    mockMvc.perform(put("/user/me/settings")
        .session(session)
        .contentType(MediaType.APPLICATION_JSON)
        .content("""
            {
              "editEventEmailActivated": false,
              "deleteEventEmailActivated": false,
              "recomendedEventEmailActivated": true
            }
            """))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.editEventEmailActivated").value(false))
        .andExpect(jsonPath("$.deleteEventEmailActivated").value(false))
        .andExpect(jsonPath("$.recomendedEventEmailActivated").value(true));
  }

  private MockHttpSession registerAndLogin(String email, String password) throws Exception {
    register(email, password);
    MockHttpSession session = (MockHttpSession) login(email, password)
        .andReturn()
        .getRequest()
        .getSession();
    assertNotNull(session);
    return session;
  }

  private void register(String email, String password) throws Exception {
    String body = String.format("""
        {
          "name": "Mario",
          "lastname": "Rossi",
          "email": "%s",
          "password": "%s",
          "favouriteCity": "Milano",
          "macroTags": ["Informatica e Tecnologie"],
          "microTags": ["Programmazione"]
        }
        """, email, password);

    mockMvc.perform(post("/user")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body))
        .andExpect(status().isOk());
  }

  private org.springframework.test.web.servlet.ResultActions login(String email, String password) throws Exception {
    String body = String.format("""
        {
          "email": "%s",
          "password": "%s"
        }
        """, email, password);

    return mockMvc.perform(post("/user/login")
        .contentType(MediaType.APPLICATION_JSON)
        .content(body));
  }
}
