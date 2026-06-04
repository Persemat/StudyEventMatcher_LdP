package com.example.backend.dto;

import java.util.List;

/**
 * Data Transfer Object representing the system response payload for a user's
 * profile data.
 * <p>
 * It encapsulates public profile identity details and interest preferences to
 * be safely
 * transmitted and rendered on the client interface, omitting sensitive
 * authentication data.
 * </p>
 */
public record UserResponse(
        Long id,
        String name,
        String lastname,
        String email,
        String favouriteCity,
        List<String> macroTags,
        List<String> microTags) {
}
