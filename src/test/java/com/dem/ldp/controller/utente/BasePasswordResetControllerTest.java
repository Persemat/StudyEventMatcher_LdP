package com.dem.ldp.controller.utente;

import com.dem.ldp.repository.PasswordResetTokenRepository;
import com.dem.ldp.repository.UtenteRepository;
import com.dem.ldp.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BasePasswordResetControllerTest {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UtenteRepository utenteRepository;

    @Autowired
    protected PasswordResetTokenRepository passwordResetTokenRepository;

    @MockitoBean
    protected EmailService emailService;

    @BeforeEach
    void puliziaDatabasePasswordReset() {
        passwordResetTokenRepository.deleteAll();
        utenteRepository.deleteAll();
    }
}