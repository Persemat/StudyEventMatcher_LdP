package com.dem.ldp.dto;

import java.time.LocalDateTime;
import java.util.List;

public record EventoResponse(
        Long id,
        String titolo,
        LocalDateTime dataOra,
        Double durata,
        String luogo,
        List<String> tag,
        String descrizione,
        Integer maxPartecipanti,
        boolean partecipazioneCreatoreNecessaria,
        String stato,
        CreatoreResponse creatore,
        Integer currentParticipants,
        boolean isBooked,
        boolean isCreatedByMe) {
            
            public record CreatoreResponse(
            Long id,
            String nome,
            String cognome) {
    }
        }