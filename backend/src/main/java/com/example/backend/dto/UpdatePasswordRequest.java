package com.example.backend.dto;

/**
 * Data Transfer Object representing a client request payload to change an
 * authenticated user's password.
 * <p>
 * It encapsulates the existing password for identity re-verification, along
 * with the newly
 * requested password and its matching confirmation string used to prevent typo
 * errors before execution.
 * </p>
 */
public record UpdatePasswordRequest(
        String currentPassword,
        String newPassword,
        String confirmPassword) {
}
