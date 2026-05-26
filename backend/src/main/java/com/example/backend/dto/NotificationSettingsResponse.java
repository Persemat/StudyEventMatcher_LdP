package com.example.backend.dto;

public record NotificationSettingsResponse(
        boolean editEventEmailActivated,
        boolean deleteEventEmailActivated,
        boolean recomendedEventEmailActivated) {

}
