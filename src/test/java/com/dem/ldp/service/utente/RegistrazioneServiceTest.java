package com.dem.ldp.service.utente;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.dem.ldp.dto.UtenteRequest;
import com.dem.ldp.dto.UtenteResponse;
import com.dem.ldp.model.Utente;

import org.junit.jupiter.api.Test;
import java.lang.reflect.Field;

import java.util.Optional;

public class RegistrazioneServiceTest extends BaseUtenteServiceTest {
    @Test
    void registra_emailNonValida_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("Mario", "Rossi", "marioexample.com", "Password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Email non valida", ex.getMessage());
        verify(utenteRepository, never()).findByEmail(any());
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_emailGiaRegistrata_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("Mario", "Rossi", "mario@example.com", "Password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(new Utente()));

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Email già registrata", ex.getMessage());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_passwordTroppoCorta_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("Mario", "Rossi", "mario@example.com", "Pass1");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Password troppo corta", ex.getMessage());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_passwordSenzaMaiuscola_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("Mario", "Rossi", "mario@example.com", "password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Serve una maiuscola", ex.getMessage());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_passwordSenzaNumero_lanciaEccezione() {
        UtenteRequest request = new UtenteRequest("Mario", "Rossi", "mario@example.com", "Password");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.registra(request)
        );

        assertEquals("Serve un numero", ex.getMessage());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_valido_codificaPasswordESalva() throws Exception {
        UtenteRequest request = new UtenteRequest("Mario", "Rossi", "mario@example.com", "Password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123"))
                .thenReturn("HASH123");

        Utente salvato = new Utente("Mario", "Rossi", "mario@example.com", "HASH123");
        setId(salvato, 1L);

        when(utenteRepository.save(any(Utente.class))).thenReturn(salvato);

        UtenteResponse response = utenteService.registra(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mario", response.nome());
        assertEquals("Rossi", response.cognome());
        assertEquals("mario@example.com", response.email());

        verify(passwordEncoder).encode("Password123");
        verify(utenteRepository).save(any(Utente.class));
    }
    private void setId(Utente utente, Long id) throws Exception {
        Field field = Utente.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(utente, id);
    }
}
