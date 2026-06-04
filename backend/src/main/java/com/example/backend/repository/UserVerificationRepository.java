package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.model.UserVerification;

/**
 * Repository interface for managing {@link UserVerification} persistence.
 * <p>
 * It provides core operations for handling email token verifications during the
 * user
 * registration and account activation lifecycle.
 */
@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {

    /**
     * Retrieves the email verification entry associated with a specific user email.
     * <p>
     * Note: In this repository, the user's email address acts as the primary key
     * identifier.
     * </p>
     *
     * @param email the email address of the user whose verification status is being
     *              requested
     * @return an {@link Optional} containing the user's verification record, or
     *         empty if none exists
     */
    Optional<UserVerification> findById(String email);

    /**
     * Locates a verification record using its unique alpha-numeric or numeric
     * registration token.
     * <p>
     * This is primarily utilized within the email confirmation step to validate the
     * user's input token.
     * </p>
     *
     * @param token the unique token string provided by the user
     * @return an {@link Optional} containing the matching verification record, or
     *         empty if the token is invalid or expired
     */
    Optional<UserVerification> findByToken(String token);
}
