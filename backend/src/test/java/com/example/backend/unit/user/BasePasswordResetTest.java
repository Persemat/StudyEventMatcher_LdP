package com.example.backend.unit.user;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;

import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.PasswordResetService;

@Tag("unit")
public abstract class BasePasswordResetTest {

    protected UserRepository userRepository;
    protected PasswordResetTokenRepository passwordResetTokenRepository;
    protected PasswordEncoder passwordEncoder;
    protected EmailService emailService;
    protected PasswordResetService passwordResetService;

    @BeforeEach
    void setupPasswordResetUnit() {
        userRepository = mock(UserRepository.class);
        passwordResetTokenRepository = mock(PasswordResetTokenRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        emailService = mock(EmailService.class);

        passwordResetService = new PasswordResetService(
                userRepository,
                passwordResetTokenRepository,
                passwordEncoder,
                emailService
        );
    }
}
