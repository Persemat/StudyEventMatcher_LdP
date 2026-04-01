package com.dem.ldp.controller;

import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.UtenteRepository;
import com.dem.ldp.service.UtenteService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/Utenti")
public class UtenteController {

    private final UtenteRepository utenteRepository;
    private final UtenteService utenteService;

    public UtenteController(UtenteRepository utenteRepository, UtenteService utenteService) {
        this.utenteRepository = utenteRepository;
        this.utenteService = utenteService;
    }

    @PostMapping
    public ResponseEntity<?> createUtente(@RequestBody Utente utente) {
        return utenteService.registra(utente);
    }

    @GetMapping
    public List<Utente> getAllUtentes() {
        return utenteRepository.findAll();
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Utente utenteLogin) {
        return utenteService.login(utenteLogin);
    }
}