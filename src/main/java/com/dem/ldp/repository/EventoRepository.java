package com.dem.ldp.repository;
import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import com.dem.ldp.model.Evento;
public interface EventoRepository extends JpaRepository<Evento, Long> {
    Optional<Evento> findByCreatoreIdAndTitoloAndDataOraAndLuogo(
                Long creatoreId,
                String titolo,
                LocalDateTime dataOra,
                String luogo
        );
}
