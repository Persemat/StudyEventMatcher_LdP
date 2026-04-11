package com.dem.ldp.service.evento;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.dem.ldp.dto.EventoResponse;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;


public class EventiPersonaliServiceTest extends BaseEventoServiceTest {
    @Test
    void getAllEvents_utenteAutenticato_conEventiPersonali_restituisceSoloEventiDellUtente() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@test.com");
        utente.setPassword("Password123!");

        Utente altroUtente = new Utente();
        altroUtente.setId(2L);
        altroUtente.setNome("Luigi");
        altroUtente.setCognome("Verdi");
        altroUtente.setEmail("luigi@test.com");
        altroUtente.setPassword("Password123!");

        Evento eventoMio = new Evento();
        eventoMio.setTitolo("Evento Mio");
        eventoMio.setDataOra(LocalDateTime.of(2026, 4, 20, 15, 0));
        eventoMio.setLuogo("Aula 1");
        eventoMio.setDurata(2.0);
        eventoMio.setTag(List.of("Java"));
        eventoMio.setDescrizione("Descrizione");
        eventoMio.setStato("aperto");
        eventoMio.setCreatore(utente);

        Evento eventoAltro = new Evento();
        eventoAltro.setTitolo("Evento Altro");
        eventoAltro.setDataOra(LocalDateTime.of(2026, 4, 21, 16, 0));
        eventoAltro.setLuogo("Aula 2");
        eventoAltro.setDurata(1.5);
        eventoAltro.setTag(List.of("React"));
        eventoAltro.setDescrizione("Descrizione");
        eventoAltro.setStato("aperto");
        eventoAltro.setCreatore(altroUtente);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findAll()).thenReturn(List.of(eventoMio, eventoAltro));

        List<EventoResponse> result = eventoService.getAllEvents(session);

        assertEquals(2, result.size());
        assertTrue(result.stream().anyMatch(EventoResponse::isCreatedByMe));
        assertTrue(result.stream().anyMatch(e -> !e.isCreatedByMe()));
    }
    @Test
    void get_utenteAutenticato_senzaEventiPersonali_restituisceListaVuotaPerGliEventiCreatiDaMe() {
        Utente altroUtente = new Utente();
        altroUtente.setId(2L);
        altroUtente.setNome("Luigi");
        altroUtente.setCognome("Verdi");
        altroUtente.setEmail("luigi@test.com");
        altroUtente.setPassword("Password123!");

        Evento eventoAltro = new Evento();
        eventoAltro.setTitolo("Evento Altro");
        eventoAltro.setDataOra(LocalDateTime.of(2026, 4, 21, 16, 0));
        eventoAltro.setLuogo("Aula 2");
        eventoAltro.setDurata(1.5);
        eventoAltro.setTag(List.of("React"));
        eventoAltro.setDescrizione("Descrizione");
        eventoAltro.setStato("aperto");
        eventoAltro.setCreatore(altroUtente);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findAll()).thenReturn(List.of(eventoAltro));

        List<EventoResponse> result = eventoService.getAllEvents(session);

        assertEquals(1, result.size());
        assertTrue(result.stream().noneMatch(EventoResponse::isCreatedByMe));
    }

    @Test
    void getEventiPersonali_contengonoTitoloDataLuogoETag() {
        Utente utente = new Utente();
        utente.setId(1L);
        utente.setNome("Mario");
        utente.setCognome("Rossi");
        utente.setEmail("mario@test.com");
        utente.setPassword("Password123!");

        Evento evento = new Evento();
        evento.setTitolo("Studio Basi di Dati");
        evento.setDataOra(LocalDateTime.of(2026, 4, 10, 15, 0));
        evento.setLuogo("Aula 2");
        evento.setDurata(2.0);
        evento.setTag(List.of("Informatica"));
        evento.setDescrizione("Ripasso SQL");
        evento.setStato("aperto");
        evento.setCreatore(utente);

        when(session.getAttribute("userId")).thenReturn(1L);
        when(eventoRepository.findAll()).thenReturn(List.of(evento));

        List<EventoResponse> result = eventoService.getAllEvents(session);

        assertEquals(1, result.size());
        EventoResponse response = result.get(0);
        assertNotNull(response.titolo());
        assertNotNull(response.dataOra());
        assertNotNull(response.luogo());
        assertNotNull(response.tag());
    }
}
