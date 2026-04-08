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
import org.springframework.test.util.ReflectionTestUtils;

import com.dem.ldp.dto.EventoRequest;
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
        // 1. DTO Aggiornato (Senza l'ID utente in fondo)
        EventoRequest request = new EventoRequest(
                "Matematica",
                LocalDateTime.parse("2026-10-01T15:00:00"),
                2.0,
                "Biblioteca Comunale",
                List.of("matematica"),
                "Ripasso",
                5,
                true);

        Utente creatore = new Utente();
        ReflectionTestUtils.setField(creatore, "id", 1L);

        // 2. SIMULIAMO LA SESSIONE
        // Quando il service chiede "userId" alla sessione, restituiamo 1L
        when(session.getAttribute("userId")).thenReturn(1L);
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(creatore));

        when(eventoRepository.save(any(Evento.class))).thenAnswer(i -> i.getArgument(0));

        // 3. ESECUZIONE (Passiamo anche il mock della sessione)
        Evento risultato = eventoService.creaNuovoEvento(request, session);

        // 4. VERIFICA
        assertNotNull(risultato);
        assertEquals("Matematica", risultato.getTitolo());
        assertEquals("aperto", risultato.getStato());
        assertEquals(creatore, risultato.getCreatore());
        verify(eventoRepository, times(1)).save(any(Evento.class));
    }

    @Test
    void testCreaNuovoEvento_SessioneMancante() {
        EventoRequest request = new EventoRequest(
                "Matematica", LocalDateTime.now(), 2.0, "Aula 1",
                List.of("test"), "desc", 5, true);

        // Simuliamo sessione vuota (userId non presente)
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
                "Studio Analisi", LocalDateTime.parse("2026-04-10T15:00:00"),
                2.0, "Aula 1", List.of("Matematica"), "Ripasso", 5, true);

        // Sessione valida, ma utente sparito dal DB
        when(session.getAttribute("userId")).thenReturn(99L);
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () -> {
            eventoService.creaNuovoEvento(request, session);
        });

        assertTrue(ex.getMessage().contains("Utente non trovato"));
        verify(eventoRepository, never()).save(any());
    }
}