package com.example.backend.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.UpdateEmailRequest;
import com.example.backend.dto.UpdatePasswordRequest;
import com.example.backend.dto.UpdatePersonalDataRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;

public class UpdateUserProfileTest extends BaseUserTest {

    @Test
    void updatePersonalData_shouldSaveNameAndLastname_whenValid() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
        User saved = userWithId(1L, "Marco", "Bianchi", "mario@example.com", "HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.updatePersonalData(
                1L,
                new UpdatePersonalDataRequest("Marco", "Bianchi")
        );

        assertNotNull(response);
        assertEquals("Marco", response.name());
        assertEquals("Bianchi", response.lastname());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updatePersonalData_shouldThrowException_whenRequiredFieldsAreMissing() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePersonalData(1L, new UpdatePersonalDataRequest("", "Rossi"))
        );

        assertEquals("Campi obbligatori mancanti", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateEmail_shouldSaveEmail_whenValid() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
        User saved = userWithId(1L, "Mario", "Rossi", "marco@example.com", "HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("marco@example.com")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.updateEmail(1L, new UpdateEmailRequest("marco@example.com"));

        assertEquals("marco@example.com", response.email());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updateEmail_shouldThrowException_whenEmailIsInvalid() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateEmail(1L, new UpdateEmailRequest("marioexample.com"))
        );

        assertEquals("Email non valida", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updateEmail_shouldThrowException_whenEmailAlreadyExistsForAnotherUser() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
        User otherUser = userWithId(2L, "Luca", "Verdi", "luca@example.com", "HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.findByEmail("luca@example.com")).thenReturn(Optional.of(otherUser));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updateEmail(1L, new UpdateEmailRequest("luca@example.com"))
        );

        assertEquals("Email già registrata", ex.getMessage());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_shouldSavePassword_whenCurrentPasswordIsCorrect() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "OLD_HASH");
        User saved = userWithId(1L, "Mario", "Rossi", "mario@example.com", "NEW_HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", "OLD_HASH")).thenReturn(true);
        when(passwordEncoder.encode("NewPass1")).thenReturn("NEW_HASH");
        when(userRepository.save(any(User.class))).thenReturn(saved);

        UserResponse response = userService.updatePassword(
                1L,
                new UpdatePasswordRequest("OldPass1", "NewPass1", "NewPass1")
        );

        assertEquals("mario@example.com", response.email());
        verify(passwordEncoder).encode("NewPass1");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void updatePassword_shouldThrowException_whenCurrentPasswordIsWrong() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "OLD_HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("WrongPass1", "OLD_HASH")).thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(1L, new UpdatePasswordRequest("WrongPass1", "NewPass1", "NewPass1"))
        );

        assertEquals("Password attuale non corretta", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_shouldThrowException_whenNewPasswordIsWeak() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "OLD_HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("OldPass1", "OLD_HASH")).thenReturn(true);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(1L, new UpdatePasswordRequest("OldPass1", "password", "password"))
        );

        assertEquals("La password deve contenere almeno una maiuscola", ex.getMessage());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void updatePassword_shouldThrowException_whenPasswordConfirmationDoesNotMatch() throws Exception {
        User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "OLD_HASH");

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> userService.updatePassword(1L, new UpdatePasswordRequest("OldPass1", "NewPass1", "OtherPass1"))
        );

        assertEquals("Le password non corrispondono", ex.getMessage());
        verify(passwordEncoder, never()).matches(any(), any());
        verify(passwordEncoder, never()).encode(any());
        verify(userRepository, never()).save(any());
    }

    private User userWithId(Long id, String name, String lastname, String email, String password) throws Exception {
        User user = new User(name, lastname, email, password);
        Field field = User.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(user, id);
        return user;
    }
}
