package com.example.backend.dto;

import java.util.List;

/**
 * Data Transfer Object for the intelligent dashboard recommendations.
 * <p>
 * It encapsulates the list of recommended events and a specific status message code
 * to inform the frontend about the result of the recommendation engine's processing.
 *
 * @param recommendedEvents the list of events matching the user's interests and geographical location, sorted by score
 * @param messageCode a status string identifying the exact outcome. 
 * Possible values: "SUCCESS", "NO_TAG_MATCH", "NO_LOCATION_MATCH", "MISSING_COORDINATES"
 */
public record RecommendationResponse(
        List<EventResponse> recommendedEvents,
        String messageCode
) {
}