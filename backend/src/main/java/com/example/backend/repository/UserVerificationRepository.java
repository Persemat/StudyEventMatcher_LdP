package com.example.backend.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import com.example.backend.model.UserVerification;
import com.example.backend.model.User;

@Repository
public interface UserVerificationRepository extends JpaRepository<UserVerification, String> {
    Optional<UserVerification> findById(String email);

    Optional<UserVerification> findByToken(String token);
}
