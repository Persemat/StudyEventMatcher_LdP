package com.example.backend.dto;

/**
 * Data Transfer Object representing a client request payload containing
 * authentication credentials.
 * <p>
 * It captures the raw credentials submitted during the login process before
 * they are
 * validated and processed by the service layer.
 * </p>
 */
public record LoginRequest(
                String email,
                String password) {
}
