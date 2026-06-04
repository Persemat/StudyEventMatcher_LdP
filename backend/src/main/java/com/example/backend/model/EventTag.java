package com.example.backend.model;

import java.util.Objects;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;

/**
 * Value object class representing a categorized topic tag for an event.
 * <p>
 * This class is annotated as {@link Embeddable}, meaning its fields are stored
 * directly
 * within the parent entity's collection table mappings rather than existing as
 * a separate
 * standalone entity.
 */
@Embeddable
public class EventTag {

    @Column(name = "macro_tag", nullable = false)
    private String macroTag;

    @Column(name = "micro_tag")
    private String microTag;

    /**
     * Default no-argument constructor required by JPA specification.
     */
    public EventTag() {
    }

    /**
     * Constructs an EventTag pairing a general category with a specific sub-topic.
     *
     * @param macroTag the primary interest area or category label
     * @param microTag the optional targeted sub-topic label (can be null)
     */
    public EventTag(String macroTag, String microTag) {
        this.macroTag = macroTag;
        this.microTag = microTag;
    }

    public String getMacroTag() {
        return macroTag;
    }

    public void setMacroTag(String macroTag) {
        this.macroTag = macroTag;
    }

    public String getMicroTag() {
        return microTag;
    }

    public void setMicroTag(String microTag) {
        this.microTag = microTag;
    }

    /**
     * Formats the tag properties into a human-readable display label suitable for
     * the user interface.
     * <p>
     * If the nested micro-tag is absent, it returns only the macro category.
     * If both are present, they are concatenated into a hierarchical string
     * structure.
     * </p>
     *
     * @return a formatted string label representing the hierarchical path of the
     *         tag
     */
    public String toDisplayLabel() {
        if (microTag == null || microTag.isBlank()) {
            return macroTag;
        }
        return macroTag + " > " + microTag;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof EventTag eventTag)) {
            return false;
        }
        return Objects.equals(macroTag, eventTag.macroTag)
                && Objects.equals(microTag, eventTag.microTag);
    }

    @Override
    public int hashCode() {
        return Objects.hash(macroTag, microTag);
    }
}
