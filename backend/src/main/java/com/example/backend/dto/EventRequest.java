package com.example.backend.dto;

import java.time.LocalDateTime;
import java.util.List;

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
        boolean creatorParticipationRequired
)
