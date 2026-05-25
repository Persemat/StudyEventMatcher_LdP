package com.example.backend.unit.user;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.mockito.Mockito.mock;
import org.junit.jupiter.api.Tag;

import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserVerificationRepository; // <-- NUOVO IMPORT
import com.example.backend.service.EmailService; // <-- NUOVO IMPORT
import com.example.backend.service.UserService;

@Tag("unit")
public abstract class BaseUserTest {

    protected UserRepository userRepository;
    protected PasswordEncoder passwordEncoder;
    protected UserVerificationRepository userVerificationRepository;
    protected EmailService emailService;
    protected UserService userService;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        userVerificationRepository = mock(UserVerificationRepository.class);
        emailService = mock(EmailService.class);

        userService = new UserService(
                userRepository,
                passwordEncoder,
                userVerificationRepository,
                emailService,
                false);
    }
}