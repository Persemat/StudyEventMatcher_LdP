package com.example.backend.unit.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.lang.reflect.Field;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.example.backend.dto.UserRequest;
import java.util.List;

public class AddUserPersonalDataTest extends BaseUserTest {

        @Test
        void register_shouldThrowException_whenFirstNameIsMissing() {

                UserRequest request = new UserRequest("", "Rossi", "mario@example.com", "Password123", "Milano",
                                List.of(),
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

                UserRequest request = new UserRequest("Mario", "", "mario@example.com", "Password123", "Milano",
                                List.of(),
                                List.of());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.register(request));

                assertEquals("Cognome obbligatorio", ex.getMessage());
                verify(userRepository, never()).save(any());
                verify(passwordEncoder, never()).encode(any());
        }

        @Test
        void register_shouldThrowException_whenFavouriteCityIsMissing() {

                UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password123", "",
                                List.of("Informatica"), List.of());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.register(request));

                assertEquals("Bisogna indicare una città preferita", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void register_shouldThrowException_whenMacroTagsAreEmpty() {

                UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password123", "Milano",
                                List.of(), List.of());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.register(request));

                assertEquals("Almeno una macro area di interesse deve essere indicata", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void register_shouldThrowException_whenTooManyMacroTags() {

                List<String> tooManyMacros = List.of("Tag1", "Tag2", "Tag3", "Tag4");
                UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password123", "Milano",
                                tooManyMacros, List.of());

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.register(request));

                assertEquals("Si possono indicare al massimo 3 aree di interesse generale ", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void register_shouldThrowException_whenTooManyMicroTags() {

                List<String> macros = List.of("Informatica");
                List<String> tooManyMicros = List.of("T1", "T2", "T3", "T4", "T5", "T6");
                UserRequest request = new UserRequest("Mario", "Rossi", "mario@example.com", "Password123", "Milano",
                                macros, tooManyMicros);

                IllegalArgumentException ex = assertThrows(
                                IllegalArgumentException.class,
                                () -> userService.register(request));

                assertEquals("Si possono indicare al massimo 5 argomenti di interesse specifici ", ex.getMessage());
                verify(userRepository, never()).save(any());
        }

        @Test
        void register_shouldSaveUser_whenAllDataAreCorrect() throws Exception {

                List<String> macros = List.of("Informatica e Tecnologie", "Psicologia");
                List<String> micros = List.of("Java", "Spring Boot", "React");
                UserRequest request = new UserRequest(
                                "Mario",
                                "Rossi",
                                "mario.success@example.com",
                                "Password123!",
                                "Milano",
                                macros,
                                micros);

                when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
                when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword123");

                when(userRepository.save(any(com.example.backend.model.User.class))).thenAnswer(invocation -> {
                        com.example.backend.model.User savedUser = invocation.getArgument(0);
                        Field field = com.example.backend.model.User.class.getDeclaredField("id");
                        field.setAccessible(true);
                        field.set(savedUser, 1L);
                        return savedUser;
                });

                com.example.backend.dto.UserResponse response = userService.register(request);

                assertEquals("Mario", response.name());
                assertEquals("Rossi", response.lastname());
                assertEquals("mario.success@example.com", response.email());
                assertEquals("Milano", response.favouriteCity());

                verify(userRepository).save(any(com.example.backend.model.User.class));
        }
}