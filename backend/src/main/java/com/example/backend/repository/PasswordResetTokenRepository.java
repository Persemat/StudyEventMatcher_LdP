package com.example.backend.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.example.backend.model.PasswordResetToken;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByEmailAndCodeAndUsedFalse(String email, String code);

    void deleteByExpiresAtBefore(LocalDateTime now);

    void deleteByEmail(String email);

    Optional<PasswordResetToken> findTopByEmailOrderByCreatedAtDesc(String email);
}