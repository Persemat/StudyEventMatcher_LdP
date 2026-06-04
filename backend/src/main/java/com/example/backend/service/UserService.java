package com.example.backend.service;

import org.springframework.stereotype.Service;
import com.example.backend.dto.LoginRequest;
import com.example.backend.dto.UpdateEmailRequest;
import com.example.backend.dto.UpdatePasswordRequest;
import com.example.backend.dto.UpdatePersonalDataRequest;
import com.example.backend.dto.UpdatePreferencesRequest;
import com.example.backend.dto.UserRequest;
import com.example.backend.dto.UserResponse;
import com.example.backend.dto.NotificationSettingsRequest;
import com.example.backend.dto.NotificationSettingsResponse;
import com.example.backend.model.User;
import com.example.backend.repository.UserRepository;
import com.example.backend.model.UserSettings;
import com.example.backend.repository.UserSettingsRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.List;
import java.util.ArrayList;
import org.springframework.beans.factory.annotation.Value;
import com.example.backend.model.UserVerification;
import com.example.backend.repository.UserVerificationRepository;
import jakarta.transaction.Transactional;

/**
 * Service responsible for user management.
 *
 * It handles:
 * - user registration and email token verification workflows
 * - user authentication (login mechanisms)
 * - retrieval and modification of user profile data, credentials, and tags
 * - retrieval and management of personalized notification communication
 * settings
 */
@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final UserVerificationRepository verificationRepository;
    private final EmailService emailService;
    private final UserSettingsRepository settingsRepository;

    private boolean requireEmailVerification;

    /**
     * Constructs the UserService.
     *
     * @param userRepository           repository used to access user data
     * @param passwordEncoder          encoder used to securely hash passwords
     * @param verificationRepository   repository used to access email verification
     *                                 data
     * @param emailService             service responsible for sending application
     *                                 emails
     * @param requireEmailVerification feature flag specifying if activation tokens
     *                                 are mandatory
     * @param settingsRepository       repository used to manage user notification
     *                                 preferences
     */
    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            UserVerificationRepository verificationRepository,
            EmailService emailService,
            @Value("${app.security.require-email-verification:false}") boolean requireEmailVerification,
            UserSettingsRepository settingsRepository) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.verificationRepository = verificationRepository;
        this.emailService = emailService;
        this.requireEmailVerification = requireEmailVerification;
        this.settingsRepository = settingsRepository;
    }

    /**
     * Registers a new user in the system.
     * <p>
     * Validates input fields and uniqueness requirements, securely hashes the
     * password,
     * and automatically initializes a default notification preference profile for
     * the user.
     * If required by system configuration, it also generates and dispatches an
     * email verification token.
     *
     * @param request contains user registration data
     * @return the created user as a response DTO
     * @throws IllegalArgumentException if any validation rule fails or the email is
     *                                  already taken
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

        validatePassword(password);

        if (favouriteCity == null || favouriteCity.isBlank()) {
            throw new IllegalArgumentException("Bisogna indicare una città preferita");
        }

        if (macroTags == null || macroTags.isEmpty()) {
            throw new IllegalArgumentException("Almeno una macro area di interesse deve essere indicata");
        }

        if (macroTags.size() > 3) {
            throw new IllegalArgumentException("Si possono indicare al massimo 3 aree di interesse generale ");
        }

        if (microTags != null && microTags.size() > 5) {
            throw new IllegalArgumentException("Si possono indicare al massimo 5 argomenti di interesse specifici ");
        }

        User user = new User();
        user.setName(name);
        user.setLastname(lastName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFavouriteCity(favouriteCity);
        user.setMacroTags(macroTags);
        user.setMicroTags(microTags != null ? microTags : new ArrayList<>());

        User savedUser = userRepository.save(user);

        UserSettings notificationSettings = new UserSettings(savedUser.getEmail(), true, true, false);
        settingsRepository.save(notificationSettings);

        if (requireEmailVerification) {

            String token = String.valueOf((int) (Math.random() * 900000) + 100000);

            UserVerification verification = new UserVerification(savedUser.getEmail(), token, false);
            verificationRepository.save(verification);

            emailService.sendRegistrationVerificationEmail(savedUser.getEmail(), token);
        }

        return toResponse(savedUser);
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

        verificationRepository.findById(email).ifPresent(verification -> {
            if (!verification.isVerified()) {
                throw new IllegalStateException("Devi confermare la tua email prima di poter accedere.");
            }
        });

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
     * Updates the personal details (name and lastname) of the authenticated user.
     *
     * @param userId  the ID of the current user extracted from the session
     * @param request the request DTO containing the updated name and lastname
     * @return the updated user mapped to a {@link UserResponse} DTO
     * @throws IllegalStateException    if the session userId is null
     * @throws IllegalArgumentException if required fields are missing or blank, or
     *                                  the user does not exist
     */
    public UserResponse updatePersonalData(Long userId, UpdatePersonalDataRequest request) {
        User user = getUserFromSession(userId);

        String name = request.name();
        String lastname = request.lastname();

        if (name == null || name.isBlank() || lastname == null || lastname.isBlank()) {
            throw new IllegalArgumentException("Campi obbligatori mancanti");
        }

        user.setName(name.trim());
        user.setLastname(lastname.trim());

        return toResponse(userRepository.save(user));
    }

    /**
     * Updates the user's location preferences and tag interests.
     * <p>
     * Validates that general macro tags do not exceed 3 and specific micro tags
     * do not exceed 5.
     *
     * @param userId  the ID of the current user extracted from the session
     * @param request the request DTO containing the updated favorite city and
     *                interest tags
     * @return the updated user mapped to a {@link UserResponse} DTO
     * @throws IllegalStateException    if the session userId is null
     * @throws IllegalArgumentException if inputs violate tag size constraints,
     *                                  city/macro tags are missing,
     *                                  or the user is not found
     */
    public UserResponse updatePreferences(Long userId, UpdatePreferencesRequest request) {
        User user = getUserFromSession(userId);

        String favouriteCity = request.favouriteCity();
        List<String> macroTags = request.macroTags();
        List<String> microTags = request.microTags();

        if (favouriteCity == null || favouriteCity.isBlank()) {
            throw new IllegalArgumentException("Bisogna indicare una città preferita");
        }

        if (macroTags == null || macroTags.isEmpty()) {
            throw new IllegalArgumentException("Almeno una macro area di interesse deve essere indicata");
        }

        if (macroTags.size() > 3) {
            throw new IllegalArgumentException("Si possono indicare al massimo 3 aree di interesse generale ");
        }

        if (microTags != null && microTags.size() > 5) {
            throw new IllegalArgumentException("Si possono indicare al massimo 5 argomenti di interesse specifici ");
        }

        user.setFavouriteCity(request.favouriteCity());

        user.getMacroTags().clear();
        user.getMacroTags().addAll(request.macroTags());

        user.getMicroTags().clear();
        if (request.microTags() != null) {
            user.getMicroTags().addAll(request.microTags());
        }

        return toResponse(userRepository.save(user));
    }

    /**
     * Updates the user's registered email address after validating its format and
     * ensuring uniqueness.
     *
     * @param userId  the ID of the current user extracted from the session
     * @param request the request DTO containing the new email address
     * @return the updated user mapped to a {@link UserResponse} DTO
     * @throws IllegalStateException    if the session userId is null
     * @throws IllegalArgumentException if the email format is invalid, already
     *                                  taken by another user,
     *                                  or the user does not exist
     */
    public UserResponse updateEmail(Long userId, UpdateEmailRequest request) {
        User user = getUserFromSession(userId);
        String email = request.email();

        if (email == null || !email.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            throw new IllegalArgumentException("Email non valida");
        }

        String normalizedEmail = email.trim();

        userRepository.findByEmail(normalizedEmail)
                .filter(existingUser -> !user.getId().equals(existingUser.getId()))
                .ifPresent(existingUser -> {
                    throw new IllegalArgumentException("Email già registrata");
                });

        user.setEmail(normalizedEmail);

        return toResponse(userRepository.save(user));
    }

    /**
     * Updates the user's password securely after verifying the current password and
     * complexity rules.
     *
     * @param userId  the ID of the current user extracted from the session
     * @param request the request DTO containing current password, new password, and
     *                confirmation
     * @return the updated user mapped to a {@link UserResponse} DTO
     * @throws IllegalStateException    if the session userId is null
     * @throws IllegalArgumentException if input fields are blank, confirmation
     *                                  mismatches,
     *                                  complexity rules fail, or the current
     *                                  password is wrong
     */
    public UserResponse updatePassword(Long userId, UpdatePasswordRequest request) {
        User user = getUserFromSession(userId);

        String currentPassword = request.currentPassword();
        String newPassword = request.newPassword();
        String confirmPassword = request.confirmPassword();

        if (currentPassword == null || currentPassword.isBlank()) {
            throw new IllegalArgumentException("Password attuale obbligatoria");
        }

        if (newPassword == null || newPassword.isBlank()) {
            throw new IllegalArgumentException("Nuova password obbligatoria");
        }

        if (confirmPassword == null || confirmPassword.isBlank()) {
            throw new IllegalArgumentException("Conferma password obbligatoria");
        }

        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("Le password non corrispondono");
        }

        validatePassword(newPassword);

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new IllegalArgumentException("Password attuale non corretta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        return toResponse(userRepository.save(user));
    }

    /**
     * Retrieves the email notification settings for the authenticated user.
     * <p>
     * If no customized settings are found, it falls back to a default
     * configuration.
     *
     * @param userId the ID of the current user extracted from the session
     * @return the notification settings mapped to a
     *         {@link NotificationSettingsResponse} DTO
     * @throws IllegalStateException    if the session userId is null
     * @throws IllegalArgumentException if the user does not exist
     */
    public NotificationSettingsResponse showNotificationSettings(Long userId) {
        User loggedUser = getUserFromSession(userId);
        String userEmail = loggedUser.getEmail();

        UserSettings notificationSettings = settingsRepository.findById(userEmail)
                .orElseGet(() -> new UserSettings(userEmail, true, true, false));

        return (new NotificationSettingsResponse(notificationSettings.isEditEventEmailActivated(),
                notificationSettings.isDeleteEventEmailActivated(),
                notificationSettings.isRecomendedEventEmailActivated()));

    }

    /**
     * Updates the user's notification settings flags.
     *
     * @param request the request DTO containing the updated notification flags
     * @param userId  the ID of the current user extracted from the session
     * @return the saved notification settings mapped to a
     *         {@link NotificationSettingsResponse} DTO
     * @throws IllegalStateException    if the session userId is null
     * @throws IllegalArgumentException if the user does not exist
     */
    public NotificationSettingsResponse modifySettings(NotificationSettingsRequest request, Long userId) {
        User loggedUser = getUserFromSession(userId);
        String userEmail = loggedUser.getEmail();

        UserSettings notificationSettings = settingsRepository.findById(userEmail)
                .orElseGet(() -> new UserSettings(userEmail, true, true, false));

        notificationSettings.setDeleteEventEmailActivated(request.deleteEventEmailActivated());
        notificationSettings.setEditEventEmailActivated(request.editEventEmailActivated());
        notificationSettings.setRecomendedEventEmailActivated(request.recomendedEventEmailActivated());

        UserSettings savedNotificationSettings = settingsRepository.save(notificationSettings);

        return (new NotificationSettingsResponse(savedNotificationSettings.isEditEventEmailActivated(),
                savedNotificationSettings.isDeleteEventEmailActivated(),
                savedNotificationSettings.isRecomendedEventEmailActivated()));
    }

    /**
     * Verifies a user's email using the provided token.
     *
     * @param token the unique verification token
     * @throws IllegalArgumentException if the token is invalid
     */
    public void verifyUserEmail(String token) {
        UserVerification verification = verificationRepository.findByToken(token)
                .orElseThrow(() -> new IllegalArgumentException("Token di verifica non valido o scaduto."));

        verification.setVerified(true);
        verification.setToken(null);

        verificationRepository.save(verification);
    }

    @Transactional
    public void resendVerificationEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("Utente non trovato nel sistema."));

        UserVerification verification = verificationRepository.findById(email)
                .orElseThrow(() -> new RuntimeException("Nessuna verifica in corso per questo utente."));

        if (verification.isVerified()) {
            throw new RuntimeException("Questo account è già stato confermato ed è attivo.");
        }

        String newCode = String.valueOf((int) (Math.random() * 900000) + 100000);

        verification.setToken(newCode);
        verificationRepository.save(verification);

        emailService.sendRegistrationVerificationEmail(user.getEmail(), newCode);
    }

    /**
     * Helper method to retrieve a user entity from the database using a session
     * identifier.
     *
     * @param userId the user ID to check and search
     * @return the retrieved {@link User} entity
     * @throws IllegalStateException    if the provided userId is null
     * @throws IllegalArgumentException if no user is found with the given ID
     */
    private User getUserFromSession(Long userId) {
        if (userId == null) {
            throw new IllegalStateException("Sessione invalida o scaduta");
        }

        return userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));
    }

    /**
     * Validates that a proposed password satisfies minimum complexity requirements.
     * <p>
     * The password must be at least 8 characters long, contain at least one
     * uppercase letter,
     * and contain at least one digit.
     *
     * @param password the raw password string to validate
     * @throws IllegalArgumentException if the password fails any of the complexity
     *                                  constraints
     */
    private void validatePassword(String password) {
        if (password == null || password.length() < 8) {
            throw new IllegalArgumentException("Password troppo corta");
        }

        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("La password deve contenere almeno una maiuscola");
        }

        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("La password deve contenere almeno un numero");
        }
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
