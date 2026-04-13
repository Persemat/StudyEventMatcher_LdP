package com.dem.ldp.cucumber;

import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

public class PulisciDB {

    @Autowired
    private EventoRepository eventoRepository;

    @Autowired
    private UtenteRepository utenteRepository;

    @Before
    public void pulisciDatabase() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();
    }
}