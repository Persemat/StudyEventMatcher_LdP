package com.example.backend.dto;

/**
 * Data Transfer Object representing a client request to initiate the password
 * recovery workflow.
 * <p>
 * It encapsulates the target user's email address required by the system to
 * verify identity.
 * </p>
 */
public record ForgotPasswordRequest(String email) {
}
