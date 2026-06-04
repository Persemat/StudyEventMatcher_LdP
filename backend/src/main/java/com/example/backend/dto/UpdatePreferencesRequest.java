package com.example.backend.dto;

import java.util.List;

/**
 * Data Transfer Object representing a client request payload to update user
 * interest and geographical preferences.
 * <p>
 * It encapsulates the user's preferred city filter alongside macro and micro
 * interest categories
 * used by the system to customize event feeds and recommendations.
 * </p>
 */
public record UpdatePreferencesRequest(
                String favouriteCity,
                List<String> macroTags,
                List<String> microTags) {

}
