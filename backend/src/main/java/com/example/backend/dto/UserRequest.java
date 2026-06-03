package com.example.backend.dto;

import java.util.List;

/**
 * Data Transfer Object representing a client request payload to register a new
 * user profile.
 * <p>
 * It encapsulates all incoming registration data required by the system to
 * validate credentials,
 * map personal information, and securely provision the new account along with
 * initial interest tags.
 * </p>
 */
public record UserRequest(
                String name,
                String lastname,
                String email,
                String password,
                String favouriteCity,
                List<String> macroTags,
                List<String> microTags) {
}