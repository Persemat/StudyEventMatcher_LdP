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

    @Test
    void testCreaNuovoEvento_Successo(){

        EventoRequest request = new EventoRequest(
            "Matematica",
            LocalDateTime.parse("2024-10-01T15:00:00"),
            2.0,
            "Biblioteca Comunale",
            List.of("matematica"),
            "RIpasso",
            5,
            true,
            1L
        );

        //Creazione finto utente
        Utente creatore = new Utente();
        ReflectionTestUtils.setField(creatore, "id", 1L);
        creatore.setEmail("mario@gmail.it");

       
        //Quando ti cercano l'ID 1, restituisci il creatore
        when(utenteRepository.findById(1L)).thenReturn(Optional.of(creatore));
        
        //Quando provano a salvare un evento, restituisci semplicemente quello stesso evento
        when(eventoRepository.save(any(Evento.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Eseguiamo il vero metodo
        Evento risultato = eventoService.creaNuovoEvento(request);

        // Verifica
        // Controlliamo che il Service abbia fatto bene i compiti
        assertNotNull(risultato);
        assertEquals("Matematica", risultato.getTitolo());
        assertEquals("aperto", risultato.getStato()); // Verifica cruciale del criterio di accettazione!
        assertEquals(creatore, risultato.getCreatore());
        
        // Verifichiamo che il service abbia effettivamente chiamato il comando .save()
        verify(eventoRepository, times(1)).save(any(Evento.class));
    }

    @Test
    void testCreaNuovoEvento_UtenteNonTrovato() {
        // 1. PREPARAZIONE
        EventoRequest request = new EventoRequest(
                "Studio Analisi", 
                LocalDateTime.parse("2026-04-10T15:00:00"),
                 2.0, 
                 "Aula 1",
                List.of("Matematica"), 
                "Ripasso", 
                5, 
                true, 
                99L // ID inesistente!
        );

        //Non troviamo nessun utente con quell'Id
        when(utenteRepository.findById(99L)).thenReturn(Optional.empty());

        //Azione e verifica 
        //Ci aspettiamo una RuntimeException
        RuntimeException eccezione = assertThrows(RuntimeException.class, () -> {
            eventoService.creaNuovoEvento(request);
        });

        // Verifichiamo che il messaggio di errore sia corretto
        assertTrue(eccezione.getMessage().contains("Utente creatore non trovato con ID: 99"));
        
        // Verifichiamo che il salvataggio NON sia mai stato chiamato per errore
        verify(eventoRepository, never()).save(any());
    
    }

}
