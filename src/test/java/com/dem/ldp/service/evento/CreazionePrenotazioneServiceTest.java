package com.dem.ldp.service.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.HashSet;
import java.util.Optional;

import org.junit.jupiter.api.Test;

import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

public class CreazionePrenotazioneServiceTest extends BaseEventoServiceTest {

    @Test
    void testPrenotaEvento_Successo() {
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

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        eventoService.prenotaEvento(10L, session);

        assertEquals(2, evento.getPartecipanti().size());
        verify(eventoRepository, times(1)).save(evento);
    }

    @Test
    void testPrenotaEvento_UtenteNonAutenticato() {
        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.prenotaEvento(10L, session);
        });

        assertEquals("Utente non autenticato", ex.getMessage());
        verify(eventoRepository, never()).findById(any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testPrenotaEvento_UtenteNonTrovato() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.prenotaEvento(10L, session);
        });

        assertEquals("Utente non trovato", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testPrenotaEvento_EventoNonTrovato() {
        Utente utente = new Utente();
        utente.setId(1L);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.prenotaEvento(10L, session);
        });

        assertEquals("Evento non trovato", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testPrenotaEvento_GiaPrenotato() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Studio Java");
        evento.setMaxPartecipanti(5);
        evento.setStato("aperto");
        evento.setPartecipanti(new HashSet<>());
        evento.aggiungiPartecipante(utente);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.prenotaEvento(10L, session);
        });

        assertEquals("Sei già prenotato a questo evento", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testPrenotaEvento_EventoAlCompleto() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");

        Utente creatore = new Utente();
        creatore.setId(2L);
        creatore.setNome("Luca");

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Progetto Basi di Dati");
        evento.setMaxPartecipanti(1);
        evento.setStato("aperto");
        evento.setPartecipanti(new HashSet<>());
        evento.aggiungiPartecipante(creatore);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.prenotaEvento(10L, session);
        });

        assertEquals("Evento al completo", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testPrenotaEvento_EventoChiuso() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");

        Utente creatore = new Utente();
        creatore.setId(2L);
        creatore.setNome("Luca");

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Studio Reti");
        evento.setMaxPartecipanti(5);
        evento.setStato("chiuso");
        evento.setPartecipanti(new HashSet<>());
        evento.aggiungiPartecipante(creatore);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(utente));
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.prenotaEvento(10L, session);
        });

        assertEquals("Le prenotazioni per questo evento non sono disponibili", ex.getMessage());
        verify(eventoRepository, never()).save(any());
    }
}