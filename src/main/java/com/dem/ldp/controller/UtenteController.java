package com.dem.ldp.controller;

import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.http.ResponseEntity;

@RestController
@RequestMapping("/Utenti")
@CrossOrigin(origins = "http://localhost:5173") // Permette le richieste da React
public class UtenteController {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UtenteController(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @PostMapping
    public ResponseEntity<?> createUtente(@RequestBody Utente utente) {

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

        if (utenteRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body("Email già registrata");
        }

        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            return ResponseEntity.badRequest().body("Email non valida");
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
                "email", salvato.getEmail()
            )
        );
    }

    @GetMapping
    public List<Utente> getAllUtentes() {
        return utenteRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utente utenteLogin) {

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
            return ResponseEntity.badRequest().body("Email non trovata");
        }

        Utente utente = utenteTrovato.get();

        if (!passwordEncoder.matches(password, utente.getPassword())) {
            return ResponseEntity.badRequest().body("Password errata");
        }

        return ResponseEntity.ok(
            Map.of(
                "id", utente.getId(),
                "nome", utente.getNome(),
                "cognome", utente.getCognome(),
                "email", utente.getEmail()
            )
        );
    }
}