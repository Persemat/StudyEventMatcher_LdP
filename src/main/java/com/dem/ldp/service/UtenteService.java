package com.dem.ldp.service;

import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class UtenteService {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteService(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public ResponseEntity<?> registra(Utente utente) {
        String nome = utente.getNome();
        String cognome = utente.getCognome();
        String email = utente.getEmail();
        String password = utente.getPassword();

        if (nome == null || nome.isBlank()) {
            return ResponseEntity.badRequest().body("Nome obbligatorio");
        }

        if (cognome == null || cognome.isBlank()) {
            return ResponseEntity.badRequest().body("Cognome obbligatorio");
        }

        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body("Email non valida");
        }

        if (utenteRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email già registrata");
        }

        if (password == null || password.length() < 8) {
            return ResponseEntity.badRequest().body("Password troppo corta");
        }

        if (!password.matches(".*[A-Z].*")) {
            return ResponseEntity.badRequest().body("Serve una maiuscola");
        }

        if (!password.matches(".*\\d.*")) {
            return ResponseEntity.badRequest().body("Serve un numero");
        }

        utente.setPassword(passwordEncoder.encode(password));
        Utente salvato = utenteRepository.save(utente);

        return ResponseEntity.ok(
                Map.of(
                        "id", salvato.getId(),
                        "nome", salvato.getNome(),
                        "cognome", salvato.getCognome(),
                        "email", salvato.getEmail()));
    }

    public List<Utente> getAllUtenti() {
        return utenteRepository.findAll();
    }

    public ResponseEntity<?> login(Utente utenteLogin, HttpSession session) { // <--- Aggiungi HttpSession
        String email = utenteLogin.getEmail();
        String password = utenteLogin.getPassword();

        if (email == null || email.isBlank()) {
            return ResponseEntity.badRequest().body("Email obbligatoria");
        }

        if (password == null || password.isBlank()) {
            return ResponseEntity.badRequest().body("Password obbligatoria");
        }

        var utenteTrovato = utenteRepository.findByEmail(email);
        if (utenteTrovato.isEmpty()) {
            return ResponseEntity.status(400).body("Email non trovata"); // Meglio 401 che 400 per la sicurezza
        }

        Utente utente = utenteTrovato.get();

        if (!passwordEncoder.matches(password, utente.getPassword())) {
            return ResponseEntity.status(400).body("Password errata");
        }

        // Salviamo l'ID nella sessione sul SERVER.
        // Il client riceverà solo un cookie criptato (JSESSIONID) che punta a questa
        // riga.
        session.setAttribute("userId", utente.getId());

        return ResponseEntity.ok(
                Map.of(
                        "nome", utente.getNome(),
                        "cognome", utente.getCognome(),
                        "email", utente.getEmail()
                // Non serve più mandare l'ID al frontend!
                ));
    }

    public ResponseEntity<?> getLoggedUser(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Sessione non valida o scaduta");
        }

        Optional<Utente> userOpt = utenteRepository.findById(userId);

        if (userOpt.isEmpty()) {
            return ResponseEntity.status(404).body("Utente non trovato");
        }
        Utente user = userOpt.get();
        Map<String, String> response = Map.of(
                "nome", user.getNome(),
                "cognome", user.getCognome(),
                "email", user.getEmail());
        return ResponseEntity.ok(response);

    }

}