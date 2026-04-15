package com.dem.ldp.dto;

public record ResetPasswordRequest(
        String email,
        String codice,
        String nuovaPassword
) {
}