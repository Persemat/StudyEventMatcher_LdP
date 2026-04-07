package com.dem.ldp.service;

import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class UtenteServiceTest {

    @Mock
    private HttpSession session;

    private UtenteRepository utenteRepository;
    private PasswordEncoder passwordEncoder;
    private UtenteService utenteService;

    @BeforeEach
    void setUp() {
        utenteRepository = mock(UtenteRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        session = mock(HttpSession.class);

        utenteService = new UtenteService(utenteRepository, passwordEncoder);
    }

    @Test
    void registra_nomeMancante_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password123");

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Nome obbligatorio", response.getBody());
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_cognomeMancante_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password123");

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Cognome obbligatorio", response.getBody());
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_emailNonValida_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("marioexample.com");
        utente.setPassword("Password123");

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email non valida", response.getBody());
        verify(utenteRepository, never()).findByEmail(any());
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_emailGiaRegistrata_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(new Utente()));

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email già registrata", response.getBody());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_passwordTroppoCorta_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Pass1");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Password troppo corta", response.getBody());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_passwordSenzaMaiuscola_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Serve una maiuscola", response.getBody());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_passwordSenzaNumero_restituisce400() {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Serve un numero", response.getBody());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(utenteRepository, never()).save(any());
        verify(passwordEncoder, never()).encode(any());
    }

    @Test
    void registra_valido_codificaPasswordESalva() throws Exception {
        Utente utente = new Utente();
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@example.com");
        utente.setPassword("Password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());
        when(passwordEncoder.encode("Password123"))
                .thenReturn("HASH123");

        Utente salvato = new Utente("Mario", "Rossi", "mario@example.com", "HASH123");
        setId(salvato, 1L);

        when(utenteRepository.save(any(Utente.class))).thenReturn(salvato);

        ResponseEntity<?> response = utenteService.registra(utente);

        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        assertEquals(1L, body.get("id"));
        assertEquals("Mario", body.get("nome"));
        assertEquals("Rossi", body.get("cognome"));
        assertEquals("mario@example.com", body.get("email"));

        verify(passwordEncoder).encode("Password123");
        verify(utenteRepository).save(any(Utente.class));
    }

    @Test
    void login_emailObbligatoria_restituisce400() {
        Utente utenteLogin = new Utente();
        utenteLogin.setEmail("");
        utenteLogin.setPassword("Password123");

        ResponseEntity<?> response = utenteService.login(utenteLogin, session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email obbligatoria", response.getBody());
        verify(utenteRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_passwordObbligatoria_restituisce400() {
        Utente utenteLogin = new Utente();
        utenteLogin.setEmail("mario@example.com");
        utenteLogin.setPassword("");

        ResponseEntity<?> response = utenteService.login(utenteLogin, session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Password obbligatoria", response.getBody());
        verify(utenteRepository, never()).findByEmail(any());
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_emailNonTrovata_restituisce400() {
        Utente utenteLogin = new Utente();
        utenteLogin.setEmail("mario@example.com");
        utenteLogin.setPassword("Password123");

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.empty());

        ResponseEntity<?> response = utenteService.login(utenteLogin, session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Email non trovata", response.getBody());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(passwordEncoder, never()).matches(any(), any());
    }

    @Test
    void login_passwordErrata_restituisce400() {
        Utente utenteLogin = new Utente();
        utenteLogin.setEmail("mario@example.com");
        utenteLogin.setPassword("Password123");

        Utente utenteDb = new Utente("Mario", "Rossi", "mario@example.com", "HASH");
        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(utenteDb));
        when(passwordEncoder.matches("Password123", "HASH"))
                .thenReturn(false);

        ResponseEntity<?> response = utenteService.login(utenteLogin, session);

        assertEquals(400, response.getStatusCode().value());
        assertEquals("Password errata", response.getBody());
        verify(utenteRepository).findByEmail("mario@example.com");
        verify(passwordEncoder).matches("Password123", "HASH");
    }

    @Test
    void login_valido_restituisce200() throws Exception {
        Utente utenteLogin = new Utente();
        utenteLogin.setEmail("mario@example.com");
        utenteLogin.setPassword("Password123");

        Utente utenteDb = new Utente("Mario", "Rossi", "mario@example.com", "HASH");
        setId(utenteDb, 1L);

        when(utenteRepository.findByEmail("mario@example.com"))
                .thenReturn(Optional.of(utenteDb));
        when(passwordEncoder.matches("Password123", "HASH"))
                .thenReturn(true);

        ResponseEntity<?> response = utenteService.login(utenteLogin, session);

        assertEquals(200, response.getStatusCode().value());

        @SuppressWarnings("unchecked")
        Map<String, Object> body = (Map<String, Object>) response.getBody();

        assertNotNull(body);
        // assertEquals(1L, body.get("id"));
        verify(session).setAttribute("userId", 1L);
        assertEquals("Mario", body.get("nome"));
        assertEquals("Rossi", body.get("cognome"));
        assertEquals("mario@example.com", body.get("email"));

        verify(utenteRepository).findByEmail("mario@example.com");
        verify(passwordEncoder).matches("Password123", "HASH");
    }

    private void setId(Utente utente, Long id) throws Exception {
        Field field = Utente.class.getDeclaredField("id");
        field.setAccessible(true);
        field.set(utente, id);
    }
}