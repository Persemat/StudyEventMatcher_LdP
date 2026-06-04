package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Event responses.
 * <p>
 * It includes structured relational data, participant details, and contextual
 * flags
 * (such as booking, ownership, and favorite states) evaluated against the
 * currently logged-in user.
 * </p>
 */
public record EventResponse(
                Long id,
                String title,
                LocalDateTime dateTime,
                Double duration,
                String location,
                Double latitude,
                Double longitude,
                List<EventTagDto> tags,
                String description,
                Integer maxParticipants,
                boolean creatorParticipationRequired,
                String status,
                CreatorResponse creator,
                Integer currentParticipants,
                boolean isBooked,
                boolean isCreatedByMe,
                boolean isFavorite,
                List<ParticipantResponse> participants) {
        /**
         * DTO for the creator's basic information.
         */
        public record CreatorResponse(
                        Long id,
                        String name,
                        String lastName

        ) {
        }

        /**
         * DTO for the participants' basic information (Name and Last Name).
         */
        public record ParticipantResponse(
                        Long id,
                        String name,
                        String lastName,
                        String email) {
        }
}
