package com.dem.ldp.dto;

public record UtenteRequest(
        String nome,
        String cognome,
        String email,
        String password
) {}