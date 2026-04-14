package com.dem.ldp.service.evento;

import com.dem.ldp.repository.EventoRepository;
import com.dem.ldp.repository.UtenteRepository;
import com.dem.ldp.service.EventoService;

import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.Tag;
@Tag("unit")
@ExtendWith(MockitoExtension.class)
public abstract class BaseEventoServiceTest {

    @InjectMocks
    protected EventoService eventoService;

    @Mock
    protected EventoRepository eventoRepository;

    @Mock
    protected UtenteRepository utenteRepository;

    @Mock
    protected HttpSession session;
}