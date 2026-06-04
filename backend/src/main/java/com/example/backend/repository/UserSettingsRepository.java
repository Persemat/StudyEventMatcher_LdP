package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.model.UserSettings;

/**
 * Repository interface for managing {@link UserSettings} persistence.
 * <p>
 * It serves as the data access layer for retrieving, modifying, and storing
 * personalized user communication and email notification preferences.
 */
@Repository
public interface UserSettingsRepository extends JpaRepository<UserSettings, String> {

    /**
     * Retrieves the notification settings profile associated with a specific user
     * email.
     * <p>
     * Note: In this repository, the email address acts as the primary key
     * identifier.
     * </p>
     *
     * @param email the email address of the user whose settings are being requested
     * @return an {@link Optional} containing the matching user notification
     *         settings, or empty if none exist
     */
    Optional<UserSettings> findById(String email);

}