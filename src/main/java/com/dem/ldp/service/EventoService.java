package com.dem.ldp.service;

import com.dem.ldp.dto.EventoRequest;
import com.dem.ldp.model.Evento;
import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
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

    public Evento creaNuovoEvento(EventoRequest request) {
        
        //Recupero dell'utente creatore
        Optional<Utente> creatoreOpt = utenteRepository.findById(request.creatoreId());
        if (creatoreOpt.isEmpty()) {
            throw new RuntimeException("Utente creatore non trovato con ID: " + request.creatoreId());
        }
        Utente creatore = creatoreOpt.get();

        // 2. Creazione e mappatura dell'Entità
        Evento nuovoEvento = new Evento();
        nuovoEvento.setTitolo(request.titolo());
        nuovoEvento.setDataOra(request.dataOra());
        nuovoEvento.setLuogo(request.luogo());
        nuovoEvento.setTag(request.tag());
        nuovoEvento.setDescrizione(request.descrizione());
        nuovoEvento.setMaxPartecipanti(request.maxPartecipanti());
        nuovoEvento.setPartecipazioneCreatoreNecessaria(request.partecipazioneCreatoreNecessaria());
        
        nuovoEvento.setStato("aperto");
        nuovoEvento.setCreatore(creatore);

        // 3. Salvataggio tramite Repository
        return eventoRepository.save(nuovoEvento);
    }
}
