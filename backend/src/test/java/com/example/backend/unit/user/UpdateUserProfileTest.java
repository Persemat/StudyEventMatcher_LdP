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
                                new UpdatePersonalDataRequest("Marco", "Bianchi"));

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
                                () -> userService.updatePersonalData(1L, new UpdatePersonalDataRequest("", "Rossi")));

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
                                () -> userService.updateEmail(1L, new UpdateEmailRequest("marioexample.com")));

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
                                () -> userService.updateEmail(1L, new UpdateEmailRequest("luca@example.com")));

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
                                new UpdatePasswordRequest("OldPass1", "NewPass1", "NewPass1"));

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
                                () -> userService.updatePassword(1L,
                                                new UpdatePasswordRequest("WrongPass1", "NewPass1", "NewPass1")));

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
                                () -> userService.updatePassword(1L,
                                                new UpdatePasswordRequest("OldPass1", "password", "password")));

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
                                () -> userService.updatePassword(1L,
                                                new UpdatePasswordRequest("OldPass1", "NewPass1", "OtherPass1")));

                assertEquals("Le password non corrispondono", ex.getMessage());
                verify(passwordEncoder, never()).matches(any(CharSequence.class), any(String.class));
                verify(passwordEncoder, never()).encode(any(CharSequence.class));
                verify(userRepository, never()).save(any());
        }

        @Test
        void updatePreferences_shouldSavePreferences_whenValid() throws Exception {
                User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
                user.setFavouriteCity("Roma");
                user.setMacroTags(new java.util.ArrayList<>(java.util.List.of("Psicologia")));
                user.setMicroTags(new java.util.ArrayList<>());

                User saved = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
                saved.setFavouriteCity("Milano");
                saved.setMacroTags(new java.util.ArrayList<>(java.util.List.of("Informatica e Tecnologie")));
                saved.setMicroTags(new java.util.ArrayList<>(java.util.List.of("Java")));

                when(userRepository.findById(1L)).thenReturn(Optional.of(user));
                when(userRepository.save(any(User.class))).thenReturn(saved);

                com.example.backend.dto.UpdatePreferencesRequest request = new com.example.backend.dto.UpdatePreferencesRequest(
                                "Milano",
                                java.util.List.of("Informatica e Tecnologie"),
                                java.util.List.of("Java"));

                UserResponse response = userService.updatePreferences(1L, request);

                assertNotNull(response);
                assertEquals("Milano", response.favouriteCity());
                assertEquals(1, response.macroTags().size());
                assertEquals("Informatica e Tecnologie", response.macroTags().get(0));
                assertEquals(1, response.microTags().size());
                assertEquals("Java", response.microTags().get(0));
                verify(userRepository).save(any(User.class));
        }

        @Test
        void updatePreferences_shouldThrowException_whenCityIsMissing() throws Exception {
                User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));

                com.example.backend.dto.UpdatePreferencesRequest request = new com.example.backend.dto.UpdatePreferencesRequest(
                                "   ",
                                java.util.List.of("Informatica"),
                                java.util.List.of("Java"));

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updatePreferences(1L, request));

                assertEquals("Bisogna indicare una città preferita", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void updatePreferences_shouldThrowException_whenMacroTagsAreEmpty() throws Exception {
                User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));

                com.example.backend.dto.UpdatePreferencesRequest request = new com.example.backend.dto.UpdatePreferencesRequest(
                                "Milano",
                                java.util.List.of(),
                                java.util.List.of());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updatePreferences(1L, request));

                assertEquals("Almeno una macro area di interesse deve essere indicata", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void updatePreferences_shouldThrowException_whenTooManyMacroTags() throws Exception {
                User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));

                com.example.backend.dto.UpdatePreferencesRequest request = new com.example.backend.dto.UpdatePreferencesRequest(
                                "Milano",
                                java.util.List.of("T1", "T2", "T3", "T4"),
                                java.util.List.of());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updatePreferences(1L, request));

                assertEquals("Si possono indicare al massimo 3 aree di interesse generale ", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void updatePreferences_shouldThrowException_whenTooManyMicroTags() throws Exception {
                User user = userWithId(1L, "Mario", "Rossi", "mario@example.com", "HASH");
                when(userRepository.findById(1L)).thenReturn(Optional.of(user));

                com.example.backend.dto.UpdatePreferencesRequest request = new com.example.backend.dto.UpdatePreferencesRequest(
                                "Milano",
                                java.util.List.of("Informatica"),
                                java.util.List.of("m1", "m2", "m3", "m4", "m5", "m6"));

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.updatePreferences(1L, request));

                assertEquals("Si possono indicare al massimo 5 argomenti di interesse specifici ", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        private User userWithId(Long id, String name, String lastname, String email, String password) throws Exception {
                User user = new User();
                user.setName(name);
                user.setLastname(lastname);
                user.setEmail(email);
                user.setPassword(password);
                Field field = User.class.getDeclaredField("id");
                field.setAccessible(true);
                field.set(user, id);
                return user;
        }
}
