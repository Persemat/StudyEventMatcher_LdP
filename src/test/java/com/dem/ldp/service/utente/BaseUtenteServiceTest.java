package com.dem.ldp.service.utente;

import com.dem.ldp.repository.UtenteRepository;
import com.dem.ldp.service.UtenteService;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

public abstract class BaseUtenteServiceTest {

    protected UtenteRepository utenteRepository;
    protected PasswordEncoder passwordEncoder;
    protected UtenteService utenteService;

    @BeforeEach
    void baseSetUpUtente() {
        utenteRepository = mock(UtenteRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        utenteService = new UtenteService(utenteRepository, passwordEncoder);
    }
}