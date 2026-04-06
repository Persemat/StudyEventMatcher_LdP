package com.dem.ldp.controller;

import com.dem.ldp.model.Utente;
import com.dem.ldp.service.UtenteService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Utenti")
@CrossOrigin(origins = "http://localhost:5173") // Permette le richieste da React
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @PostMapping
    public ResponseEntity<?> createUtente(@RequestBody Utente utente) {
        return utenteService.registra(utente);
    }

    @GetMapping
    public List<Utente> getAllUtentes() {
        return utenteService.getAllUtenti();
    }

    @GetMapping("/me")
    public ResponseEntity<?> getLoggedUser(HttpSession session) {
        return utenteService.getLoggedUser(session);
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utente utente, HttpSession session) {
        // 2. Passa la sessione al metodo del service
        return utenteService.login(utente, session);
    }
}