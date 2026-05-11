package com.example.backend.dto;

public record UserRequest(
    String name,
    String lastname,
    String email,
    String password
) {}

