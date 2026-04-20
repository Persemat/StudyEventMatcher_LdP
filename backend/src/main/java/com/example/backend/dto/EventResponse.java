package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        boolean isCreatedByMe
) {

    public record CreatorResponse(
            Long id,
            String name,
            String lastName
    ) {
    }
}
