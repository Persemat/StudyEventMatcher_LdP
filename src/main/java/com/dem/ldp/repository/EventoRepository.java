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
                        "e.stato = 'aperto' AND " +
                        // LA MAGIA DELL'AND: Conta i tag corrispondenti e verifica che siano pari al
                        // numero di tag cercati
                        "(:checkTags = false OR :tagCount = (SELECT COUNT(t) FROM Evento e2 JOIN e2.tag t WHERE e2.id = e.id AND t IN :tags)) AND "
                        +
                        "(:search IS NULL OR LOWER(e.titolo) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.descrizione) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
                        +
                        "(:luogo IS NULL OR LOWER(e.luogo) LIKE LOWER(CONCAT('%', :luogo, '%'))) AND " +
                        "(:dataStart IS NULL OR e.dataOra >= :dataStart) AND " +
                        "(:dataEnd IS NULL OR e.dataOra < :dataEnd)")
        List<Evento> findByFiltriDinamici(
                        @Param("search") String search,
                        @Param("checkTags") boolean checkTags,
                        @Param("tags") List<String> tags,
                        @Param("tagCount") long tagCount, // <-- NUOVO PARAMETRO
                        @Param("luogo") String luogo,
                        @Param("dataStart") java.time.LocalDateTime dataStart,
                        @Param("dataEnd") java.time.LocalDateTime dataEnd);
}
