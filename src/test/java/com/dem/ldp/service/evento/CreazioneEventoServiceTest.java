package com.dem.ldp.service.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

public class CreazioneEventoServiceTest extends BaseEventoServiceTest  {
    @Test
    void testCreaNuovoEvento_Successo() {
        EventoRequest request = new EventoRequest(
                "Matematica",
                LocalDateTime.parse("2026-10-01T15:00:00"),
                2.0,
                "Biblioteca Comunale",
                List.of("matematica"),
                "Ripasso",
                5,
                true
        );

        Utente creatore = new Utente();
        creatore.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(creatore));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        Evento risultato = eventoService.creaNuovoEvento(request, session);

        assertNotNull(risultato);
        assertEquals("Matematica", risultato.getTitolo());
        assertEquals("aperto", risultato.getStato());
        assertEquals(creatore, risultato.getCreatore());
        verify(eventoRepository, times(1)).save(any(Evento.class));
    }

    @Test
    void testCreaNuovoEvento_SessioneMancante() {
        EventoRequest request = new EventoRequest(
                "Matematica",
                LocalDateTime.now(),
                2.0,
                "Aula 1",
                List.of("test"),
                "desc",
                5,
                true
        );

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.creaNuovoEvento(request, session);
        });

        assertEquals("Sessione non valida o scaduta", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testCreaNuovoEvento_UtenteNonTrovato() {
        EventoRequest request = new EventoRequest(
                "Studio Analisi",
                LocalDateTime.parse("2026-04-10T15:00:00"),
                2.0,
                "Aula 1",
                List.of("Matematica"),
                "Ripasso",
                5,
                true
        );

        when(session.getAttribute("userId")).thenReturn(99L);
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.creaNuovoEvento(request, session);
        });

        assertTrue(ex.getMessage().contains("Utente non trovato"));
        verify(eventoRepository, never()).save(any());
    }
}
