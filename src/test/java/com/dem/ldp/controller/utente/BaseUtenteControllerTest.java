package com.dem.ldp.controller.utente;

import com.dem.ldp.repository.UtenteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.junit.jupiter.api.Tag;
@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseUtenteControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UtenteRepository utenteRepository;

    @BeforeEach
    void puliziaDatabaseUtente() {
        utenteRepository.deleteAll();
    }
}