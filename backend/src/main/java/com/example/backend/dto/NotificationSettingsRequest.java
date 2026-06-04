package com.example.backend.dto;

/**
 * Data Transfer Object representing a client request payload to modify email
 * notification preferences.
 * <p>
 * It captures the updated states of the communication toggles before they are
 * mapped and
 * persisted into the user's permanent profile settings.
 * </p>
 */
public record NotificationSettingsRequest(
                boolean editEventEmailActivated,
                boolean deleteEventEmailActivated,
                boolean recomendedEventEmailActivated) {

}
