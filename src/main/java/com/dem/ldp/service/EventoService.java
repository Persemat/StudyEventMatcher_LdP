package com.dem.ldp.service;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.dto.EventoResponse;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import jakarta.servlet.http.HttpSession;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class EventoService {
    private static final Logger logger = LoggerFactory.getLogger(EventoService.class);

    private final EventoRepository eventoRepository;
    private final UtenteRepository utenteRepository;
    private final EmailService emailService;

    public EventoService(EventoRepository eventoRepository, UtenteRepository utenteRepository,
            EmailService emailService) {
        this.eventoRepository = eventoRepository;
        this.utenteRepository = utenteRepository;
        this.emailService = emailService;
    }

    public Evento creaNuovoEvento(EventoRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione non valida o scaduta");
        }
        Utente creatore = utenteRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        boolean eventoGiaEsistente = eventoRepository
                .findByCreatoreIdAndTitoloAndDataOraAndLuogo(
                        creatore.getId(),
                        request.titolo(),
                        request.dataOra(),
                        request.luogo())
                .isPresent();

        if (eventoGiaEsistente) {
            throw new RuntimeException("Esiste già un evento identico creato da questo utente");
        }

        Evento nuovoEvento = new Evento();
        nuovoEvento.setTitolo(request.titolo());
        nuovoEvento.setDataOra(request.dataOra());
        nuovoEvento.setDurata(request.durata());
        nuovoEvento.setLuogo(request.luogo());

        if (request.tag() != null) {
            List<String> tagNormalizzati = request.tag().stream()
                    .filter(t -> t != null && !t.isBlank())
                    .map(t -> t.trim().toUpperCase())
                    .collect(Collectors.toList());

            nuovoEvento.setTag(tagNormalizzati);
        }

        nuovoEvento.setDescrizione(request.descrizione());
        nuovoEvento.setMaxPartecipanti(request.maxPartecipanti());
        nuovoEvento.setPartecipazioneCreatoreNecessaria(request.partecipazioneCreatoreNecessaria());
        nuovoEvento.setStato("aperto");

        nuovoEvento.setCreatore(creatore);
        nuovoEvento.aggiungiPartecipante(creatore);

        return eventoRepository.save(nuovoEvento);
    }

    @Transactional
    public Evento modificaEvento(Long id, EventoRequest request, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione non valida o scaduta");
        }

        Evento evento = eventoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        if (evento.getCreatore() == null || !evento.getCreatore().getId().equals(userId)) {
            throw new RuntimeException("Solo il creatore può modificare l'evento");
        }
        if (!evento.getDataOra().isAfter(LocalDateTime.now().plusHours(24))) {
            throw new RuntimeException("Non è possibile modificare un evento entro 24 ore dall'inizio");
        }
        // Controllo importante: maxPartecipanti non può essere minore dei partecipanti
        // attuali
        if (request.maxPartecipanti() != null &&
                request.maxPartecipanti() < evento.getPartecipanti().size()) {
            throw new RuntimeException(
                    "Il numero massimo di partecipanti non può essere minore dei partecipanti attuali");
        }

        evento.setTitolo(request.titolo());
        evento.setDataOra(request.dataOra());
        evento.setDurata(request.durata());
        evento.setLuogo(request.luogo());

        if (request.tag() != null) {
            List<String> tagNormalizzati = request.tag().stream()
                    .filter(t -> t != null && !t.isBlank())
                    .map(t -> t.trim().toUpperCase())
                    .collect(Collectors.toList());

            evento.setTag(tagNormalizzati);
        }

        evento.setDescrizione(request.descrizione());
        evento.setMaxPartecipanti(request.maxPartecipanti());
        evento.setPartecipazioneCreatoreNecessaria(request.partecipazioneCreatoreNecessaria());

        return eventoRepository.save(evento);
    }

    public List<EventoResponse> getAllEvents(HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        List<Evento> eventi = eventoRepository.findAll();

        boolean daSalvare = false;

        for (Evento evento : eventi) {
            if (evento.getDataOra().isBefore(java.time.LocalDateTime.now())
                    && evento.getStato().equals("aperto")) {
                evento.setStato("chiuso");
                daSalvare = true;
            }
        }

        if (daSalvare) {
            eventoRepository.saveAll(eventi);
        }

        return eventi.stream()
                .map(evento -> toResponse(evento, userId))
                .toList();
    }

    @Transactional
    public String cancellaEventoCreato(long eventoId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Sessione non valida o scaduta");
        }

        Evento evento = eventoRepository.findById(eventoId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        if (evento.getCreatore() == null || !evento.getCreatore().getId().equals(userId)) {
            throw new RuntimeException("Solo il creatore può cancellare l'evento");
        }

        Utente creatore = evento.getCreatore();

        if (evento.isPartecipazioneCreatoreNecessaria()) {
            try {
                emailService.inviaMailCancellazioneEventoAiPartecipanti(evento);
            } catch (Exception e) {
                logger.error("Errore durante l'invio delle mail di cancellazione per l'evento {}",
                        evento.getTitolo(), e);
            }

            evento.getPartecipanti().clear();
            eventoRepository.delete(evento);
            return "Evento cancellato con successo";
        }

        evento.rimuoviPartecipante(creatore);

        Set<Utente> partecipantiResidui = evento.getPartecipanti();

        if (partecipantiResidui.isEmpty()) {
            eventoRepository.delete(evento);
            return "Evento cancellato con successo";
        }

        Utente nuovoCreatore = new ArrayList<>(partecipantiResidui).stream()
                .sorted(Comparator.comparing(Utente::getId))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("Impossibile riassegnare il creatore"));

        evento.setCreatore(nuovoCreatore);
        eventoRepository.save(evento);

        return "Creatore rimosso e ruolo riassegnato con successo";
    }

    @Transactional
    public void prenotaEvento(Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        if (userId == null) {
            throw new RuntimeException("Utente non autenticato");
        }

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Evento evento = eventoRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        // Controllo: non puoi prenotarti due volte
        boolean isPrenotato = evento.getPartecipanti().stream()
                .anyMatch(partecipante -> partecipante.getId().equals(userId));

        if (isPrenotato) {
            throw new RuntimeException("Sei già prenotato a questo evento");
        }

        // Controllo: evento pieno
        if (evento.getMaxPartecipanti() != null &&
                evento.getPartecipanti().size() >= evento.getMaxPartecipanti()) {
            throw new RuntimeException("Evento al completo");
        }

        // Controllo opzionale: evento chiuso
        if (evento.getStato() != null && !"aperto".equalsIgnoreCase(evento.getStato().toString())) {
            throw new RuntimeException("Le prenotazioni per questo evento non sono disponibili");
        }

        evento.aggiungiPartecipante(utente);
        eventoRepository.save(evento);
    }

    @Transactional
    public void cancellaPrenotazione(Long eventId, HttpSession session) {
        Long userId = (Long) session.getAttribute("userId");

        // Utente deve essere autenticato
        if (userId == null) {
            throw new RuntimeException("Il sistema richiede l'autenticazione");
        }

        Utente utente = utenteRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("Utente non trovato"));

        Evento evento = eventoRepository.findById(eventId)
                .orElseThrow(() -> new RuntimeException("Evento non trovato"));

        // Controllo: non puoi cancellare la prenotazione se non sei prenotato
        boolean isPrenotato = evento.getPartecipanti().stream()
                .anyMatch(partecipante -> partecipante.getId().equals(userId));

        if (!isPrenotato) {
            throw new RuntimeException("Non sei prenotato a questo evento");
        }

        evento.rimuoviPartecipante(utente);
        eventoRepository.save(evento);

    }

    private EventoResponse toResponse(Evento evento, Long userId) {
        Utente creatore = evento.getCreatore();

        EventoResponse.CreatoreResponse creatoreResponse = new EventoResponse.CreatoreResponse(
                creatore.getId(),
                creatore.getNome(),
                creatore.getCognome());
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
                userId != null && creatore.getId().equals(userId));
    }

    public List<String> getAllDistinctTags() {
        return eventoRepository.findDistinctTags();
    }

    public List<EventoResponse> searchForEvents(HttpSession session, String search, String tag) {
        Long userId = (Long) session.getAttribute("userId");

        String searchParam = (search == null || search.trim().isEmpty()) ? null : search;
        String tagParam = ("Tutti".equalsIgnoreCase(tag) || tag == null) ? null : tag;

        // Ora passiamo solo due parametri
        List<Evento> eventi = eventoRepository.findByFiltriDinamici(searchParam, tagParam);

        boolean daSalvare = false;

        for (Evento evento : eventi) {
            if (evento.getDataOra().isBefore(java.time.LocalDateTime.now())
                    && evento.getStato().equals("aperto")) {
                evento.setStato("chiuso");
                daSalvare = true;
            }
        }

        if (daSalvare) {
            eventoRepository.saveAll(eventi);
        }

        return eventi.stream()
                // IL TOCCO DA MAESTRO: Filtriamo via dalla risposta quelli che
                // sono appena stati chiusi dal ciclo for qui sopra!
                .filter(e -> "aperto".equals(e.getStato()))
                .map(evento -> toResponse(evento, userId))
                .toList();
    }
}