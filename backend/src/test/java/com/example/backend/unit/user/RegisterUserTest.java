package com.example.backend.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

public class RegisterUserTest extends BaseUserTest {

    @Test
    void register_shouldThrowException_whenEmailIsInvalid() {
        UserRequest request = new UserRequest("Mario", "Rossi", "marioexample.com", "Password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("Email non valida", ex.getMessage());
        verify(userRepository, never()).findByEmail(any());
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_shouldThrowException_whenEmailAlreadyExists() {
        UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password123");

        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(new User()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("Email già registrata", ex.getMessage());
        verify(userRepository).findByEmail("mario@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_shouldThrowException_whenPasswordTooShort() {
        UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Pass1");

        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("Password troppo corta", ex.getMessage());
        verify(userRepository).findByEmail("mario@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_shouldThrowException_whenPasswordMissingUppercase() {
        UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "password123");

        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("La password deve contenere almeno una maiuscola", ex.getMessage());
        verify(userRepository).findByEmail("mario@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_shouldThrowException_whenPasswordMissingNumber() {
        UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password");

        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.register(request)
        );

        assertEquals("La password deve contenere almeno un numero", ex.getMessage());
        verify(userRepository).findByEmail("mario@example.com");
        verify(userRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void register_shouldSaveAndEncodePassword_whenValid() throws Exception {
        UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password123");

        when(userRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        when(passwordEncoder.encode("Password123"))
                .thenReturn("HASH123");

        User saved = new User("Mario", "Rossi", "mario@example.com", "HASH123");
        setId(saved, 1L);

        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.register(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mario", response.name());
        assertEquals("Rossi", response.lastname());
        assertEquals("mario@example.com", response.email());

        verify(passwordEncoder).encode("Password123");
        verify(userRepository).save(any(User.class));
    }

    private void setId(User user, Long id) throws Exception {
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
    }
}