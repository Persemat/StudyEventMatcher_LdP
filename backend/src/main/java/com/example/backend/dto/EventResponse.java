package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Data Transfer Object for Event responses.
 * It includes a list of participants to display in the details view.
 */
public record EventResponse(
        Long id,
        String title,
        LocalDateTime dateTime,
        Double duration,
        String location,
        List<String> tags,
        String description,
        Integer maxParticipants,
        boolean creatorParticipationRequired,
        String status,
        CreatorResponse creator,
        Integer currentParticipants,
        boolean isBooked,
        boolean isCreatedByMe,
        List<ParticipantResponse> participants
) {
/**
*  DTO for the creator's basic information.
*/
    public record CreatorResponse(
            Long id,
            String name,
            String lastName
    ) {}
/**
* DTO for the participants' basic information (Name and Last Name).
*/
    public record ParticipantResponse(
            Long id,
            String name,
            String lastName
    ) {}
}
