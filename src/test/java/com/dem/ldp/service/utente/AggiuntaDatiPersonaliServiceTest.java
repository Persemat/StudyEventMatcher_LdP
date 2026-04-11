package com.dem.ldp.service.utente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.dem.ldp.dto.UtenteRequest;

import org.junit.jupiter.api.Test;

public class AggiuntaDatiPersonaliServiceTest extends BaseUtenteServiceTest {
    @Test
    void registra_nomeMancante_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("", "Rossi", "mario@example.com", "Password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Nome obbligatorio", ex.getMessage());
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_cognomeMancante_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("Mario", "", "mario@example.com", "Password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Cognome obbligatorio", ex.getMessage());
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }
}
