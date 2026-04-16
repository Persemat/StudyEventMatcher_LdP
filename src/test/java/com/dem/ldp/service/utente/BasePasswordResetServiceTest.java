package com.dem.ldp.service.utente;

import com.dem.ldp.repository.PasswordResetTokenRepository;
import com.dem.ldp.repository.UtenteRepository;
import com.dem.ldp.service.EmailService;
import com.dem.ldp.service.PasswordResetService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

@Tag("unit")
public abstract class BasePasswordResetServiceTest {

    protected UtenteRepository utenteRepository;
    protected PasswordResetTokenRepository passwordResetTokenRepository;
    protected PasswordEncoder passwordEncoder;
    protected EmailService emailService;
    protected PasswordResetService passwordResetService;

    @BeforeEach
    void baseSetUpPasswordReset() {
        utenteRepository = mock(UtenteRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);

        passwordResetService = new PasswordResetService(
                utenteRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                emailService
        );
    }
}