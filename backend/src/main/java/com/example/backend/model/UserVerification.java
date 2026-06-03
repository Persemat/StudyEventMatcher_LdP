package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/**
 * Entity class representing the email verification status and registration
 * token for a user account.
 * <p>
 * It acts as a security store during the account registration lifecycle,
 * tracking whether the
 * email address has been successfully validated via a unique alpha-numeric or
 * numeric activation code.
 * </p>
 */
@Entity
public class UserVerification {

    @Id
    private String email;
    private String token;
    private boolean verified;

    /**
     * Default no-argument constructor required by JPA specification.
     */
    public UserVerification() {
    }

    /**
     * Constructs a new UserVerification entry initialized with the target email
     * identifier,
     * a unique security token, and its initial verification flag state.
     *
     * @param email    the unique email address acting as the primary key identifier
     * @param token    the unique verification code string sent to the user's email
     * @param verified the initial activation status flag of the user account
     */
    public UserVerification(String email, String token, boolean verified) {
        this.email = email;
        this.token = token;
        this.verified = verified;
    }

    public String getEmail() {
        return this.email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getToken() {
        return this.token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public boolean isVerified() {
        return this.verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

}