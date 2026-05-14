package com.example.backend.unit.event;

import com.example.backend.repository.EventRepository;
import com.example.backend.repository.UserRepository;
import com.example.backend.service.EmailService;
import com.example.backend.service.EventService;

import jakarta.servlet.http.HttpSession;

import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@Tag("unit")
@ExtendWith(MockitoExtension.class)
public abstract class BaseEventTest {

    @InjectMocks
    protected EventService eventService;

    @Mock
    protected EventRepository eventRepository;

    @Mock
    protected UserRepository userRepository;

    @Mock
    protected EmailService emailService;

    @Mock
    protected HttpSession session;
}
