package com.dem.ldp.dto;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Questo record rappresenta il "modulo" che React invia al server.
 */
public record EventoRequest(
    String titolo,
    LocalDateTime dataOra,
    Double durata,
    String luogo,
    List<String> tag,
    String descrizione,
    Integer maxPartecipanti,
    boolean partecipazioneCreatoreNecessaria,
    Long creatoreId // Usiamo l'ID invece dell'intero oggetto Utente
) {}