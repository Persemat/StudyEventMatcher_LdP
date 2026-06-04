package com.example.backend.unit.user;

import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Optional;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.example.backend.model.User;
import com.example.backend.model.UserVerification;
import com.example.backend.repository.UserRepository;
import com.example.backend.repository.UserVerificationRepository;
import com.example.backend.repository.UserSettingsRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.UserService;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.LoginRequest;

@Tag("unit")
public class UserEmailVerificationUnitTest {

    private UserRepository userRepository;
    private PasswordEncoder passwordEncoder;
    private UserVerificationRepository verificationRepository;
    private EmailService emailService;
    private UserService userServiceWithMailActive;
    private UserSettingsRepository settingsRepository;

    @BeforeEach
    void setUp() {
        userRepository = mock(UserRepository.class);
        passwordEncoder = mock(PasswordEncoder.class);
        verificationRepository = mock(UserVerificationRepository.class);
        emailService = mock(EmailService.class);
        settingsRepository = mock(UserSettingsRepository.class);

        userServiceWithMailActive = new UserService(
                userRepository,
                passwordEncoder,
                verificationRepository,
                emailService,
                true,
                settingsRepository);
    }

    @Test
    void register_ShouldSaveVerificationTokenAndSendEmail_WhenVerificationIsActive() {

        UserRequest request = new UserRequest(
                "Paolo", "Rossi", "paolo@test.com", "Password123!",
                "Milano", java.util.List.of("Tech"), java.util.List.of("Java"));

        User mockSavedUser = new User();
        mockSavedUser.setEmail("paolo@test.com");

        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashed_pwd");
        when(userRepository.save(any(User.class))).thenReturn(mockSavedUser);

        userServiceWithMailActive.register(request);

        ArgumentCaptor<UserVerification> verificationCaptor = ArgumentCaptor.forClass(UserVerification.class);
        verify(verificationRepository, times(1)).save(verificationCaptor.capture());

        UserVerification savedVerification = verificationCaptor.getValue();
        assertEquals("paolo@test.com", savedVerification.getEmail());
        assertNotNull(savedVerification.getToken());
        assertFalse(savedVerification.isVerified());

        verify(emailService, times(1)).sendRegistrationVerificationEmail(eq("paolo@test.com"),
                eq(savedVerification.getToken()));
    }

    @Test
    void login_ShouldThrowException_WhenEmailIsNotVerified() {

        String email = "paolo@test.com";
        String password = "hashed_pwd";
        User mockUser = new User();
        mockUser.setEmail(email);
        mockUser.setPassword(password);

        LoginRequest request = new LoginRequest(email, password);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(true);

        UserVerification mockVerification = new UserVerification(email, "some-token", false);
        when(verificationRepository.findById(email)).thenReturn(Optional.of(mockVerification));

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> {
            userServiceWithMailActive.login(request);
        });

        assertEquals("Devi confermare la tua email prima di poter accedere.", exception.getMessage());
    }
}
