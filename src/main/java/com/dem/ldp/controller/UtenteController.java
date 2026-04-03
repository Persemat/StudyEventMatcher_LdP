package com.dem.ldp.controller;

import com.dem.ldp.model.Utente;
import com.dem.ldp.service.UtenteService;
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

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utente utenteLogin) {
        return utenteService.login(utenteLogin);
    }
}