package com.dem.ldp.service;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.dto.EventoResponse;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Service;

import java.util.List;

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

        Evento nuovoEvento = new Evento();
        nuovoEvento.setTitolo(request.titolo());
        nuovoEvento.setDataOra(request.dataOra());
        nuovoEvento.setDurata(request.durata());
        nuovoEvento.setLuogo(request.luogo());
        nuovoEvento.setTag(request.tag());
        nuovoEvento.setDescrizione(request.descrizione());
        nuovoEvento.setMaxPartecipanti(request.maxPartecipanti());
        nuovoEvento.setPartecipazioneCreatoreNecessaria(request.partecipazioneCreatoreNecessaria());

        // Se stato è String:
        nuovoEvento.setStato("aperto");

        // Se stato è enum, usa invece:
        // nuovoEvento.setStato(StatoEvento.APERTO);

        nuovoEvento.setCreatore(creatore);
        nuovoEvento.aggiungiPartecipante(creatore);

        return eventoRepository.save(nuovoEvento);
    }

    public List<EventoResponse> getAllEvents(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        return eventoRepository.findAll()
                .stream()
                .map(evento -> toResponse(evento, userId))
                .toList();
    }

    private EventoResponse toResponse(Evento evento, Long userId) {
        Utente creatore = evento.getCreatore();

        EventoResponse.CreatoreResponse creatoreResponse = new EventoResponse.CreatoreResponse(
                creatore.getId(),
                creatore.getNome(),
                creatore.getCognome()
        );
        boolean isBooked = userId != null &&
            evento.getPartecipanti().stream().anyMatch(u -> u.getId().equals(userId));
        return new EventoResponse(
                evento.getId(),
                evento.getTitolo(),
                evento.getDataOra(),
                evento.getDurata(),
                evento.getLuogo(),
                evento.getTag(),
                evento.getDescrizione(),
                evento.getMaxPartecipanti(),
                evento.isPartecipazioneCreatoreNecessaria(),
                evento.getStato().toString(),
                creatoreResponse,
                evento.getPartecipanti().size(),
                isBooked,
                userId != null && creatore.getId().equals(userId)
        );
    }
}