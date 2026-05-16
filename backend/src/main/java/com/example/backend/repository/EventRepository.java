package com.example.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.example.backend.model.Event;

public interface EventRepository extends JpaRepository<Event, Long> {

    // ================= DUPLICATE CHECK =================
    Optional<Event> findByCreatorIdAndTitleAndDateTimeAndLocation(
            Long creatorId,
            String title,
            LocalDateTime dateTime,
            String location
    );

    // ================= DISTINCT TAGS =================
    @Query("""
            SELECT DISTINCT
                CASE
                    WHEN t.microTag IS NULL OR t.microTag = '' THEN t.macroTag
                    ELSE CONCAT(t.macroTag, ' > ', t.microTag)
                END
            FROM Event e JOIN e.tags t
            """)
    List<String> findDistinctTags();

    // ================= DYNAMIC FILTER SEARCH =================
    @Query("SELECT DISTINCT e FROM Event e WHERE " +
            "(:checkTags = false OR EXISTS (SELECT t FROM Event e2 JOIN e2.tags t WHERE e2.id = e.id AND " +
            "(t.macroTag IN :tags OR " +
            "(CASE WHEN t.microTag IS NULL OR t.microTag = '' THEN t.macroTag ELSE CONCAT(t.macroTag, ' > ', t.microTag) END) IN :tags))) AND " +
            "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND " +
            "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
            "(:startDate IS NULL OR e.dateTime >= :startDate) AND " +
            "(:endDate IS NULL OR e.dateTime < :endDate)")
    List<Event> findByDynamicFilters(
            @Param("search") String search,
            @Param("checkTags") boolean checkTags,
            @Param("tags") List<String> tags,
            @Param("location") String location,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );
}
