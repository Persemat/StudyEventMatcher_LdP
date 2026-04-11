package com.dem.ldp.controller.evento;

import com.dem.ldp.model.Utente;
import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseEventoControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EventoRepository eventoRepository;

    @Autowired
    protected UtenteRepository utenteRepository;

    protected Long utenteId;

    @BeforeEach
    void preparaDatabaseBase() {
        eventoRepository.deleteAll();
        utenteRepository.deleteAll();

        Utente utenteTest = new Utente();
        utenteTest.setNome("Luca");
        utenteTest.setCognome("Rossi");
        utenteTest.setEmail("luca@example.com");
        utenteTest.setPassword("Pass123");

        utenteTest = utenteRepository.save(utenteTest);
        this.utenteId = utenteTest.getId();
    }
}