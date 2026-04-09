package com.dem.ldp.dto;

public record UtenteResponse(
        Long id,
        String nome,
        String cognome,
        String email
) {}