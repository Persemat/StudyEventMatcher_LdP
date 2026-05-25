package com.example.backend.dto;

public record NotificationSettingsRequest(
        boolean editEventEmailActivated,
        boolean deleteEventEmailActivated,
        boolean recomendedEventEmailActivated) {

}
