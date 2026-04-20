package com.example.backend.integration.event;

import com.example.backend.model.User;
import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

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

        User user = new User();
        user.setName("Luca");
        user.setLastname("Rossi");
        user.setEmail("luca@example.com");
        user.setPassword("Pass123");

        user = userRepository.saveAndFlush(user);
        this.userId = user.getId();
    }
}