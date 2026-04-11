package com.dem.ldp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.model.Evento;
import com.dem.ldp.service.EventoService;

import jakarta.servlet.http.HttpSession;

@RestController
@RequestMapping("/Eventi")
@CrossOrigin(origins = "http://localhost:5173", allowCredentials = "true") // Permette le richieste da React
public class EventoController {
    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @GetMapping
    public ResponseEntity<?> getAllEvents(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Sessione non valida o scaduta");
        }

        return ResponseEntity.ok(eventoService.getAllEvents(session));
    }

    @PostMapping
    public ResponseEntity<?> creaEvento(@RequestBody EventoRequest request, HttpSession session) {
        // Validazione campi obbligatori

        if (request.titolo() == null || request.titolo().isBlank() ||
                request.dataOra() == null || request.durata() == null ||
                request.luogo() == null || request.luogo().isBlank() ||
                request.tag() == null || request.tag().isEmpty()) {

            // Usa una stringa SEMPLICE senza troppe parentesi per il test
            return ResponseEntity.badRequest().body("Campi obbligatori mancanti");
        }

        try {
            // Passa al Service
            Evento eventoCreato = eventoService.creaNuovoEvento(request, session);
            return ResponseEntity.ok("Evento '" + eventoCreato.getTitolo() + "' creato con successo!");
        } catch (RuntimeException e) {
            // Stampiamo l'errore nel terminale per aiutare il debug!
            System.out.println("⚠️ ERRORE INTERNO NEL SERVICE: " + e.getMessage());
            e.printStackTrace();

            return ResponseEntity.status(404).body("Errore: " + e.getMessage());
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> cancellaEvento(@PathVariable long id, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            return ResponseEntity.status(401).body("Sessione non valida o scaduta");
        }

        try {
            String messaggio = eventoService.cancellaEventoCreato(id, session);
            return ResponseEntity.ok(messaggio);
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest().body("Errore: " + e.getMessage());
        }
    }

}