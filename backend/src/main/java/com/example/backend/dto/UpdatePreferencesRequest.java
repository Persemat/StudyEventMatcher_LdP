package com.example.backend.dto;

import java.util.List;

public record UpdatePreferencesRequest(
        String favouriteCity,
        List<String> macroTags,
        List<String> microTags) {

}
