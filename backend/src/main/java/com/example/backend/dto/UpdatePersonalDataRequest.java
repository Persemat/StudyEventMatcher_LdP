package com.example.backend.dto;

/**
 * Data Transfer Object representing a client request payload to update basic
 * user identity profile data.
 * <p>
 * It isolates and encapsulates modifications to the user's name and surname
 * before they are
 * processed and updated in the database context.
 * </p>
 */
public record UpdatePersonalDataRequest(
        String name,
        String lastname) {
}
