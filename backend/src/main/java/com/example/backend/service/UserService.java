package com.example.backend.service;

import org.springframework.stereotype.Service;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;


@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;


    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public UserResponse register(UserRequest request) {

        String name = request.name();
        String lastName = request.lastname();
        String email = request.email();
        String password = request.password();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name is required");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Last name is required");
        }

        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Invalid email");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email already registered");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password too short");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }


        User user = new User();
        user.setName(name);
        user.setLastname(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));

        return toResponse(userRepository.save(user));
    }



    public UserResponse login(LoginRequest request) {
        String email = request.email();
        String password = request.password();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Invalid credentials");
        }

        return toResponse(user);
    }


    public UserResponse getLoggedUser(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("Invalid or expired session");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        return toResponse(user);
    }



    private UserResponse toResponse(User user) {
        return new UserResponse(
            user.getId(),
            user.getName(),
            user.getLastname(),
            user.getEmail()
        );
    }
}
