package com.dem.ldp.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.dem.ldp.model.Evento;

public interface EventoRepository extends JpaRepository<Evento, Long> {
        Optional<Evento> findByCreatoreIdAndTitoloAndDataOraAndLuogo(
                        Long creatoreId,
                        String titolo,
                        LocalDateTime dataOra,
                        String luogo);

        @Query("SELECT DISTINCT t FROM Evento e JOIN e.tag t")
        List<String> findDistinctTags();

        @Query("SELECT e FROM Evento e WHERE " +
                        "e.stato = 'aperto' AND " + // <-- REGOLA FISSA: SOLO APERTI
                        "(:tag IS NULL OR :tag MEMBER OF e.tag) AND " +
                        "(:search IS NULL OR LOWER(e.titolo) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.descrizione) LIKE LOWER(CONCAT('%', :search, '%')))")
        List<Evento> findByFiltriDinamici(
                        @Param("search") String search,
                        @Param("tag") String tag);
}
