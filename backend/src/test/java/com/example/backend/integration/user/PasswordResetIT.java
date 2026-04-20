package com.example.backend.integration.user;

import com.example.backend.model.PasswordResetToken;
import com.example.backend.model.User;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Tag("integration")
public class PasswordResetIT extends BasePasswordResetIT{

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void forgotPasswordValid_shouldReturn200AndSaveToken() throws Exception {
        User user = new User("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        userRepository.save(user);

        String json = """
                {
                  "email": "mario@example.com"
                }
                """;

        mockMvc.perform(post("/password-reset/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Password reset code sent via email")));

        assertTrue(passwordResetTokenRepository.findAll().stream()
                .anyMatch(token -> token.getEmail().equals("mario@example.com")));
    }

    @Test
    void forgotPasswordEmailNotRegistered_shouldReturn400() throws Exception {
        String json = """
                {
                  "email": "mario@example.com"
                }
                """;

        mockMvc.perform(post("/password-reset/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("No user found with this email")));
    }

    @Test
    void forgotPasswordTooSoon_shouldReturn400() throws Exception {
        User user = new User("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCode("123456");
        token.setCreatedAt(LocalDateTime.now().minusSeconds(10));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        String json = """
                {
                  "email": "mario@example.com"
                }
                """;

        mockMvc.perform(post("/password-reset/forgot")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Wait at least 30 seconds before requesting a new code")));
    }

    @Test
    void resetPasswordValid_shouldReturn200AndUpdatePassword() throws Exception {
        User user = new User("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCode("123456");
        token.setCreatedAt(LocalDateTime.now().minusMinutes(1));
        token.setExpiresAt(LocalDateTime.now().plusMinutes(10));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        String json = """
                {
                  "email": "mario@example.com",
                  "code": "123456",
                  "newPassword": "NuovaPass1"
                }
                """;

        mockMvc.perform(post("/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Password updated successfully")));

        User updated = userRepository.findByEmail("mario@example.com").orElseThrow();
        assertTrue(passwordEncoder.matches("NuovaPass1", updated.getPassword()));
    }

    @Test
    void resetPasswordInvalidCode_shouldReturn400() throws Exception {
        User user = new User("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        userRepository.save(user);

        String json = """
                {
                  "email": "mario@example.com",
                  "code": "999999",
                  "newPassword": "NuovaPass1"
                }
                """;

        mockMvc.perform(post("/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Invalid code")));
    }

    @Test
    void resetPasswordExpiredCode_shouldReturn400() throws Exception {
        User user = new User("Mario", "Rossi", "mario@example.com", passwordEncoder.encode("Password123"));
        userRepository.save(user);

        PasswordResetToken token = new PasswordResetToken();
        token.setEmail("mario@example.com");
        token.setCode("123456");
        token.setCreatedAt(LocalDateTime.now().minusMinutes(20));
        token.setExpiresAt(LocalDateTime.now().minusMinutes(1));
        token.setUsed(false);
        passwordResetTokenRepository.save(token);

        String json = """
                {
                  "email": "mario@example.com",
                  "code": "123456",
                  "newPassword": "NuovaPass1"
                }
                """;

        mockMvc.perform(post("/password-reset/confirm")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isBadRequest())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("Code expired")));
    }
}