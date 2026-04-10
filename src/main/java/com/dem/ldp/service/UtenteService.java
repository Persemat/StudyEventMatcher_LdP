package com.dem.ldp.service;

import com.dem.ldp.dto.LoginRequest;
import com.dem.ldp.dto.UtenteRequest;
import com.dem.ldp.dto.UtenteResponse;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UtenteResponse registra(UtenteRequest request) {
        String nome = request.nome();
        String cognome = request.cognome();
        String email = request.email();
        String password = request.password();

        if (nome == null || nome.isBlank()) {
            throw new IllegalArgumentException("Nome obbligatorio");
        }

        if (cognome == null || cognome.isBlank()) {
            throw new IllegalArgumentException("Cognome obbligatorio");
        }

        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email non valida");
        }

        if (utenteRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email già registrata");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password troppo corta");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Serve una maiuscola");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Serve un numero");
        }

        Utente utente = new Utente();
        utente.setNome(nome);
        utente.setCognome(cognome);
        utente.setEmail(email);
        utente.setPassword(passwordEncoder.encode(password));

        Utente salvato = utenteRepository.save(utente);
        return toResponse(salvato);
    }

    public UtenteResponse login(LoginRequest request) {
    String email = request.email();
    String password = request.password();

    if (email == null || email.isBlank()) {
        throw new IllegalArgumentException("Email obbligatoria");
    }

    if (password == null || password.isBlank()) {
        throw new IllegalArgumentException("Password obbligatoria");
    }

    Utente utente = utenteRepository.findByEmail(email)
            .orElseThrow(() -> new IllegalArgumentException("Credenziali non valide"));

    if (!passwordEncoder.matches(password, utente.getPassword())) {
        throw new IllegalArgumentException("Credenziali non valide");
    }

    return toResponse(utente);
    }

    public UtenteResponse getLoggedUser(Long userId) {
    if (userId == null) {
        throw new IllegalStateException("Sessione non valida o scaduta");
    }

    Utente user = utenteRepository.findById(userId)
            .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

    return toResponse(user);
    }

    private UtenteResponse toResponse(Utente utente) {
        return new UtenteResponse(
                utente.getId(),
                utente.getNome(),
                utente.getCognome(),
                utente.getEmail()
        );
    }
}