package com.dem.ldp.service.evento;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;
import com.dem.ldp.dto.EventoResponse;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;

public class VisualizzazioneEventiServiceTest extends BaseEventoServiceTest {
    @Test
    void testGetAllEvents_RestituisceEventiMappatiCorrettamente() {
        Utente creatore = new Utente();
        creatore.setId(1L);
        creatore.setNome("Mario");
        creatore.setCognome("Rossi");

        Evento evento = new Evento();
        evento.setTitolo("Ripasso Analisi");
        evento.setDataOra(LocalDateTime.parse("2026-04-10T10:30:00"));
        evento.setLuogo("Aula 4");
        evento.setDurata(2.0);
        evento.setTag(List.of("Matematica"));
        evento.setDescrizione("Preparazione esame");
        evento.setStato("aperto");
        evento.setCreatore(creatore);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findAll()).thenReturn(List.of(evento));

        List<EventoResponse> result = eventoService.getAllEvents(session);

        assertEquals(1, result.size());
        assertEquals("Ripasso Analisi", result.get(0).titolo());
        assertEquals("Aula 4", result.get(0).luogo());
        assertEquals("Mario", result.get(0).creatore().nome());
        assertTrue(result.get(0).isCreatedByMe());
    }

    @Test
    void testGetAllEvents_ListaVuota() {
        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findAll()).thenReturn(List.of());

        List<EventoResponse> result = eventoService.getAllEvents(session);

        assertTrue(result.isEmpty());
    }

    
}
