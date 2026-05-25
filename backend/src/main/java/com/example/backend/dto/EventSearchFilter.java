package com.example.backend.dto;

import java.util.List;

public record EventSearchFilter(
        String search,
        List<String> tags,
        String location,
        String date,
        Double latitude,
        Double longitude,
        Integer distanceKm,
        Boolean hideOverlapping
) {}