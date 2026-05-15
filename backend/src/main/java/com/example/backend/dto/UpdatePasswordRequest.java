package com.example.backend.dto;

public record UpdatePasswordRequest(
    String currentPassword,
    String newPassword,
    String confirmPassword
) {}
