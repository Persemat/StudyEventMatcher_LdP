package com.example.backend.dto;

import java.util.List;

public record UserResponse(
                Long id,
                String name,
                String lastname,
                String email,
                String favouriteCity,
                List<String> macroTags,
                List<String> microTags) {
}
