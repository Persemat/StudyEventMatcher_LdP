package com.example.backend.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.UserRequest;
import java.util.List;

public class AddUserPersonalDataTest extends BaseUserTest {

    @Test
    void register_shouldThrowException_whenFirstNameIsMissing() {

        UserRequest request = new UserRequest("", "Rossi", "mario@example.com", "Password123", "Milano", List.of(),
                List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request));

        assertEquals("Nome obbligatorio", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_shouldThrowException_whenLastNameIsMissing() {

        UserRequest request = new UserRequest("Mario", "", "mario@example.com", "Password123", "Milano", List.of(),
                List.of());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request));

        assertEquals("Cognome obbligatorio", ex.getMessage());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}