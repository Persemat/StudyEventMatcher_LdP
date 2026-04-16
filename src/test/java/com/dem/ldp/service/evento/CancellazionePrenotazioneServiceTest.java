package com.dem.ldp.service.evento;

import static org.junit.Assert.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

public class CancellazionePrenotazioneServiceTest extends BaseEventoServiceTest{

    @Test
    void testCancellaPrenotazione_Successo() {
        // Simula un utente prenotato all'evento
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");

        Utente creatore = new Utente();
        creatore.setId(2L);
        creatore.setNome("Luca");

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Studio Ingegneria del Software");
        evento.setMaxPartecipanti(3);
        evento.setStato("aperto");
        evento.setPartecipanti(new HashSet<>());
        evento.aggiungiPartecipante(creatore);
        evento.aggiungiPartecipante(utente);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        // Esegui la cancellazione della prenotazione
        eventoService.cancellaPrenotazione(10L, session);

        // Verifica che l'utente sia stato rimosso dai partecipanti
        assertEquals(1, evento.getPartecipanti().size());
        assertFalse(evento.getPartecipanti().contains(utente));
    }

    @Test
    void testCancellaPrenotazione_UtenteNonAutenticato() {
        // Sessione vuota
        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.cancellaPrenotazione(10L, session);
        });

        assertEquals("Il sistema richiede l'autenticazione", ex.getMessage());
        
        // Verifichiamo che il DB non venga mai toccato
        verify(eventoRepository, never()).findById(any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testCancellaPrenotazione_UtenteNonTrovato() {
        when(session.getAttribute("userId")).thenReturn(1L);
        // DB non trova l'utente
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.cancellaPrenotazione(10L, session);
        });

        assertEquals("Utente non trovato", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testCancellaPrenotazione_EventoNonTrovato() {
        Utente utente = new Utente();
        utente.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        // DB non trova l'evento
        when(eventoRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.cancellaPrenotazione(10L, session);
        });

        assertEquals("Evento non trovato", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testCancellaPrenotazione_UtenteNonPrenotato() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Studio Reti");
        evento.setPartecipanti(new HashSet<>());
        // ATTENZIONE: Mario NON è iscritto a questo evento

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.cancellaPrenotazione(10L, session);
        });

        // Il messaggio esatto richiesto dalla User Story di Cucumber
        assertEquals("Non sei prenotato a questo evento", ex.getMessage());
        
        // L'evento non deve essere salvato perché c'è stato un errore
        verify(eventoRepository, never()).save(any());
    }

}
