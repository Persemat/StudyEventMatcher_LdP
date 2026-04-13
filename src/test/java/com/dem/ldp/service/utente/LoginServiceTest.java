package com.dem.ldp.service.utente;

import com.dem.ldp.dto.LoginRequest;
import com.dem.ldp.dto.UtenteResponse;
import com.dem.ldp.model.Utente;

import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class LoginServiceTest extends BaseUtenteServiceTest {
    @Test
    void login_emailObbligatoria_lanciaEccezione() {
        LoginRequest request = new LoginRequest("", "Password123");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.login(request)
        );

        assertEquals("Email obbligatoria", ex.getMessage());
        verify(utenteRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_passwordObbligatoria_lanciaEccezione() {
        LoginRequest request = new LoginRequest("mario@example.com", "");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.login(request)
        );

        assertEquals("Password obbligatoria", ex.getMessage());
        verify(utenteRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_emailNonTrovata_lanciaEccezione() {
        LoginRequest request = new LoginRequest("mario@example.com", "Password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.login(request)
        );

        assertEquals("Credenziali non valide", ex.getMessage());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_passwordErrata_lanciaEccezione() {
        LoginRequest request = new LoginRequest("mario@example.com", "Password123");

        Utente utenteDb = new Utente("Mario", "Rossi", "mario@example.com", "HASH");
        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(utenteDb));
        when(passwordEncoder.matches("Password123", "HASH"))
                .thenReturn(false);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> utenteService.login(request)
        );

        assertEquals("Credenziali non valide", ex.getMessage());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(passwordEncoder).matches("Password123", "HASH");
    }

    @Test
    void login_valido_restituisceUtenteResponse() throws Exception {
    LoginRequest request = new LoginRequest("mario@example.com", "Password123");

    Utente utenteDb = new Utente("Mario", "Rossi", "mario@example.com", "HASH");
    setId(utenteDb, 1L);

    when(utenteRepository.findByEmail("mario@example.com"))
            .thenReturn(Optional.of(utenteDb));
    when(passwordEncoder.matches("Password123", "HASH"))
            .thenReturn(true);

    UtenteResponse response = utenteService.login(request);

    assertNotNull(response);
    assertEquals(1L, response.id());
    assertEquals("Mario", response.nome());
    assertEquals("Rossi", response.cognome());
    assertEquals("mario@example.com", response.email());

    verify(utenteRepository).findByEmail("mario@example.com");
    verify(passwordEncoder).matches("Password123", "HASH");
}

    @Test
    void getLoggedUser_senzaSessione_lanciaEccezione() {

    IllegalStateException ex = assertThrows(
            IllegalStateException.class,
            () -> utenteService.getLoggedUser(null)
    );

    assertEquals("Sessione non valida o scaduta", ex.getMessage());
    verify(utenteRepository, never()).findById(any());
}

    @Test
    void getLoggedUser_utenteNonTrovato_lanciaEccezione() {

    when(utenteRepository.findById(1L)).thenReturn(Optional.empty());

    IllegalArgumentException ex = assertThrows(
            IllegalArgumentException.class,
            () -> utenteService.getLoggedUser(1L)
    );

    assertEquals("Utente non trovato", ex.getMessage());
    verify(utenteRepository).findById(1L);
}

    @Test
    void getLoggedUser_valido_restituisceUtenteResponse() throws Exception {

        Utente utente = new Utente("Mario", "Rossi", "mario@example.com", "HASH");
        setId(utente, 1L);

        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));

        UtenteResponse response = utenteService.getLoggedUser(1L);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals("Mario", response.nome());
        assertEquals("Rossi", response.cognome());
        assertEquals("mario@example.com", response.email());
    }

    private void setId(Utente utente, Long id) throws Exception {
        Field field = Utente.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(utente, id);
    }
}
