package com.example.backend.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

@Entity
public class UserVerification {

    @Id
    private String email;
    private String token;
    private boolean verified;

    public UserVerification() {
    }

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