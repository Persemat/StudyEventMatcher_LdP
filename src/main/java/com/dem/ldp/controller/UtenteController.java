package com.dem.ldp.controller;

import com.dem.ldp.dto.LoginRequest;
import com.dem.ldp.dto.UtenteRequest;
import com.dem.ldp.dto.UtenteResponse;
import com.dem.ldp.service.UtenteService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/Utenti")
@CrossOrigin(origins = "http://localhost:5173")
public class UtenteController {

    private final UtenteService utenteService;

    public UtenteController(UtenteService utenteService) {
        this.utenteService = utenteService;
    }

    @PostMapping
    public ResponseEntity<?> createUtente(@RequestBody UtenteRequest request) {
        try {
            UtenteResponse response = utenteService.registra(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request, HttpSession session) {
        try {
            UtenteResponse response = utenteService.login(request);
            session.setAttribute("userId", response.id());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/me")
    public ResponseEntity<?> getLoggedUser(HttpSession session) {
        try {
            Long userId = (Long) session.getAttribute("userId");
            UtenteResponse response = utenteService.getLoggedUser(userId);
            return ResponseEntity.ok(response);
        } catch (IllegalStateException e) {
            return ResponseEntity.status(401).body(e.getMessage());
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(404).body(e.getMessage());
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<?> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);

        if (session != null) {
            try {
                session.invalidate();
                return ResponseEntity.ok("Logout effettuato con successo");
            } catch (IllegalStateException e) {
                // La sessione era già invalida: per l'utente è come se il logout fosse riuscito
                return ResponseEntity.ok("Sessione già terminata");
            }
        }

        return ResponseEntity.ok("Nessuna sessione attiva, utente già sloggato");
    }

}
