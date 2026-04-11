package com.dem.ldp.service;

import com.dem.ldp.dto.LoginRequest;
import com.dem.ldp.dto.UtenteRequest;
import com.dem.ldp.dto.UtenteResponse;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UtenteServiceTest {

    private UtenteRepository utenteRepository;
    private PasswordEncoder passwordEncoder;
    private UtenteService utenteService;

    @BeforeEach
    void setUp() {
        utenteRepository = mock(UtenteRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        utenteService = new UtenteService(utenteRepository, passwordEncoder);
    }

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