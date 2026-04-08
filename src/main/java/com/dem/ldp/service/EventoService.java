package com.dem.ldp.service;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class EventoService {

    private final EventoRepository eventoRepository;
    private final UtenteRepository utenteRepository;

    public EventoService(EventoRepository eventoRepository, UtenteRepository utenteRepository) {
        this.eventoRepository = eventoRepository;
        this.utenteRepository = utenteRepository;
    }

    public Evento creaNuovoEvento(EventoRequest request, HttpSession session) {

        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione non valida o scaduta");
        }

        Utente creatore = utenteRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        // Creazione e mappatura dell'Entità
        Evento nuovoEvento = new Evento();
        nuovoEvento.setTitolo(request.titolo());
        nuovoEvento.setDataOra(request.dataOra());
        nuovoEvento.setDurata(request.durata());
        nuovoEvento.setLuogo(request.luogo());
        nuovoEvento.setTag(request.tag());
        nuovoEvento.setDescrizione(request.descrizione());
        nuovoEvento.setMaxPartecipanti(request.maxPartecipanti());
        nuovoEvento.setPartecipazioneCreatoreNecessaria(request.partecipazioneCreatoreNecessaria());

        nuovoEvento.setStato("aperto");
        nuovoEvento.setCreatore(creatore);

        // Salvataggio tramite Repository
        return eventoRepository.save(nuovoEvento);
    }
}
