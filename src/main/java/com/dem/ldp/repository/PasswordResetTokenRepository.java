package com.dem.ldp.repository;

import com.dem.ldp.model.PasswordResetToken;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByEmailAndCodiceAndUsatoFalse(String email, String codice);

    void deleteByScadenzaBefore(LocalDateTime now);

    void deleteByEmail(String email);

    Optional<PasswordResetToken> findTopByEmailOrderByCreatoIlDesc(String email);
    
}