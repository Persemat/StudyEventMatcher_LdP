package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.backend.model.User;

/**
 * Repository interface for managing {@link User} persistence.
 * <p>
 * It serves as the primary data access layer for retrieving user accounts,
 * enforcing email-based identity constraints, and managing user profiles.
 */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Retrieves a user account associated with a specific unique email address.
     * <p>
     * This method is central to the application's authentication, registration
     * uniqueness checks,
     * and security management workflows.
     *
     * @param email the unique email address used to locate the user profile
     * @return an {@link Optional} containing the matching user profile, or empty if
     *         no account exists
     */
    Optional<User> findByEmail(String email);
}
