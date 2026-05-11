package com.example.backend.cucumber;


import io.cucumber.java.Before;
import org.springframework.beans.factory.annotation.Autowired;

import com.example.backend.repository.EventRepository;
import com.example.backend.repository.PasswordResetTokenRepository;
import com.example.backend.repository.UserRepository;

public class ClearDB {

    @Autowired
    private EventRepository eventRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordResetTokenRepository passwordResetTokenRepository;

    @Before
    public void pulisciDatabase() {
        eventRepository.deleteAll();
        userRepository.deleteAll();
        passwordResetTokenRepository.deleteAll();
    }
}