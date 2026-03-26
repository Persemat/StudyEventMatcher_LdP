package com.dem.ldp.repository;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dem.ldp.model.Utente;

public interface UtenteRepository extends JpaRepository<Utente, Long>{
    Optional<Utente> findByEmail(String email);
}
