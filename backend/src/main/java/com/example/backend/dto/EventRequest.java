package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object representing a client request payload to create or
 * update an event.
 */
public record EventRequest(
                String title,
                LocalDateTime dateTime,
                Double duration,
                String location,
                Double latitude,
                Double longitude,
                List<?> tags,
                String description,
                Integer maxParticipants,
                boolean creatorParticipationRequired) {
}
