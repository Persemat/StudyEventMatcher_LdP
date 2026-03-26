package com.dem.ldp.controller;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;
import org.springframework.security.crypto.password.PasswordEncoder;



@RestController
@RequestMapping("/Utenti")
public class UtenteController {

    private final UtenteRepository UtenteRepository;
    private final PasswordEncoder passwordEncoder;


    public UtenteController(UtenteRepository UtenteRepository, PasswordEncoder passwordEncoder) {
        this.UtenteRepository = UtenteRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @PostMapping
    public Utente createUtente(@RequestBody Utente utente) {
        utente.setPassword(passwordEncoder.encode(utente.getPassword()));
        return UtenteRepository.save(utente);
    }
    // per verificare che la password inserita sia giusta nel login
    // encoder.matches("password inserita", "password crypatata del database");
    @GetMapping
    public List<Utente> getAllUtentes() {
        return UtenteRepository.findAll();
    }
}