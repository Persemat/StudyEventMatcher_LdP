package com.example.backend.dto;

/**
 * Data Transfer Object representing a client request payload to finalize the
 * password reset process.
 * <p>
 * It encapsulates the user's email identifier, the secure verification code
 * received by the user,
 * and the new unhashed password string requested for account update.
 * </p>
 */
public record ResetPasswordRequest(
                String email,
                String code,
                String newPassword) {
}
