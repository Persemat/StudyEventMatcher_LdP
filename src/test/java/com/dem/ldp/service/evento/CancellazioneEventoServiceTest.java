package com.dem.ldp.service.evento;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;


import java.util.HashSet;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CancellazioneEventoServiceTest extends BaseEventoServiceTest  {


    @Test
    void cancellaEventoCreato_presenzaCreatoreObbligatoria_eliminaEvento() {
        Utente creatore = new Utente();
        creatore.setId(1L);
        creatore.setNome("Luca");
        creatore.setCognome("Rossi");

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Evento test");
        evento.setCreatore(creatore);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setPartecipanti(new HashSet<>());

        evento.aggiungiPartecipante(creatore);

        for (long i = 2; i <= 6; i++) {
            Utente u = new Utente();
            u.setId(i);
            u.setNome("Utente" + i);
            evento.aggiungiPartecipante(u);
        }

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        String risultato = eventoService.cancellaEventoCreato(10L, session);

        assertEquals("Evento cancellato con successo", risultato);
        verify(eventoRepository).delete(evento);
        assertTrue(evento.getPartecipanti().isEmpty());
    }

    @Test
    void cancellaEventoCreato_senzaPresenzaObbligatoria_conAltriIscritti_rimuoveCreatoreERiassegna() {
        Utente creatore = new Utente();
        creatore.setId(1L);
        creatore.setNome("Luca");

        Utente utente2 = new Utente();
        utente2.setId(2L);
        utente2.setNome("Marco");

        Utente utente3 = new Utente();
        utente3.setId(3L);
        utente3.setNome("Anna");

        Evento evento = new Evento();
        evento.setId(11L);
        evento.setTitolo("Evento test");
        evento.setCreatore(creatore);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setPartecipanti(new HashSet<>());

        evento.aggiungiPartecipante(creatore);
        evento.aggiungiPartecipante(utente2);
        evento.aggiungiPartecipante(utente3);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(11L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        String risultato = eventoService.cancellaEventoCreato(11L, session);

        assertEquals("Creatore rimosso e ruolo riassegnato con successo", risultato);
        assertEquals(utente2, evento.getCreatore());
        assertFalse(evento.getPartecipanti().contains(creatore));
        assertTrue(evento.getPartecipanti().contains(utente2));
        assertTrue(evento.getPartecipanti().contains(utente3));
        verify(eventoRepository, never()).delete(any());
        verify(eventoRepository).save(evento);
    }

    @Test
    void cancellaEventoCreato_senzaPresenzaObbligatoria_senzaAltriIscritti_eliminaEvento() {
        Utente creatore = new Utente();
        creatore.setId(1L);
        creatore.setNome("Luca");

        Evento evento = new Evento();
        evento.setId(12L);
        evento.setTitolo("Evento test");
        evento.setCreatore(creatore);
        evento.setPartecipazioneCreatoreNecessaria(false);
        evento.setPartecipanti(new HashSet<>());

        evento.aggiungiPartecipante(creatore);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(12L)).thenReturn(Optional.of(evento));

        String risultato = eventoService.cancellaEventoCreato(12L, session);

        assertEquals("Evento cancellato con successo", risultato);
        verify(eventoRepository).delete(evento);
    }

    @Test
    void cancellaEventoCreato_utenteNonCreatore_lanciaEccezione() {
        Utente creatore = new Utente();
        creatore.setId(2L);
        creatore.setNome("Marco");

        Evento evento = new Evento();
        evento.setId(13L);
        evento.setCreatore(creatore);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setPartecipanti(new HashSet<>());

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(13L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventoService.cancellaEventoCreato(13L, session));

        assertEquals("Solo il creatore può cancellare l'evento", ex.getMessage());
        verify(eventoRepository, never()).delete(any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void cancellaEventoCreato_eventoNonTrovato_lanciaEccezione() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventoService.cancellaEventoCreato(99L, session));

        assertEquals("Evento non trovato", ex.getMessage());
        verify(eventoRepository, never()).delete(any());
    }

    @Test
    void cancellaEventoCreato_sessioneMancante_lanciaEccezione() {
        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> eventoService.cancellaEventoCreato(10L, session));

        assertEquals("Sessione non valida o scaduta", ex.getMessage());
        verify(eventoRepository, never()).findById(anyLong());
    }
}