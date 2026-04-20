package com.example.backend.integration.user;

import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
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
public abstract class BaseUserIT{

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected UserRepository userRepository;
    
    @Autowired
    protected EventRepository eventRepository;

    @BeforeEach
    void cleanUserDatabase() {
        eventRepository.deleteAllInBatch();
        userRepository.deleteAll();
    }
}