package com.example.backend.dto;

import java.util.List;

public record UserRequest(
        String name,
        String lastname,
        String email,
        String password,
        String favouriteCity,
        List<String> macroTags,
        List<String> microTags) {
}