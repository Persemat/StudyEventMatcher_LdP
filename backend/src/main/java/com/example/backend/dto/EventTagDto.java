package com.example.backend.dto;

import com.example.backend.model.EventTag;

/**
 * Data Transfer Object representing a decoupled, lightweight view of an event's
 * tag.
 * <p>
 * This record is used to serialize and transmit tag information to the client
 * without exposing the underlying persistent database structures directly.
 * </p>
 */
public record EventTagDto(
        String macroTag,
        String microTag) {
    /**
     * Factory method that maps a persistent {@link EventTag} domain value object
     * into a detached {@link EventTagDto} representation.
     *
     * @param tag the database domain tag entity to copy properties from
     * @return a new, immutable instance of {@link EventTagDto} populated with the
     *         tag values
     */
    public static EventTagDto from(EventTag tag) {
        return new EventTagDto(tag.getMacroTag(), tag.getMicroTag());
    }
}
