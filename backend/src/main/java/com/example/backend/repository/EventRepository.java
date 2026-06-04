package com.example.backend.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.example.backend.model.Event;

/**
 * Repository interface for managing {@link Event} persistence and complex
 * relational queries.
 * <p>
 * It provides specialized capabilities for dynamic filtering, tag aggregation,
 * duplication checks, and user-event participation queries.
 */
@Repository
public interface EventRepository extends JpaRepository<Event, Long> {

        /**
         * Checks for the existence of an identical event created by the same user
         * at the same time and location.
         *
         * @param creatorId the ID of the event organizer
         * @param title     the name of the event
         * @param dateTime  the scheduled date and time of the event
         * @param location  the venue or physical space where the event takes place
         * @return an {@link Optional} containing the duplicate event if found, or empty
         *         otherwise
         */
        Optional<Event> findByCreatorIdAndTitleAndDateTimeAndLocation(
                        Long creatorId,
                        String title,
                        LocalDateTime dateTime,
                        String location);

        /**
         * Aggregates and formats all unique tags used across all events in the system.
         * <p>
         * If a tag has a sub-category (micro-tag), it formats the string as "macroTag
         * &gt; microTag".
         * If the sub-category is empty or absent, it simply returns the "macroTag".
         *
         * @return a list of distinct, formatted tag strings suitable for UI dropdown
         *         filters
         */
        @Query("""
                        SELECT DISTINCT
                            CASE
                                WHEN t.microTag IS NULL OR t.microTag = '' THEN t.macroTag
                                ELSE CONCAT(t.macroTag, ' > ', t.microTag)
                            END
                        FROM Event e JOIN e.tags t
                        """)
        List<String> findDistinctTags();

        /**
         * Executes an advanced, multi-parameter dynamic search over upcoming events.
         * <p>
         * Supports matching by free-text search terms (title or description), location
         * naming,
         * explicit date ranges, and intersection against a specific list of formatted
         * tag pairs.
         * </p>
         *
         * @param search    optional free-text keyword to look up inside titles or
         *                  descriptions
         * @param checkTags boolean flag specifying whether tag filtering should be
         *                  enforced
         * @param tags      a list of target formatted tag strings to match against
         *                  event profiles
         * @param location  optional textual filter to isolate matching venue names
         * @param startDate optional lower boundary for the event schedule timeline
         * @param endDate   optional upper boundary for the event schedule timeline
         * @return a filtered list of distinct events matching all active criteria
         */
        @Query("SELECT DISTINCT e FROM Event e WHERE " +
                        "(:checkTags = false OR EXISTS (SELECT t FROM Event e2 JOIN e2.tags t WHERE e2.id = e.id AND " +
                        "(t.macroTag IN :tags OR " +
                        "(CASE WHEN t.microTag IS NULL OR t.microTag = '' THEN t.macroTag ELSE CONCAT(t.macroTag, ' > ', t.microTag) END) IN :tags))) AND "
                        +
                        "(:search IS NULL OR LOWER(e.title) LIKE LOWER(CONCAT('%', :search, '%')) OR LOWER(e.description) LIKE LOWER(CONCAT('%', :search, '%'))) AND "
                        +
                        "(:location IS NULL OR LOWER(e.location) LIKE LOWER(CONCAT('%', :location, '%'))) AND " +
                        "(:startDate IS NULL OR e.dateTime >= :startDate) AND " +
                        "(:endDate IS NULL OR e.dateTime < :endDate)")
        List<Event> findByDynamicFilters(
                        @Param("search") String search,
                        @Param("checkTags") boolean checkTags,
                        @Param("tags") List<String> tags,
                        @Param("location") String location,
                        @Param("startDate") LocalDateTime startDate,
                        @Param("endDate") LocalDateTime endDate);

        /**
         * Retrieves all events that a specific user has joined as a participant.
         *
         * @param userId the unique identifier of the target user
         * @return a list of distinct events the specified user is booked into
         */
        @Query("SELECT DISTINCT e FROM Event e JOIN e.participants p WHERE p.id = :userId")
        List<Event> findBookedByParticipantId(@Param("userId") Long userId);
}
