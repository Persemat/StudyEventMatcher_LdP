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
import com.example.backend.dto.NotificationSettingsRequest;
import com.example.backend.dto.NotificationSettingsResponse;
import com.example.backend.service.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

/**
 * REST controller responsible for user-related operations.
 *
 * It provides endpoints for:
 * - user registration and email token verification
 * - user authentication (login, logout, and session handling)
 * - retrieving and updating user profile data (personal details, credentials,
 * and tags)
 * - retrieving and updating personalized notification settings
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

    /**
     * Retrieves the notification settings of the currently authenticated user.
     *
     * @param session the current HTTP session
     * @return a {@link ResponseEntity} containing the user's notification settings,
     *         a 401 status if the session is invalid, or a 404 status if the user
     *         is not found
     */
    @GetMapping("/me/settings")
    public ResponseEntity<?> getNotificationSettings(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            NotificationSettingsResponse settingResponse = userService.showNotificationSettings(userId);
            return ResponseEntity.ok(settingResponse);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    /**
     * Updates the personal data (e.g., name, lastname) of the authenticated user.
     *
     * @param request the request object containing the updated personal data fields
     * @param session the current HTTP session
     * @return a {@link ResponseEntity} containing the updated user information,
     *         a 401 status if the session is invalid, or a 400 status if validation
     *         fails
     */
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

    /**
     * Updates the email address of the authenticated user.
     *
     * @param request the request object containing the new email address
     * @param session the current HTTP session
     * @return a {@link ResponseEntity} containing the updated user information,
     *         a 401 status if the session is invalid, or a 400 status if the email
     *         is invalid or already exists
     */
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

    /**
     * Updates the password of the authenticated user.
     *
     * @param request the request object containing the current password, the new
     *                password, and confirmation
     * @param session the current HTTP session
     * @return a {@link ResponseEntity} containing the updated user information,
     *         a 401 status if the session is invalid, or a 400 status if the
     *         current password is wrong or the new one is weak
     */
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

    /**
     * Updates the academic or professional preferences (city, macro-tags,
     * micro-tags) of the authenticated user.
     *
     * @param request the request object containing the updated city and interest
     *                tags
     * @param session the current HTTP session
     * @return a {@link ResponseEntity} containing the updated user information,
     *         a 401 status if the session is invalid, or a 400 status if validation
     *         rules are violated
     */
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
     * Updates the communication and email notification preferences of the
     * authenticated user.
     *
     * @param request the request object containing the updated flags for
     *                notification preferences
     * @param session the current HTTP session
     * @return a {@link ResponseEntity} containing the updated notification
     *         settings,
     *         a 401 status if the session is invalid, or a 400 status if updates
     *         cannot be processed
     */
    @PutMapping("/me/settings")
    public ResponseEntity<?> updateNotificationSettings(@RequestBody NotificationSettingsRequest request,
            HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            NotificationSettingsResponse settingResponse = userService.modifySettings(request, userId);
            return ResponseEntity.ok(settingResponse);
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

    /**
     * Verifies a user's email address using a unique token sent via email.
     *
     * @param token the unique verification token provided in the URL query string
     * @return a {@link ResponseEntity} containing a success status map,
     *         or a 400 status map if the token is invalid or expired
     */
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

    /**
     * Resends a new verification token to the user's email address if the previous
     * one expired.
     *
     * @param email the email address of the user requiring a new verification code
     * @return a {@link ResponseEntity} indicating whether the operation succeeded,
     *         or a 400 status with an error message otherwise
     */
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
