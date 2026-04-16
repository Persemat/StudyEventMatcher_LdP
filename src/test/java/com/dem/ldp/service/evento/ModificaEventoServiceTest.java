package com.dem.ldp.service.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.junit.jupiter.api.Test;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

public class ModificaEventoServiceTest extends BaseEventoServiceTest {

    @Test
    void testModificaEvento_Successo() {
        EventoRequest request = new EventoRequest(
                "Progetto Java Aggiornato",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Biblioteca",
                List.of("Java", "Backend"),
                "Descrizione aggiornata",
                12,
                false
        );

        Utente creatore = new Utente();
        creatore.setId(1L);

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.parse("2026-10-15T15:00:00"));
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setCreatore(creatore);
        evento.setPartecipanti(new HashSet<>(Set.of(creatore)));

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));
        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        Evento risultato = eventoService.modificaEvento(10L, request, session);

        assertNotNull(risultato);
        assertEquals("Progetto Java Aggiornato", risultato.getTitolo());
        assertEquals(LocalDateTime.parse("2026-10-16T17:00:00"), risultato.getDataOra());
        assertEquals(3.0, risultato.getDurata());
        assertEquals("Biblioteca", risultato.getLuogo());
        assertEquals(List.of("Java", "Backend"), risultato.getTag());
        assertEquals("Descrizione aggiornata", risultato.getDescrizione());
        assertEquals(12, risultato.getMaxPartecipanti());
        assertEquals(false, risultato.isPartecipazioneCreatoreNecessaria());

        verify(eventoRepository, times(1)).save(any(Evento.class));
    }

    @Test
    void testModificaEvento_SessioneMancante() {
        EventoRequest request = new EventoRequest(
                "Progetto Java Aggiornato",
                LocalDateTime.now().plusDays(2),
                3.0,
                "Biblioteca",
                List.of("Java"),
                "Descrizione aggiornata",
                12,
                false
        );

        when(session.getAttribute("userId")).thenReturn(null);

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.modificaEvento(10L, request, session);
        });

        assertEquals("Sessione non valida o scaduta", ex.getMessage());
        verify(eventoRepository, never()).findById(any());
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testModificaEvento_EventoNonTrovato() {
        EventoRequest request = new EventoRequest(
                "Progetto Java Aggiornato",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Biblioteca",
                List.of("Java"),
                "Descrizione aggiornata",
                12,
                false
        );

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.modificaEvento(10L, request, session);
        });

        assertTrue(ex.getMessage().contains("Evento non trovato"));
        verify(eventoRepository, never()).save(any());
    }

    @Test
    void testModificaEvento_UtenteNonCreatore() {
        EventoRequest request = new EventoRequest(
                "Progetto Java Aggiornato",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Biblioteca",
                List.of("Java"),
                "Descrizione aggiornata",
                12,
                false
        );

        Utente creatore = new Utente();
        creatore.setId(2L);

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Progetto Java");
        evento.setCreatore(creatore);
        evento.setPartecipanti(new HashSet<>(Set.of(creatore)));

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.modificaEvento(10L, request, session);
        });

        assertTrue(ex.getMessage().contains("Solo il creatore può modificare l'evento"));
        verify(eventoRepository, never()).save(any());
    }

   @Test
    void testModificaEvento_MaxPartecipantiMinoreDeiPartecipantiAttuali() {
        EventoRequest request = new EventoRequest(
                "Progetto Java Aggiornato",
                LocalDateTime.parse("2026-10-16T17:00:00"),
                3.0,
                "Biblioteca",
                List.of("Java"),
                "Descrizione aggiornata",
                2,
                false
        );

        Utente creatore = new Utente();
        creatore.setId(1L);

        Utente partecipante1 = new Utente();
        partecipante1.setId(2L);

        Utente partecipante2 = new Utente();
        partecipante2.setId(3L);

        Evento evento = new Evento();
        evento.setId(10L);
        evento.setTitolo("Progetto Java");
        evento.setDataOra(LocalDateTime.now().plusDays(3)); // importante
        evento.setDurata(2.5);
        evento.setLuogo("Aula Magna");
        evento.setTag(List.of("Java"));
        evento.setDescrizione("Ripasso");
        evento.setMaxPartecipanti(10);
        evento.setPartecipazioneCreatoreNecessaria(true);
        evento.setStato("aperto");
        evento.setCreatore(creatore);

        evento.setPartecipanti(new HashSet<>());
        evento.getPartecipanti().add(creatore);
        evento.getPartecipanti().add(partecipante1);
        evento.getPartecipanti().add(partecipante2);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findById(10L)).thenReturn(Optional.of(evento));

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.modificaEvento(10L, request, session);
        });

        assertEquals(
                "Il numero massimo di partecipanti non può essere minore dei partecipanti attuali",
                ex.getMessage()
        );
        verify(eventoRepository, never()).save(any());
    }
}