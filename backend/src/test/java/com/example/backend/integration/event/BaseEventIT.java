package com.example.backend.integration.event;

import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.ArrayList;

//@Tag("integration")
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public abstract class BaseEventIT {

    @Autowired
    protected MockMvc mockMvc;

    @Autowired
    protected EventRepository eventRepository;

    @Autowired
    protected UserRepository userRepository;

    protected Long userId;

    @BeforeEach
    void prepareBaseDatabase() {
        eventRepository.deleteAll();
        eventRepository.flush();
        userRepository.deleteAll();
        userRepository.flush();

        User user = new User(
                "Luca",
                "Rossi",
                "luca@example.com",
                "Pass123",
                "Milano",
                List.of("Tech"),
                new ArrayList<>());

        user = userRepository.saveAndFlush(user);
        this.userId = user.getId();
    }
}