package com.example.backend.unit.user;


import org.junit.jupiter.api.Test;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginTest extends BaseUserTest {
    @Test
    void login_emailRequired_throwsException() {
        LoginRequest request = new LoginRequest("", "Password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );

        assertEquals("Email obbligatoria", ex.getMessage());
        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_passwordRequired_throwsException() {
        LoginRequest request = new LoginRequest("mario@example.com", "");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );

        assertEquals("Password Obbligatoria", ex.getMessage());
        verify(userRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_emailNotFound_throwsException() {
        LoginRequest request = new LoginRequest("mario@example.com", "Password123");

        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );

        assertEquals("Credenziali invalide", ex.getMessage());
        verify(userRepository).findByEmail("mario@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_wrongPassword_throwsException() {
        LoginRequest request = new LoginRequest("mario@example.com", "Password123");

        User userDb = new User("Mario", "Rossi", "mario@example.com", "HASH");
        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(userDb));
        when(passwordEncoder.matches("Password123", "HASH"))
                .thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.login(request)
        );

        assertEquals("Credenziali invalide", ex.getMessage());
        verify(userRepository).findByEmail("mario@example.com");
        verify(passwordEncoder).matches("Password123", "HASH");
    }

    @Test
    void login_valid_returnsUserResponse() throws Exception {
    LoginRequest request = new LoginRequest("mario@example.com", "Password123");

    User userDb = new User("Mario", "Rossi", "mario@example.com", "HASH");
    setId(userDb, 1L);

    when(userRepository.findByEmail("mario@example.com"))
            .thenReturn(Optional.of(userDb));
    when(passwordEncoder.matches("Password123", "HASH"))
            .thenReturn(true);

    UserResponse response = userService.login(request);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Mario", response.name());
    assertEquals("Rossi", response.lastname());
    assertEquals("mario@example.com", response.email());

    verify(userRepository).findByEmail("mario@example.com");
    verify(passwordEncoder).matches("Password123", "HASH");
}

    @Test
    void getLoggedUser_withoutSession_throwsException() {

    IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> userService.getLoggedUser(null)
    );

    assertEquals("Sessione invalida o scaduta", ex.getMessage());
    verify(userRepository, never()).findById(any());
}

    @Test
    void getLoggedUser_userNotFound_throwsException() {

    when(userRepository.findById(1L)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> userService.getLoggedUser(1L)
    );

    assertEquals("Utente non trovato", ex.getMessage());
    verify(userRepository).findById(1L);
}

    @Test
    void getLoggedUser_valid_returnsUserResponse() throws Exception {

        User User = new User("Mario", "Rossi", "mario@example.com", "HASH");
        setId(User, 1L);

        when(userRepository.findById(1L)).thenReturn(Optional.of(User));

        UserResponse response = userService.getLoggedUser(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mario", response.name());
        assertEquals("Rossi", response.lastname());
        assertEquals("mario@example.com", response.email());
    }

    private void setId(User User, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(User, id);
    }
}
