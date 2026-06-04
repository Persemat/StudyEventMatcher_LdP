package com.example.backend.dto;

/**
 * Data Transfer Object representing the system response payload for a user's
 * notification preferences.
 * <p>
 * It encapsulates the active states of the communication flags used by the
 * client
 * to render toggle switches on the frontend settings interface.
 * </p>
 */
public record NotificationSettingsResponse(
                boolean editEventEmailActivated,
                boolean deleteEventEmailActivated,
                boolean recomendedEventEmailActivated) {

}
