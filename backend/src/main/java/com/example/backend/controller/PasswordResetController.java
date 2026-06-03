package com.example.backend.controller;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.backend.dto.ForgotPasswordRequest;
import com.example.backend.dto.ResetPasswordRequest;
import com.example.backend.service.PasswordResetService;

/**
 * REST controller responsible for password reset operations.
 *
 * It provides endpoints for:
 * - requesting a password reset code
 * - confirming password reset with a valid code
 */
@RestController
@RequestMapping("/password-reset")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    /**
     * Constructs the PasswordResetController.
     *
     * @param passwordResetService service used to handle password reset logic
     */
    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    /**
     * Handles password reset requests by generating and sending a verification
     * code.
     *
     * @param request contains the user's email
     * @return a success message if the code is sent, or an error response otherwise
     */
    @PostMapping("/forgot")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequest request) {
        try {
            String message = passwordResetService.forgotPassword(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("errore", e.getMessage()));
        }
    }

    /**
     * Resets the user's password using a valid reset code.
     *
     * @param request contains email, verification code, and new password
     * @return a {@link ResponseEntity} containing a map with a success message if
     *         the password is updated,
     *         or a 400 Bad Request status with an error map if validation or code
     *         verification fails
     */
    @PostMapping("/confirm")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequest request) {
        try {
            String message = passwordResetService.resetPassword(request);
            return ResponseEntity.ok(Map.of("message", message));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("errore", e.getMessage()));
        }
    }
}