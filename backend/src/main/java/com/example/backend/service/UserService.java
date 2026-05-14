package com.example.backend.service;

import org.springframework.stereotype.Service;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.ArrayList;

/**
 * Service responsible for user management.
 *
 * It handles:
 * - user registration
 * - authentication (login)
 * - retrieval of the currently logged user
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Constructs the UserService.
     *
     * @param userRepository  repository used to access user data
     * @param passwordEncoder encoder used to securely hash passwords
     */
    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Registers a new user.
     *
     * Validates input data, ensures email uniqueness, and stores the password
     * securely.
     *
     * @param request contains user registration data
     * @return the created user as a response DTO
     * @throws IllegalArgumentException if any field is invalid or the email is
     *                                  already registered
     */
    public UserResponse register(UserRequest request) {

        String name = request.name();
        String lastName = request.lastname();
        String email = request.email();
        String password = request.password();
        String favouriteCity = request.favouriteCity();
        List<String> macroTags = request.macroTags();
        List<String> microTags = request.microTags();

        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Nome obbligatorio");
        }

        if (lastName == null || lastName.isBlank()) {
            throw new IllegalArgumentException("Cognome obbligatorio");
        }

        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email non valida");
        }

        if (userRepository.findByEmail(email).isPresent()) {
            throw new IllegalArgumentException("Email già registrata");
        }

        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password troppo corta");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno una maiuscola");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La password deve contenere almeno un numero");
        }

        if (favouriteCity == null || favouriteCity.isBlank()) {
            throw new IllegalArgumentException("Bisogna indicare una città preferita");
        }

        if (macroTags == null || macroTags.isEmpty()) {
            throw new IllegalArgumentException("Almeno una macro area di interesse deve essere indicata");
        }

        if (macroTags.size() > 3) {
            throw new IllegalArgumentException("Si possono indicare al massimo 3 macro tag ");
        }

        if (microTags.size() > 5) {
            throw new IllegalArgumentException("Si possono indicare al massimo 5 micro tag ");
        }

        User user = new User();
        user.setName(name);
        user.setLastname(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFavouriteCity(favouriteCity);
        user.setMacroTags(macroTags);
        user.setMicroTags(microTags != null ? microTags : new ArrayList<>());

        return toResponse(userRepository.save(user));
    }

    /**
     * Authenticates a user using email and password.
     *
     * @param request contains login credentials
     * @return the authenticated user as a response DTO
     * @throws IllegalArgumentException if credentials are invalid
     */
    public UserResponse login(LoginRequest request) {
        String email = request.email();
        String password = request.password();

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException("Email obbligatoria");
        }

        if (password == null || password.isBlank()) {
            throw new IllegalArgumentException("Password Obbligatoria");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Credenziali invalide"));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new IllegalArgumentException("Credenziali invalide");
        }

        return toResponse(user);
    }

    /**
     * Retrieves the currently logged user based on session ID.
     *
     * @param userId ID of the logged user
     * @return the user as a response DTO
     * @throws IllegalStateException    if the session is invalid
     * @throws IllegalArgumentException if the user does not exist
     */
    public UserResponse getLoggedUser(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("Sessione invalida o scaduta");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        return toResponse(user);
    }

    /**
     * Maps a User entity to a UserResponse DTO.
     *
     * @param user the user entity
     * @return the corresponding response DTO
     */
    private UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getLastname(),
                user.getEmail(),
                user.getFavouriteCity(),
                user.getMacroTags(),
                user.getMicroTags());
    }
}