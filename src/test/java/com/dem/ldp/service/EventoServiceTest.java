package com.dem.ldp.service;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.dto.EventoResponse;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;

@ExtendWith(MockitoExtension.class)
public class EventoServiceTest {

    @InjectMocks
    private EventoService eventoService;

    @Mock
    private EventoRepository eventoRepository;

    @Mock
    private UtenteRepository utenteRepository;

    @Mock
    private jakarta.servlet.http.HttpSession session;

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
    void getAllEvents_utenteAutenticato_senzaEventiPersonali_restituisceListaVuotaPerGliEventiCreatiDaMe() {
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
    void getAllEvents_eventiPersonali_contengonoTitoloDataLuogoETag() {
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