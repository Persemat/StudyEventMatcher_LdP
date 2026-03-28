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
public class UtenteController {

    private final UtenteRepository utenteRepository;
    private final PasswordEncoder passwordEncoder;


    public UtenteController(UtenteRepository utenteRepository, PasswordEncoder passwordEncoder) {
        this.utenteRepository = utenteRepository;
        this.passwordEncoder = passwordEncoder;

    }

@PostMapping
public ResponseEntity<?> createUtente(@RequestBody Utente utente) {

    String email = utente.getEmail();
    String password = utente.getPassword();

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
            "email", salvato.getEmail()
        )
    );
}
    // per verificare che la password inserita sia giusta nel login
    // encoder.matches("password inserita", "password crypatata del database");
    @GetMapping
    public List<Utente> getAllUtentes() {
        return utenteRepository.findAll();
    }
}