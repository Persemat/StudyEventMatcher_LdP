package com.dem.ldp.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
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
@CrossOrigin(origins = "http://localhost:5173") // Permette le richieste da React
public class EventoController {
    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
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

}