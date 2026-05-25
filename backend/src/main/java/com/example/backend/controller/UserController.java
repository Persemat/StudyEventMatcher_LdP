package com.example.backend.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestParam;

import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.UpdateEmailRequest;
import com.example.backend.dto.UpdatePasswordRequest;
import com.example.backend.dto.UpdatePersonalDataRequest;
import com.example.backend.dto.UpdatePreferencesRequest;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * REST controller responsible for user-related operations.
 *
 * It provides endpoints for:
 * - user registration
 * - user authentication (login)
 * - retrieving the logged user
 * - logout
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    /**
     * Constructs the UserController.
     *
     * @param userService service used to manage user operations
     */
    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * Registers a new user.
     *
     * @param request the user registration data
     * @return the created user or an error message if validation fails
     */
    @PostMapping
    public ResponseEntity<?> createUser(@RequestBody UserRequest request) {
        try {
            UserResponse response = userService.register(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Authenticates a user and creates a session.
     *
     * If authentication is successful, the user ID is stored in the session.
     *
     * @param request the login credentials
     * @param session the current HTTP session
     * @return the authenticated user or an error message if credentials are invalid
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            UserResponse response = userService.login(request);
            session.setAttribute("userId", response.id());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException | IllegalStateException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Retrieves the currently logged user based on session data.
     *
     * @param session the current HTTP session
     * @return the logged user or an error response if the session is invalid
     */
    @GetMapping("/me")
    public ResponseEntity<?> getLoggedUser(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            UserResponse response = userService.getLoggedUser(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PutMapping("/me/personal-data")
    public ResponseEntity<?> updatePersonalData(@RequestBody UpdatePersonalDataRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            UserResponse response = userService.updatePersonalData(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me/email")
    public ResponseEntity<?> updateEmail(@RequestBody UpdateEmailRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            UserResponse response = userService.updateEmail(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<?> updatePassword(@RequestBody UpdatePasswordRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            UserResponse response = userService.updatePassword(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PutMapping("/me/preferences")
    public ResponseEntity<?> updatePreferences(@RequestBody UpdatePreferencesRequest request, HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            UserResponse response = userService.updatePreferences(userId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    /**
     * Logs out the current user by invalidating the session.
     *
     * @param request the HTTP request containing the session
     * @return a message indicating the logout status
     */
    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            try {
                session.invalidate();
                return ResponseEntity.ok("Disconnessione effettuata con successo");
            } catch (IllegalStateException e) {
                return ResponseEntity.ok("Sessione già invalidata");
            }
        }

        return ResponseEntity.ok("Nessuna sessione attiva, Utente già disconnesso");
    }

    @GetMapping("/verify")
    public ResponseEntity<?> verifyEmail(@RequestParam String token) {
        try {
            userService.verifyUserEmail(token);

            return ResponseEntity.ok(java.util.Map.of(
                    "status", "success",
                    "message", "'Email verificata con successo! Verrai reindirizzato al login...'"));

        } catch (IllegalArgumentException e) {

            return ResponseEntity.badRequest().body(java.util.Map.of(
                    "status", "error",
                    "message", e.getMessage()));
        }
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<String> resendVerificationCode(@RequestParam String email) {
        try {
            userService.resendVerificationEmail(email);
            return ResponseEntity.ok("Nuovo codice di verifica inviato con successo.");
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Errore: " + e.getMessage());
        }
    }
}
