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

@RestController
@RequestMapping("/Eventi")
@CrossOrigin(origins = "http://localhost:5173") // Permette le richieste da React
public class EventoController {
    private final EventoService eventoService;

    public EventoController(EventoService eventoService) {
        this.eventoService = eventoService;
    }

    @PostMapping
    public ResponseEntity<?> creaEvento(@RequestBody EventoRequest request) {
        // Validazione campi obbligatori 
        if(request.titolo() == null || request.titolo().isBlank() ||
            request.dataOra() == null ||
            request.luogo() == null || request.luogo().isBlank() || 
            request.tag() == null || request.tag().isEmpty()) {
            return ResponseEntity.badRequest().body("Errore: Campi obbligatori mancanti (Titolo, data/ora, luogo e tag)");
        }
    
        try{
            // Passa al Service
            Evento eventoCreato = eventoService.creaNuovoEvento(request);
            return ResponseEntity.ok("Evento '" + eventoCreato.getTitolo() + "' creato con successo!");
        }catch(RuntimeException e){
            return ResponseEntity.status(404).body("Errore: " + e.getMessage());
        }
    }

}