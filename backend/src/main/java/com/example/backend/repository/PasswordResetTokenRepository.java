package com.example.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.PasswordResetToken;

/**
 * Repository interface for managing {@link PasswordResetToken} persistence.
 * <p>
 * It provides core operations for verification code matching, safety cooldown
 * enforcement,
 * and automated expiration-based token cleanup workflows.
 */
@Repository
public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    /**
     * Retrieves an active, unused password reset token matching a specific user
     * email and verification code.
     *
     * @param email the target user's email address
     * @param code  the 6-digit verification code provided by the client
     * @return an {@link Optional} containing the matching unused token, or empty if
     *         none exists
     */
    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    /**
     * Purges all expired password reset tokens from the database.
     * <p>
     * This method is typically invoked as a maintenance step to prevent old tokens
     * from accumulating in the system.
     *
     * @param now the current date and time boundary threshold
     */
    void deleteByExpiresAtBefore(LocalDateTime now);

    /**
     * Removes all previous password reset tokens associated with a given email
     * address.
     * <p>
     * This ensures that only the newly generated token remains valid for the
     * password reset workflow.
     *
     * @param email the user's email address whose old tokens should be purged
     */
    void deleteByEmail(String email);

    /**
     * Retrieves the single most recently generated password reset token for a given
     * email address.
     * <p>
     * Primarily utilized within the anti-spam mechanism to compute and enforce a
     * mandatory 30-second cooldown period between successive reset requests.
     *
     * @param email the target user's email address
     * @return an {@link Optional} containing the latest token entity, or empty if
     *         none has been requested
     */
    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);
}